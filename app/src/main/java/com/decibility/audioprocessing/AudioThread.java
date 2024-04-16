package com.decibility.audioprocessing;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.decibility.ledmanager.DecibilityLEDs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

// Thread that handles receiving audio from the wearable
public class AudioThread extends Thread {
    // Bluetooth Communication
    private final BluetoothSocket mSocket;
    private final InputStream mInStream;

    // Stores the value of each ADC sample
    private final AudioState mAudioState;

    // Reference to LEDs that should be updated
    private final DecibilityLEDs mLEDs;

    // Context for preferences
    private final Context mContext;

    // Preferences to get audio parameter preferences
    private final SharedPreferences sharedPreferences;

    private boolean isRunning; // Flag to show if the thread is running
    private boolean halted;

    public AudioThread(BluetoothSocket socket, Context context) {
        mSocket = socket;
        InputStream tmpIn = null;

        // Get the input stream using temp object because the member variable is final
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.d("IO_stream_get_fail", e.toString());
        }
        mInStream = tmpIn;

        // Thread is not running until .run() is called
        isRunning = false;
        halted = false;

        mAudioState = new AudioState();
        mLEDs = new DecibilityLEDs(socket);

        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public void run() {
        isRunning = true;

        // Buffer to store samples while the burst is being verified
        short[] sampleBuffer = new short[AudioConstants.NUM_SAMPLES];

        // Read each burst as it comes in, and use the samples to update the audio state
        // If a burst is read incorrectly, it will be ignored and the next one will be read
        // If there is an issue with the bluetooth input stream, the thread's execution will end
        while (!halted) {
            try {
                // Update bounds from sharedPreferences
                mLEDs.write_config_data(
                    sharedPreferences.getString("min_volume_input", ""),
                    sharedPreferences.getString("max_volume_input", ""),
                    sharedPreferences.getString("min_frequency_input", ""),
                    sharedPreferences.getString("max_frequency_input", "")
                );

                // Wait until enough bytes for all the data and a start and end byte are available
                if(mInStream.available() < AudioConstants.BURST_LEN)
                    continue;

                // If the start byte is not right, we will ignore the rest of the data, and keep
                // reading one byte at a time until we find the start byte
                verifyByte(AudioConstants.BURST_START_CHAR, AudioConstants.INCORRECT_START_CHAR);

                // Read Samples into a temporary buffer
                readAudioData(sampleBuffer);

                // Check to make sure that the end byte is there. If it isn't, there is something
                // wrong with the message and we should discard it and wait for the next one
                verifyByte(AudioConstants.BURST_END_CHAR, AudioConstants.INCORRECT_END_CHAR);

                // At this point, we have verified that the burst is valid and we can use the new samples
                // to update the Audio State
                mAudioState.update(sampleBuffer);

                // Update LEDs Based on Audio State
                mLEDs.updateFromState(mAudioState);

            } catch (AudioReadException e) {
                Log.w(AudioConstants.AUDIO_TAG, Objects.requireNonNull(e.getMessage()));
            } catch (Exception e) {
                Log.e(AudioConstants.AUDIO_TAG, AudioConstants.BT_READ_FAILURE);
                e.printStackTrace();
                isRunning = false;
                break;
            }
        }
    }

    // Reads one byte from the input buffer, and verifies that it matches the provided byte
    // If it doesn't, this method throws an AudioReadException with the provided message
    private void verifyByte(byte correctByte, String failErrorString) throws IOException, AudioReadException {
        byte[] test = new byte[1];
        int numRead  = mInStream.read(test, 0, 1);

        if(numRead != 1 || test[0] != correctByte) {
            throw new AudioReadException(failErrorString);
        }
    }

    // Reads the data from the burst into the sample buffer
    // If there is an issue with the data it will throw an AudioReadException
    private void readAudioData(short[] sampleBuffer) throws IOException, AudioReadException {
        // Reads the raw bytes from the input stream
        byte[] byteBuffer = new byte[AudioConstants.BUFFER_LEN];
        int numRead = mInStream.read(byteBuffer, 0, AudioConstants.BUFFER_LEN);

        // If the wrong number of bytes was read, this burst has an issue
        if(numRead != AudioConstants.BUFFER_LEN) {
            throw new AudioReadException(AudioConstants.INCORRECT_NUM_SAMPLES);
        }

        // Get the actual sample value, as there are two bytes per sample
        for(int i = 0; i < AudioConstants.NUM_SAMPLES; i++) {
            sampleBuffer[i] = (short)(((byteBuffer[i * 2 + 1] & 0xFF) << 8) | (byteBuffer[i * 2] & 0xFF));
        }
    }

    public AudioState getAudioState() {
        return mAudioState;
    }

    public DecibilityLEDs getmLEDs() {
        return mLEDs;
    }

    // Stops trying to read audio data
    public void halt() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(AudioConstants.AUDIO_TAG, AudioConstants.SOCKET_CLOSE_FAILURE);
            e.printStackTrace();
        }
        halted = true;
    }
}

// Custom Exception to handle errors during audio read
class AudioReadException extends Exception {
    public AudioReadException(String message) {
        super(message == null ? "No message was provided" : message);
    }
}
