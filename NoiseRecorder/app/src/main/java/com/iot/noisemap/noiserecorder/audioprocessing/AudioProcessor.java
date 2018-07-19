package com.iot.noisemap.noiserecorder.audioprocessing;
import org.jtransforms.fft.DoubleFFT_1D;

/**
 *  Performs the audio processing.
 */
public class AudioProcessor {

    private static final double REFERENCE_LOG = Math.log10(0.00002);
    private final DoubleFFT_1D fft = new DoubleFFT_1D(RecordingConfig.BLOCK_SIZE_FFT);
    private double window_function[] = new double[RecordingConfig.BLOCK_SIZE_FFT];
    private double[] a_weighting = new double[RecordingConfig.BLOCK_SIZE_FFT];
    private int numberOfFFTs = 0;
    private double averageDB = 0;
    private double calibrationOffset_db;

    /**
     * Creates a new audio processor.
     * @param calibrationOffset_db calibration offset for the calculated noise.
     */
    public AudioProcessor(double calibrationOffset_db) {
        initCalculations();
        this.calibrationOffset_db = calibrationOffset_db;
    }

    /**
     * Initializes the window function and the A-weighting factors.
     */
    private void initCalculations() {
        calculateHannWindow();
        calculateAWeighting();
    }

    /**
     * Initializes the hann window.
     */
    private void calculateHannWindow() {
        for(int i = 0; i < RecordingConfig.BLOCK_SIZE_FFT; i++) {
            double hanningTemp = (2*Math.PI * i) / (RecordingConfig.BLOCK_SIZE_FFT - 1);
            window_function[i] = (1 - Math.cos(hanningTemp)) * 0.5;
        }
    }


    /**
     * Calculates the A-weighting.
     */
    private void calculateAWeighting() {
        for (int i = 0; i< RecordingConfig.BLOCK_SIZE_FFT /2; i++) {
            double f = i * RecordingConfig.FREQUENCY_RESOLUTION;
            double f2 = f * f;
            double f4 = f2 * f2;
            //DIN EN 61672-1: approximation based on http://www.sengpielaudio.com/BerechnungDerBewertungsfilter.pdf
            a_weighting[i] = (12200 * 12200 * f4) / ((f2 + 20.6 * 20.6) * (f2 + 12200 * 12200) * Math.sqrt(f2 + 107.7 * 107.7) * Math.sqrt(f2 + 737.9 * 737.9));
        }
    }

    /**
     * Gets called when the measurement is done.
     * @return average noise.
     */
    public double finishProcess() {
        double average = averageDB / numberOfFFTs;
        reset();
        return average;
    }

    /**
     * Resets parameters: Number of performed FFTs and average dBA.
     */
    private void reset() {
        numberOfFFTs = 0;
        averageDB = 0;
    }

    /**
     * Processes an incoming value buffer of noise samples.
     * @param valueBuffer incoming value buffer.
     */
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
            //4
            double magn = Math.sqrt(real*real+imag*imag);

            // threshold of the human hearing = reference for the db calculation
            if(magn > 0.0 ) {
                // 5
                double dbFreqA = (10.0 * (Math.log10(magn*magn*a_weighting[i]) - REFERENCE_LOG) + this.calibrationOffset_db);
                //6
                dbFreqA = (10.0 * Math.pow(10,dbFreqA/10));
                // 7a sum in log
                sumOfAmplitudes += dbFreqA;
            }
            else {
                continue; // invalid magnitude, f.e. when the audio recorder is still starting the recording
            }
        }
        // 7b
        if(sumOfAmplitudes > 0) {
            double curAverage_dB = 10*Math.log10(sumOfAmplitudes);
            numberOfFFTs++;
            averageDB += curAverage_dB;
        }
    }

    /**
     * Sets the calibration offset.
     * @param calibrationOffset Desired calibration offset.
     */
    public void setCalibrationOffset(double calibrationOffset) {
        this.calibrationOffset_db = calibrationOffset;
    }
}
