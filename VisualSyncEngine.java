package core;

import java.util.ArrayList;
import java.util.List;

/**
 * VisualSyncEngine synchronizes visual parameters with musical beats and user interactions,
 * producing dynamic visual effects for use in music visualizers or generative art systems.
 */
public class VisualSyncEngine {

    // === Music Input State ===
    private double currentTempo = 120.0;
    private double currentBeatIntensity = 0.5;
    private long lastBeatTimestamp = 0;
    private final List<Double> recentBeatIntensities = new ArrayList<>();

    // === Visual State ===
    private double beatInfluence = 0.0;
    private double visualPhase = 0.0;

    private double rotationSpeed = 1.0;
    private double scaleMultiplier = 1.0;
    private double colorShift = 0.0;
    private double complexity = 0.5;

    // === Interaction State ===
    private double interactionRipple = 0.0;
    private long lastInteractionTimestamp = 0;

    // === Beat Handling ===

    /**
     * Updates the visual engine with the latest beat data.
     *
     * @param tempo    The current tempo (BPM).
     * @param intensity The intensity of the beat (0.0–1.0).
     */
    public void updateBeat(double tempo, double intensity) {
        currentTempo = tempo;
        currentBeatIntensity = intensity;
        lastBeatTimestamp = System.currentTimeMillis();

        // Maintain recent beat history
        recentBeatIntensities.add(intensity);
        if (recentBeatIntensities.size() > 20) {
            recentBeatIntensities.remove(0);
        }

        beatInfluence = calculateBeatInfluence();
        updateVisualParameters();
    }

    private double calculateBeatInfluence() {
        if (recentBeatIntensities.isEmpty()) return 0.0;

        double average = recentBeatIntensities.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double deviation = Math.abs(currentBeatIntensity - average);
        long timeSinceLastBeat = System.currentTimeMillis() - lastBeatTimestamp;
        double decay = Math.exp(-timeSinceLastBeat / 1000.0); // decay over 1 second

        return Math.min(1.0, (currentBeatIntensity + deviation) * decay);
    }

    private void updateVisualParameters() {
        rotationSpeed = 0.5 + (currentTempo / 120.0) * 0.5;
        scaleMultiplier = 0.8 + currentBeatIntensity * 0.4;

        colorShift = (colorShift + beatInfluence * 0.1) % 1.0;
        complexity = Math.min(1.0, (currentTempo / 160.0) + (currentBeatIntensity * 0.5));
    }

    // === Animation Update ===

    /**
     * Updates visual parameters based on animation timing and user input.
     *
     * @param phase           The current animation phase.
     * @param mouseInfluence  The degree of influence from user interaction (0.0–1.0).
     */
    public void update(double phase, double mouseInfluence) {
        visualPhase = phase;

        long now = System.currentTimeMillis();
        if (now - lastInteractionTimestamp > 2000) {
            interactionRipple *= 0.95; // slow decay after 2 seconds of inactivity
        }

        double influence = beatInfluence + mouseInfluence * 0.3 + interactionRipple * 0.2;

        rotationSpeed = Math.max(0.1, rotationSpeed + influence * 0.5);
        scaleMultiplier = Math.max(0.5, scaleMultiplier + influence * 0.3);
    }

    /**
     * Adds an interaction ripple effect (e.g., from mouse or touch).
     *
     * @param strength The strength of the interaction event.
     */
    public void addInteractionRipple(double strength) {
        interactionRipple = Math.min(1.0, interactionRipple + strength * 0.01);
        lastInteractionTimestamp = System.currentTimeMillis();
    }

    // === Accessors ===

    public double getBeatIntensity() { return currentBeatIntensity; }
    public double getTempo() { return currentTempo; }
    public double getBeatInfluence() { return beatInfluence; }
    public double getRotationSpeed() { return rotationSpeed; }
    public double getScaleMultiplier() { return scaleMultiplier; }
    public double getColorShift() { return colorShift; }
    public double getComplexity() { return complexity; }
    public double getVisualPhase() { return visualPhase; }

    // === Pattern Generation ===

    /**
     * Generates a pattern value based on polar coordinates and visual state.
     *
     * @param angle Angle in radians.
     * @param radius Distance from center.
     * @param layer Layer index for multilevel effects.
     * @return A normalized pattern value.
     */
    public double getPatternValue(double angle, double radius, int layer) {
        double basePattern = Math.sin(angle * (3 + layer) + visualPhase * rotationSpeed);
        double beatPattern = Math.cos(radius * 0.1 + visualPhase * 2 + beatInfluence * Math.PI);
        double complexityPattern = Math.sin(angle * complexity * 5 + radius * 0.05) * 0.5;

        return (basePattern + beatPattern * currentBeatIntensity + complexityPattern) / 2.5;
    }

    /**
     * Calculates a dynamic radius multiplier for drawing pulsating or rippling elements.
     *
     * @param baseRadius The original radius value.
     * @param layer      The layer index for depth effect.
     * @return A scaled radius value based on visual state.
     */
    public double getRadiusMultiplier(double baseRadius, int layer) {
        double beatPulse = 1.0 + Math.sin(visualPhase * 4 + layer) * beatInfluence * 0.3;
        double ripplePulse = 1.0 + interactionRipple * 0.2;

        return baseRadius * scaleMultiplier * beatPulse * ripplePulse;
    }
}
