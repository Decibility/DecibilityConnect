package com.decibility.ledmanager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.decibility.audioprocessing.AudioState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class DecibilityLEDs {
    // Bluetooth Communication Stream
    private final OutputStream mOutStream;

    // Stores the value of each LED
    private final DecibilityLED [] volumeLEDs;
    private final DecibilityLED [] frequencyLEDs;

    @SuppressLint("NewApi")
    public DecibilityLEDs(BluetoothSocket socket) {
        OutputStream tmpOut = null;

        // Get the output stream using temp object because the member variable is final
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("IO_stream_get_fail", e.toString());
        }
        mOutStream = tmpOut;

        // Set up arrays for volume and frequency LEDs
        volumeLEDs = new DecibilityLED[LEDConstants.NUM_VOL_LEDS];
        frequencyLEDs = new DecibilityLED[LEDConstants.NUM_FREQ_LEDS];

        for(int i = 0; i < LEDConstants.NUM_VOL_LEDS; i++) {
            volumeLEDs[i] = new DecibilityLED();
        }

        for(int i = 0; i < LEDConstants.NUM_FREQ_LEDS; i++) {
            frequencyLEDs[i] = new DecibilityLED();
        }
    }

    // Set all LEDs to target a color
    public void setAll(int r, int g, int b) {
        for(DecibilityLED led: volumeLEDs) led.setColor(r, g, b);
        for(DecibilityLED led: frequencyLEDs) led.setColor(r, g, b);
    }

    // Set LEDs to their target color by
    public void updateAll() {
        // Build message to update LEDs as a string
        StringBuilder msg = new StringBuilder();

        msg.append(LEDConstants.LED_MESSAGE_START_CHAR);
        for(DecibilityLED led: volumeLEDs) msg.append(led.getTargetColor().toOutputString());
        for(DecibilityLED led: frequencyLEDs) msg.append(led.getTargetColor().toOutputString());
        msg.append(LEDConstants.LED_MESSAGE_END_CHAR);

        // Send the message while checking for errors
        try {
            mOutStream.write(msg.toString().getBytes());
        } catch (IOException e) {
            Log.d("Write_msg_fail", e.toString());
            return;
        }

        // If the message was successful, mark the LED colors as updated
        for(DecibilityLED led: volumeLEDs) led.markUpdated();
        for(DecibilityLED led: frequencyLEDs) led.markUpdated();
    }

    public void updateFromState(AudioState state) {
        if(state.getAvgVolume() < LEDConstants.MIN_VOLUME) {
            for(DecibilityLED led: volumeLEDs) led.setColor(0, 0, 10);
        } else if (state.getAvgVolume() > LEDConstants.MAX_VOLUME) {
            for(DecibilityLED led: volumeLEDs) led.setColor(10, 0, 0);
        } else {
            for(DecibilityLED led: volumeLEDs) led.setColor(0, 10, 0);
        }

        if(state.getMainFrequency() < LEDConstants.MIN_FREQUENCY) {
            for(DecibilityLED led: frequencyLEDs) led.setColor(0, 0, 10);
        } else if (state.getMainFrequency() > LEDConstants.MAX_FREQUENCY) {
            for(DecibilityLED led: frequencyLEDs) led.setColor(10, 0, 0);
        } else {
            for(DecibilityLED led: frequencyLEDs) led.setColor(0, 10, 0);
        }

        this.updateAll();
    }

//    public void write_config_data(String min_volume_input, String max_volume_input, String min_freq_input, String max_freq_input) {
//        String msg = min_volume_input + max_volume_input + min_freq_input + max_freq_input;
//        try {
//            mOutStream.write(msg.getBytes());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}

class DecibilityLED {
    private final DecibilityLEDColor currentColor;
    private final DecibilityLEDColor targetColor;

    public DecibilityLED() {
        currentColor = new DecibilityLEDColor(0, 0, 0);
        targetColor = new DecibilityLEDColor(0, 0, 0);
    }

    public void setColor(int r, int g, int b) {
        targetColor.setRed(r);
        targetColor.setGreen(g);
        targetColor.setBlue(b);
    }

    public DecibilityLEDColor getCurrentColor() {
        return currentColor;
    }

    public DecibilityLEDColor getTargetColor () {
        return targetColor;
    }

    public void markUpdated() {
        currentColor.setRed(targetColor.getRed());
        currentColor.setGreen(targetColor.getGreen());
        currentColor.setBlue(targetColor.getBlue());
    }
}

class DecibilityLEDColor {
    private int red;
    private int green;
    private int blue;

    public DecibilityLEDColor(int red, int green, int blue) {
        setRed(red);
        setGreen(green);
        setBlue(blue);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void setRed(int red) {
        this.red = Math.min(Math.max(red, 0), 255);
    }

    public void setGreen(int green) {
        this.green = Math.min(Math.max(green, 0), 255);
    }

    public void setBlue(int blue) {
        this.blue = Math.min(Math.max(blue, 0), 255);
    }

    public String toOutputString() {
        return new String(new char[]{(char) this.red, (char) this.green, (char) this.blue});
    }
}
