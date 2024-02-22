package com.decibility.audioprocessing;

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
        avgVolume = 0;
        for(short sampleVal: samples) {
            avgVolume += sampleVal;
        }
        avgVolume /= AudioConstants.NUM_SAMPLES;
    }

    public void updateFrequency() {
        float[] fftBuffer = new float[AudioConstants.NUM_SAMPLES];
        for(int i = 0; i < AudioConstants.NUM_SAMPLES; i++) {
            fftBuffer[i] = (float) samples[i];
        }

        mFFT.forward(fftBuffer);

        int maxIndex = 0;
        double maxAmplitude = 0;
        for(int i = 0; i < AudioConstants.NUM_SAMPLES; i++) {
            if(fftBuffer[i] > maxAmplitude) {
                maxAmplitude = fftBuffer[i];
                maxIndex = i;
            }
        }

        mainFrequency = mFFT.indexToFreq(maxIndex);
    }

    public double getAvgVolume() {
        return avgVolume;
    }

    public double getMainFrequency() {
        return mainFrequency;
    }
}
