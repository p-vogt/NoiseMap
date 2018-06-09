package com.example.patrick.noiserecorder.audioprocessing;

import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;
import android.os.Handler;
import android.widget.Toast;

import com.example.patrick.noiserecorder.location.LocationServiceConnection;
import com.example.patrick.noiserecorder.location.LocationTrackerService;
import com.example.patrick.noiserecorder.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AudioRecorder {

    private AudioProcessor fft = new AudioProcessor();
    private double lastAverageDbA = -1.0d;
    boolean isRecording = false;
    private long timeStartedRecordingInMs;
    private LocationServiceConnection serviceConnection = new LocationServiceConnection();
    // with this flag the audio recorder gets started again (at the start of a new measurement)
    private boolean startNewRecording = true;
    MainActivity callingActivity;

    AudioRecord audioRecorder;
    private String timestampOfLastAverageDbA = "";

    public AudioRecorder(MainActivity caller) {
        this.callingActivity = caller;
        // Bind to LocalService
        Intent locationIntent = new Intent(caller, LocationTrackerService.class);
        caller.bindService(locationIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);

    }
    /**
     * Starts the audio recorder and sets the record starting time.
     */
    public void startRecording() {
        if(!isRecording) {
            isRecording = true;
            final Handler recordingHandler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(isRecording) {
                        if(startNewRecording) {
                            Calendar calendar = Calendar.getInstance();
                            timeStartedRecordingInMs = calendar.getTimeInMillis();
                            audioRecorder = new AudioRecord(
                                    RecordingConfig.AUDIO_SOURCE,
                                    RecordingConfig.SAMPLE_RATE_IN_HZ,
                                    RecordingConfig.CHANNEL_CONFIG,
                                    RecordingConfig.AUDIO_FORMAT,
                                    RecordingConfig.BUFFER_SIZE_IN_BYTES);
                            audioRecorder.startRecording();
                            startNewRecording = false;
                        }

                        // Recursive call
                        final int delayTimeToNextCall = processAudioData();
                        recordingHandler.postDelayed(this, delayTimeToNextCall);
                    }
                }
            };

            // Start
            recordingHandler.post(runnable);
        }
    }
    public boolean isRecording() {
        return isRecording;
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
    public void stopRecording() {
        if(isRecording) {
            if(audioRecorder != null) {
                audioRecorder.stop();
                audioRecorder.release();
            }
            isRecording = false;
        }
    }

    /**
     * Returns the last calculated average dBA value.
     * @return the last calculated average dBA value.
     */
    public double getLastAverageDbA() {
        return lastAverageDbA;
    }

    public String getTimestampOfLastAverageDbA() {
        return timestampOfLastAverageDbA;
    }
    /**
     * Finishes one measurement process (multiple FFTs).
     * Stops recording, calculates the average dBA and triggers a location request.
     * Also plots the measurement at the gui.
     */
    private void finishMeasurement() {
        if(audioRecorder != null) {
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = timestampFormat.format(calendar.getTime());
        startNewRecording = true;

        this.timestampOfLastAverageDbA = timestamp;
                lastAverageDbA = fft.finishProcess();
        this.serviceConnection.requestLocation();
        //callingActivity.onNewMeasurementDone(this.lastAverageDb);
    }

}
