package com.iot.noisemap.noiserecorder.audioprocessing;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public final class RecordingConfig {
    public static final int SAMPLE_RATE_IN_HZ = 44100;
    public static final int FFTS_PER_SECOND = 20;
    public static final int RECORDING_DURATION_IN_MS = 1000;

    public static final int BLOCK_SIZE_FFT = SAMPLE_RATE_IN_HZ / FFTS_PER_SECOND;

    // VOICE_RECOGNITION avoids some audio preprocessing
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE_IN_BYTES = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
    public static final double FREQUENCY_RESOLUTION = SAMPLE_RATE_IN_HZ / (double) BLOCK_SIZE_FFT;
}
