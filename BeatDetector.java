package core;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BeatDetector {

    public interface BeatListener {
        void onBeat(double bpm, double intensity);
    }

    private BeatListener listener;
    private Timer timer;
    private Random random;
    private double currentBpm = 100.0; // default BPM
    private String genre = "pop";

    // For beat detection
    private static final int ENERGY_HISTORY_SIZE = 43;  // typical size for averaging
    private float[] energyHistory = new float[ENERGY_HISTORY_SIZE];
    private int energyIndex = 0;
    private boolean initialized = false;

    public BeatDetector() {
        random = new Random();
        // Optionally keep simulateBeats for fallback or remove it
        // simulateBeats();
    }

    public void setOnBeatDetected(BeatListener listener) {
        this.listener = listener;
    }

    public void setGenre(String genre) {
        this.genre = genre.toLowerCase();
        this.currentBpm = estimateBpmForGenre(this.genre);
    }

    private double estimateBpmForGenre(String genre) {
        return switch (genre) {
            case "pop" -> 100 + random.nextDouble() * 20;      // 100–120
            case "rock" -> 110 + random.nextDouble() * 30;     // 110–140
            case "hiphop", "rap" -> 85 + random.nextDouble() * 15; // 85–100
            case "edm", "dance" -> 120 + random.nextDouble() * 30; // 120–150
            case "jazz" -> 60 + random.nextDouble() * 20;       // 60–80
            case "classical" -> 50 + random.nextDouble() * 20;  // 50–70
            default -> 100;
        };
    }

    /**
     * Analyze the current audio spectrum magnitudes to detect beats.
     * @param magnitudes float array of audio spectrum magnitudes
     */
    public void analyzeAudio(float[] magnitudes) {
        if (magnitudes == null || magnitudes.length == 0) return;

        // Calculate instant energy (sum of squared magnitudes)
        float instantEnergy = 0;
        for (float mag : magnitudes) {
            instantEnergy += mag * mag;
        }

        // Initialize energy history if not done
        if (!initialized) {
            for (int i = 0; i < ENERGY_HISTORY_SIZE; i++) {
                energyHistory[i] = instantEnergy;
            }
            initialized = true;
        }

        // Calculate average energy from history
        float averageEnergy = 0;
        for (float e : energyHistory) {
            averageEnergy += e;
        }
        averageEnergy /= ENERGY_HISTORY_SIZE;

        // Calculate variance (optional, for adaptive threshold)
        float variance = 0;
        for (float e : energyHistory) {
            variance += (e - averageEnergy) * (e - averageEnergy);
        }
        variance /= ENERGY_HISTORY_SIZE;
        float C = (float)(-0.0025714 * variance + 1.5142857); // empirically determined constant

        // Detect beat if instant energy is greater than threshold * average energy
        if (instantEnergy > C * averageEnergy) {
            // Beat detected - call listener
            if (listener != null) {
                // Intensity normalized between 0.6 and 1.0 based on energy ratio
                double intensity = Math.min(1.0, 0.6 + (instantEnergy / averageEnergy - C) / (2 * C));
                listener.onBeat(currentBpm, intensity);
            }
        }

        // Update energy history circular buffer
        energyHistory[energyIndex] = instantEnergy;
        energyIndex = (energyIndex + 1) % ENERGY_HISTORY_SIZE;
    }

    /**
     * Optional: stop the simulated beat timer if you use it
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    // Uncomment if you want to keep simulation for fallback
    /*
    private void simulateBeats() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (listener != null) {
                    double intensity = 0.6 + random.nextDouble() * 0.4; // range: 0.6–1.0
                    listener.onBeat(currentBpm, intensity);
                }
            }
        }, 0, 600); // fire roughly every 600ms
    }
    */
}
