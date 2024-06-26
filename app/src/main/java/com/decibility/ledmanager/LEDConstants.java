package com.decibility.ledmanager;

public class LEDConstants {
    // Number of LEDs on each strip
    // Must be the same as the constant in the device drivers
    public static final int NUM_VOL_LEDS = 3;
    public static final int NUM_FREQ_LEDS = 3;

    // Start and stop chars for the LED setting message
    public static final char LED_MESSAGE_START_CHAR = 'w';
    public static final char LED_MESSAGE_END_CHAR = '\n';

    // TEMPORARY values to store max and min bounds for volume/frequency
    public static final double DEFAULT_MIN_VOLUME = 50;
    public static final double DEFAULT_MAX_VOLUME = 65;
    public static final double DEFAULT_MIN_FREQUENCY = 100;
    public static final double DEFAULT_MAX_FREQUENCY = 2000;

    // Error Strings
    public static final String LED_TAG = "LED Write Error";
    public static final String BOUND_FORMAT_ERROR = "Error writing new bounds: ";
}

