package com.decibility.audioprocessing;

import android.util.Log;

import com.decibility.audioprocessing.fft.FFT;

import java.util.Arrays;

public class AudioState {
    // Buffer to store the latest set of raw samples
    private short[] samples;

    // Needed Audio Data characteristics
    private double avgVolume;
    private double mainFrequency;

    // FFT Object used for frequency analysis
    private final FFT mFFT;

    AudioState() {
        samples = new short[AudioConstants.NUM_SAMPLES];
        avgVolume = 0;
        mainFrequency = 0;

        mFFT = new FFT(AudioConstants.NUM_SAMPLES, AudioConstants.AUDIO_SAMPLE_RATE);
    }

    public void update(short[] newSamples) {
        samples = Arrays.copyOf(newSamples, newSamples.length);

        updateVolume();
        updateFrequency();
    }

    public void updateVolume() {
        // Calculate and Remove the DC Offset of the audio signal
        double dcOffset = 0;
        for(short sampleVal: samples) {
            dcOffset += sampleVal;
        }
        dcOffset /= AudioConstants.NUM_SAMPLES;

        // Find average amplitude of the audio signal
        avgVolume = 0;
        for(short sampleVal: samples) {
            avgVolume += Math.abs((double)sampleVal - dcOffset);
        }
        avgVolume /= AudioConstants.NUM_SAMPLES;

        // Convert average amplitude to decibels
        avgVolume = 20 * Math.log10(avgVolume) + AudioConstants.AUDIO_OFFSET_DB;

        Log.v(AudioConstants.AUDIO_TAG, "Average Sample Volume (dB): " + avgVolume);
    }

    public void updateFrequency() {
        float[] fftBuffer = new float[AudioConstants.NUM_SAMPLES];
        for(int i = 0; i < AudioConstants.NUM_SAMPLES; i++) {
            fftBuffer[i] = samples[i];
        }

        mFFT.forward(fftBuffer);

        int maxIndex = 0;
        double maxAmplitude = 0;
        for(int i = 0; i < AudioConstants.NUM_SAMPLES / 2; i++) { // Only look at the positive frequency bins
            if(fftBuffer[i] > maxAmplitude) {
                maxAmplitude = fftBuffer[i];
                maxIndex = i;
            }
        }

        mainFrequency = mFFT.indexToFreq(maxIndex);

        Log.v(AudioConstants.AUDIO_TAG, "Dominant Frequency: " + mainFrequency + " (Bucket num: " + maxIndex + " )");
    }

    public double getAvgVolume() {
        return avgVolume;
    }

    public double getMainFrequency() {
        return mainFrequency;
    }
}
