package com.example.fpmobile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecorderHelper {
    private static final String TAG = "AudioRecorderHelper";
    private static final int SAMPLE_RATE = 44100; // Default audio sample rate
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
    );

    private AudioRecord audioRecord;
    private boolean isRecording = false;

    public AudioRecorderHelper() {
        try {
            if (BUFFER_SIZE == AudioRecord.ERROR_BAD_VALUE || BUFFER_SIZE == AudioRecord.ERROR) {
                Log.e(TAG, "Invalid buffer size.");
                return;
            }

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed.");
                audioRecord = null;
            } else {
                Log.d(TAG, "AudioRecord initialized successfully.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AudioRecorderHelper", e);
        }
    }

    public boolean isInitialized() {
        return audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }

    public void startRecording() {
        if (!isInitialized()) {
            Log.e(TAG, "AudioRecord is not initialized. Cannot start recording.");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;
        Log.d(TAG, "Recording started.");
    }

    public void stopRecording() {
        if (audioRecord != null && isRecording) {
            audioRecord.stop();
            isRecording = false;
            Log.d(TAG, "Recording stopped.");
        }
    }

    public void release() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
            Log.d(TAG, "AudioRecord resources released.");
        }
    }

    public float getAmplitude() {
        if (!isRecording) {
            Log.e(TAG, "Cannot get amplitude. AudioRecord is not recording.");
            return -1;
        }

        short[] buffer = new short[BUFFER_SIZE];
        int read = audioRecord.read(buffer, 0, buffer.length);

        if (read > 0) {
            double sum = 0;
            for (short sample : buffer) {
                sum += sample * sample;
            }

            // Calculate RMS (Root Mean Square)
            double rms = Math.sqrt(sum / read);

            // Convert to decibels
            if (rms > 0) {
                return (float) (20 * Math.log10(rms)); // dB = 20 * log10(rms)
            } else {
                return 0; // Silence
            }
        } else {
            Log.e(TAG, "Failed to read audio data.");
            return -1;
        }
    }
}