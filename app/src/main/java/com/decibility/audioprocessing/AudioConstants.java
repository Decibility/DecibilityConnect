package com.decibility.audioprocessing;

public class AudioConstants {
    // *************** Audio Sample Input Characteristics *************** //

    // Number of samples sent in one burst from the wearable
    // Must match the number specified in the wearable drivers
    // Must be a power of two or the audio processing will have issues
    public static final short NUM_SAMPLES = 16;

    // Calculated length of the buffer needed to store sample bytes
    public static final short BUFFER_LEN = NUM_SAMPLES * 2;

    // Total number of bytes in sample burst, including start and end bytes
    public static final short BURST_LEN = BUFFER_LEN + 2;

    // Sampling Rate of the samples in Hz
    // Must be the same as the sampling rate of the ADC as specified in the device drivers
    public static final int AUDIO_SAMPLE_RATE = 44100;

    // Length of time that the samples covers in seconds
    public static final double AUDIO_TIMESLICE_LENGTH = (double) NUM_SAMPLES / AUDIO_SAMPLE_RATE;

    // Characters used to mark the start and end of an audio burst
    // Must be the same as defined in the wearable drivers
    public static final byte BURST_START_CHAR = (byte)'a';
    public static final byte BURST_END_CHAR = (byte)'\n';

    // ********************** Audio Logging Messages ******************** //

    // Logging Tag
    public static final String AUDIO_TAG = "Audio Read: ";

    // Warnings
    public static final String INCORRECT_START_CHAR = "Burst has incorrect start char";
    public static final String INCORRECT_END_CHAR = "Burst has incorrect end char";
    public static final String INCORRECT_NUM_SAMPLES = "Burst has incorrect number of samples";

    // Errors
    public static final String BT_READ_FAILURE = "Failed to read from the BT input stream";
    public static final String SOCKET_CLOSE_FAILURE = "BT socket failed to close";

}
