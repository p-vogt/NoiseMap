package com.example.patrick.noiserecorder.audioprocessing;

import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;
import android.os.Handler;
import android.widget.Toast;

import com.example.patrick.noiserecorder.location.LocationServiceConnection;
import com.example.patrick.noiserecorder.location.LocationTrackerService;
import com.example.patrick.noiserecorder.MainActivity;

import java.util.Calendar;

public class AudioRecorder {

    private AudioProcessing fft = new AudioProcessing();
    private double lastAverageDb = -1.0d;
    boolean isRecording = false;
    private long timeStartedRecordingInMs;
    private LocationServiceConnection serviceConnection = new LocationServiceConnection();
    MainActivity callingActivity;

    AudioRecord audioRecorder = new AudioRecord(
            RecordingConfig.AUDIO_SOURCE,
            RecordingConfig.SAMPLE_RATE_IN_HZ,
            RecordingConfig.CHANNEL_CONFIG,
            RecordingConfig.AUDIO_FORMAT,
            RecordingConfig.BUFFER_SIZE_IN_BYTES);

    public AudioRecorder(MainActivity caller) {
        this.callingActivity = caller;
        if (audioRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Toast.makeText(caller, "Audio recorder not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        // Bind to LocalService
        Intent locationIntent = new Intent(caller, LocationTrackerService.class);
        caller.bindService(locationIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);

        final Handler recordingHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(!isRecording) {
                    startRecording();
                }
                // Recursive call
                final int delayTimeToNextCall = processAudioData();
                recordingHandler.postDelayed(this, delayTimeToNextCall);
            }
        };

        //Start
        recordingHandler.post(runnable);
    }
    /**
     * Starts the audio recorder and sets the record starting time.
     */
    private void startRecording() {

        isRecording = true;
        Calendar calendar = Calendar.getInstance();
        timeStartedRecordingInMs = calendar.getTimeInMillis();
        audioRecorder.startRecording();

    }

    /**
     * Processes one block of audio. Calculates the FFT and
     * @returns the number of milliseconds until the next recording should occur.
     */
    private int processAudioData() {

        // retrieve values from the audio buffer
        short[] valueBuffer = new short[RecordingConfig.BLOCK_SIZE_FFT];
        int elementsRead = audioRecorder.read(valueBuffer, 0, RecordingConfig.BLOCK_SIZE_FFT);
        if(elementsRead < 0) {
            return 1; // TODO
        }
        // process the data
        fft.process(valueBuffer);
        Calendar calendar = Calendar.getInstance();

        long currentRecordingTimeInMs = calendar.getTime().getTime() - timeStartedRecordingInMs;

        if(currentRecordingTimeInMs >= RecordingConfig.RECORDING_DURATION_IN_MS) {
            finishMeasurement();
            return RecordingConfig.DELAY_BETWEEN_MEASUREMENTS_IN_MS;
        }
        // TODO
        return 1;
    }


    /**
     * Stops the audio recording.
     */
    private void stopRecording() {
        audioRecorder.stop();
        isRecording = false;
    }


    /**
     * Returns the last calculated average dBA value.
     * @return the last calculated average dBA value.
     */
    public double getLastAverageDb() {
        return lastAverageDb;
    }

    /**
     * Finishes one measurement process (multiple FFTs).
     * Stops recording, calculates the average dBA and triggers a location request.
     * Also plots the measurement at the gui.
     */
    private void finishMeasurement() {
        stopRecording();
        lastAverageDb = fft.finishProcess();
        this.serviceConnection.requestLocation();
        callingActivity.onNewMeasurementDone(this.lastAverageDb);
    }

}
