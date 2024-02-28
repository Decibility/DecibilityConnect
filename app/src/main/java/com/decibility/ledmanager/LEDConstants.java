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
    public static final double MIN_VOLUME = 100;
    public static final double MAX_VOLUME = 1000;
    public static final double MIN_FREQUENCY = 10;
    public static final double MAX_FREQUENCY = 1000;
}

