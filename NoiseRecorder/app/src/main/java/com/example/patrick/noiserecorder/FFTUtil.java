package com.example.patrick.noiserecorder;

import android.view.WindowManager;

import java.util.Calendar;
import java.util.Date;
import org.jtransforms.fft.DoubleFFT_1D;


public class FFTUtil {

    final DoubleFFT_1D fft = new DoubleFFT_1D(RecordingConfig.BLOCK_SIZE_FFT);
    double window_function[] = new double[RecordingConfig.BLOCK_SIZE_FFT];
    private double[] a_weighting = new double[RecordingConfig.BLOCK_SIZE_FFT];
    private int numberOfFFTs = 0;
    private double averageDB = 0;

    FFTUtil() {

        initCalculations();
    }

    private void initCalculations() {
        calculateHannWindow();
        calculateAWeighting();
    }
    private void calculateHannWindow() {
        for(int i = 0; i < RecordingConfig.BLOCK_SIZE_FFT; i++) {
            double hanningTemp = (2*Math.PI * i) / (RecordingConfig.BLOCK_SIZE_FFT - 1 );
            window_function[i] = (1 - Math.cos(hanningTemp)) * 0.5;
        }
    }

    private void calculateAWeighting() {
        for (int i = 0; i< RecordingConfig.BLOCK_SIZE_FFT /2; i++) {
            double f = i * RecordingConfig.FREQUENCY_RESOLUTION;
            double f2 = f * f;
            double f4 = f2 * f2;
            a_weighting[i] = (12200 * 12200 * f4) / ((f2 + 20.6 * 20.6) * (f2 + 12200 * 12200) * Math.sqrt(f2 + 107.7 * 107.7) * Math.sqrt(f2 + 737.9 * 737.9));
        }
    }
    public double finishProcess() {
        double average = averageDB / numberOfFFTs;
        reset();
        return average;
    }
    private void reset() {
        numberOfFFTs = 0;
        averageDB = 0;
    }

    public void process(short[] valueBuffer) {


        double vals[] = new double[RecordingConfig.BLOCK_SIZE_FFT];

        for (int i = 0; i < RecordingConfig.BLOCK_SIZE_FFT; i++) {
            double normalized =  valueBuffer[i] / (double) Short.MAX_VALUE;
            vals[i] = normalized * window_function[i];
        }

        //==================
        //calculate fft
        //==================
        fft.realForward(vals);

        double sumOfAmplitudes = 0;

        // only half of the fft block size, because we now have real + imag values
        for (int i = 0; i < RecordingConfig.BLOCK_SIZE_FFT/2; i++) {

            double real = vals[2*i];
            double imag = vals[2*i+1];
            // TODO setting + configurable
            double calibration_offset = -1.75; // in dBA
            // TODO Window_sample_count/2 correct?
            //763
            double magn = Math.sqrt(real*real+imag*imag);

            // threshold of the human hearing = reference for the db calculation
            final double amplitudeRef = 0.00002;
            if(magn > 0.0 ) {
                // 5
                double dbFreqA = (10.0 * Math.log10(magn*magn*a_weighting[i]/amplitudeRef) + calibration_offset);
                //6
                dbFreqA = (10.0 * Math.pow(10,dbFreqA/10));
                // 7a sum in log
                sumOfAmplitudes += dbFreqA;
            }
            else {
                continue; // invalid magnitude, f.e. when the audio recorder still starting the recording
            }
        }
        // 7b
        if(sumOfAmplitudes > 0) {
            double curAverage_dB = 10*Math.log10(sumOfAmplitudes);
            numberOfFFTs++;
            averageDB += curAverage_dB;
        }
    }
}
