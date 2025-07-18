package ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import core.VisualSyncEngine;

public class KaleidoscopeCanvas extends Canvas {
    private VisualSyncEngine syncEngine;
    private GraphicsContext gc;
    private AnimationTimer animationTimer;

    private double centerX, centerY;
    private double mouseX = 0, mouseY = 0;
    private double animationPhase = 0;
    private double magneticSpin = 0;
    private boolean isAnimating = false;

    private Color[] currentColors;
    private Color[] targetColors;
    private Color[] accentColors;
    private double[] colorPhases;

    public KaleidoscopeCanvas(VisualSyncEngine syncEngine) {
        super(800, 600);
        this.syncEngine = syncEngine;
        this.gc = getGraphicsContext2D();

        setupMouseHandlers();
        setMoodColors("neutral");

        this.centerX = getWidth() / 2;
        this.centerY = getHeight() / 2;
        this.colorPhases = new double[6];
    }

    private void setupMouseHandlers() {
        setOnMouseMoved(this::handleMouseMove);
        setOnMouseClicked(this::handleMouseClick);
    }

    private void handleMouseMove(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();

        // Magnetic effect
        double distanceFromCenter = Math.hypot(e.getX() - centerX, e.getY() - centerY);
        double magneticForce = Math.max(0, 1.0 - distanceFromCenter / 300.0);
        magneticSpin += magneticForce * 0.02;
    }

    private void handleMouseClick(MouseEvent e) {
        double distanceFromCenter = Math.hypot(e.getX() - centerX, e.getY() - centerY);
        syncEngine.addInteractionRipple(distanceFromCenter);

        // Burst of magnetic energy
        magneticSpin += 0.5;
    }

    /**
     * Adjusted palettes:
     * - "sad" is deeper/bluer
     * - "calm" uses gray/silver/off-white
     */
    public void setMoodColors(String mood) {
        switch (mood.toLowerCase()) {
            case "happy":
            case "excited":
                targetColors = new Color[]{
                    Color.GOLD, Color.ORANGE, Color.LIGHTCORAL,
                    Color.LIGHTGREEN, Color.YELLOW, Color.PINK
                };
                accentColors = new Color[]{
                    Color.LIGHTYELLOW, Color.PEACHPUFF, Color.LIGHTPINK,
                    Color.PALEGREEN, Color.KHAKI, Color.LAVENDERBLUSH
                };
                break;

            case "sad":
            case "melancholy":
                targetColors = new Color[]{
                    Color.DARKBLUE, Color.ROYALBLUE, Color.MIDNIGHTBLUE,
                    Color.CADETBLUE, Color.DODGERBLUE, Color.STEELBLUE
                };
                accentColors = new Color[]{
                    Color.LIGHTSKYBLUE, Color.LIGHTSTEELBLUE,
                    Color.ALICEBLUE, Color.LIGHTCYAN,
                    Color.POWDERBLUE, Color.AZURE
                };
                break;

            case "angry":
            case "intense":
                targetColors = new Color[]{
                    Color.CRIMSON, Color.DARKRED, Color.ORANGERED,
                    Color.GOLD, Color.FIREBRICK, Color.TOMATO
                };
                accentColors = new Color[]{
                    Color.MISTYROSE, Color.LIGHTSALMON, Color.PEACHPUFF,
                    Color.PAPAYAWHIP, Color.LIGHTCORAL, Color.WHEAT
                };
                break;

            case "calm":
            case "peaceful":
                targetColors = new Color[]{
                    Color.GAINSBORO, Color.SILVER, Color.LIGHTGRAY,
                    Color.LIGHTSLATEGRAY, Color.WHITESMOKE, Color.MINTCREAM
                };
                accentColors = new Color[]{
                    Color.WHITESMOKE, Color.LIGHTGRAY, Color.SNOW,
                    Color.LINEN, Color.LIGHTSTEELBLUE, Color.ALICEBLUE
                };
                break;

            case "energetic":
                targetColors = new Color[]{
                    Color.LIME, Color.CYAN, Color.MAGENTA,
                    Color.YELLOW, Color.SPRINGGREEN, Color.HOTPINK
                };
                accentColors = new Color[]{
                    Color.LIGHTGREEN, Color.LIGHTCYAN, Color.PLUM,
                    Color.LIGHTYELLOW, Color.PALEGREEN, Color.LIGHTPINK
                };
                break;

            case "romantic":
                targetColors = new Color[]{
                    Color.DEEPPINK, Color.MEDIUMVIOLETRED, Color.ORCHID,
                    Color.PLUM, Color.THISTLE, Color.HOTPINK
                };
                accentColors = new Color[]{
                    Color.LAVENDERBLUSH, Color.MISTYROSE, Color.SEASHELL,
                    Color.LINEN, Color.OLDLACE, Color.ANTIQUEWHITE
                };
                break;

            default:
                targetColors = new Color[]{
                    Color.MEDIUMPURPLE, Color.MEDIUMSLATEBLUE, Color.MEDIUMSEAGREEN,
                    Color.DARKORANGE, Color.INDIANRED, Color.DARKTURQUOISE
                };
                accentColors = new Color[]{
                    Color.THISTLE, Color.LIGHTSTEELBLUE, Color.LIGHTSEAGREEN,
                    Color.PEACHPUFF, Color.LIGHTCORAL, Color.PALETURQUOISE
                };
        }

        if (currentColors == null) {
            currentColors = targetColors.clone();
        }
    }

    public void startAnimation() {
        if (isAnimating) return;

        isAnimating = true;
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
            }
        };
        animationTimer.start();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            isAnimating = false;
        }
    }

    private void update() {
        double tempo = syncEngine.getTempo();
        double tempoFactor = 0.6 + 0.4 * Math.tanh((tempo - 90) / 60.0);

        // Advance phase in small increments
        animationPhase += 0.015 * tempoFactor;

        // Wrap animationPhase so it “loops” every 2π
        animationPhase %= (2 * Math.PI);

        // Smooth magnetic spin decay
        magneticSpin *= 0.98;

        // Update each color phase
        for (int i = 0; i < colorPhases.length; i++) {
            colorPhases[i] += 0.01 * (1 + i * 0.1) * tempoFactor;
            colorPhases[i] %= (2 * Math.PI);
        }

        double dist = Math.hypot(mouseX - centerX, mouseY - centerY);
        double mouseInfluence = dist / 200.0;
        syncEngine.update(animationPhase, mouseInfluence);

        interpolateColors();
    }

    private void interpolateColors() {
        double speed = 0.03;
        for (int i = 0; i < currentColors.length; i++) {
            Color from = currentColors[i];
            Color to = targetColors[i];

            double r = from.getRed() + (to.getRed() - from.getRed()) * speed;
            double g = from.getGreen() + (to.getGreen() - from.getGreen()) * speed;
            double b = from.getBlue() + (to.getBlue() - from.getBlue()) * speed;
            double a = from.getOpacity() + (to.getOpacity() - from.getOpacity()) * speed;

            currentColors[i] = new Color(r, g, b, a);
        }
    }

    /**
     * Draws a multi-stop radial “radiating” background,
     * darkening the outermost rings so shapes remain more legible.
     */
    private void drawBackground(double beatIntensity) {
        Color centerColor = currentColors[0];
        Color midColor    = accentColors[0 % accentColors.length];

        Stop[] bgStops = new Stop[]{
            new Stop(0, centerColor.deriveColor(0, 1, 1, 1.0)),
            new Stop(0.4, Color.color(
                midColor.getRed(), midColor.getGreen(), midColor.getBlue(), 0.7
            )),
            new Stop(0.8, Color.color(
                centerColor.getRed() * 0.05, centerColor.getGreen() * 0.05,
                centerColor.getBlue() * 0.05, 1.0
            )),
            new Stop(1, Color.color(
                centerColor.getRed() * 0.01, centerColor.getGreen() * 0.01,
                centerColor.getBlue() * 0.01, 1.0
            ))
        };

        RadialGradient bgGradient = new RadialGradient(
            0, 0,
            centerX, centerY,
            Math.max(getWidth(), getHeight()) * 0.9,
            false, CycleMethod.NO_CYCLE,
            bgStops
        );

        gc.setFill(bgGradient);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void draw() {
        double beatIntensity = syncEngine.getBeatIntensity();

        // 1) Radiating background
        drawBackground(beatIntensity);

        // 2) Kaleidoscope shapes
        drawKaleidoscope();
        drawOuterRings(beatIntensity);
    }

    private void drawKaleidoscope() {
        double beatIntensity = syncEngine.getBeatIntensity();
        double tempo = syncEngine.getTempo();

        int segments = 12;
        double segmentAngle = 2 * Math.PI / segments;

        // Add a very subtle background rotation (optional)
        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(Math.toDegrees(magneticSpin + animationPhase * 0.3));

        for (int i = 0; i < segments; i++) {
            gc.save();
            gc.rotate(Math.toDegrees(i * segmentAngle));
            drawSegmentPattern(beatIntensity, tempo, i);
            gc.restore();
        }

        gc.restore();
        drawCentralGlow(beatIntensity);
    }

    private void drawSegmentPattern(double beatIntensity, double tempo, int segmentIndex) {
        // Keep shapes large: baseRadius = 150 + beatIntensity*100
        double baseRadius = 150 + beatIntensity * 100;
        double radiusVariation = tempo / 120.0;

        for (int layer = 0; layer < 7; layer++) {
            double layerRadius = baseRadius * (0.4 + layer * 0.15) * radiusVariation;
            double layerPhase = colorPhases[layer % colorPhases.length];

            Color primaryColor = currentColors[layer % currentColors.length];
            Color accentColor = accentColors[layer % accentColors.length];

            // Increase alpha so shape is bolder against background:
            double alpha = Math.min(1.0, Math.max(0.25, 0.8 - layer * 0.08 + beatIntensity * 0.4));

            drawFlowingShape(
                layerRadius, layerPhase, beatIntensity,
                segmentIndex, layer,
                primaryColor, accentColor, alpha
            );
        }
    }

    private void drawFlowingShape(
        double radius, double phase, double beatIntensity,
        int variation, int layer,
        Color primaryColor, Color accentColor, double alpha
    ) {
        int points = 8 + (variation % 3);
        int totalPoints = points * 3;

        double[] xPts = new double[totalPoints];
        double[] yPts = new double[totalPoints];

        for (int i = 0; i < totalPoints; i++) {
            double angle = (2 * Math.PI * i / totalPoints) + phase;
            double pattern = syncEngine.getPatternValue(angle, radius, layer);

            double wave1 = Math.sin(angle * 3 + phase * 2) * 0.3;
            double wave2 = Math.cos(angle * 5 + phase * 1.5) * 0.2;
            double wave3 = Math.sin(angle * 7 + phase * 0.8) * 0.15;

            double warpedRadius = radius * (0.7 + 0.5 * pattern + wave1 + wave2 + wave3);
            double pulse = Math.sin(angle * points + phase) * 20 * beatIntensity;
            double finalR = warpedRadius + pulse;

            xPts[i] = finalR * Math.cos(angle);
            yPts[i] = finalR * Math.sin(angle);
        }

        // Bold radial gradient: full alpha at center, still somewhat opaque at edges
        RadialGradient shapeGradient = new RadialGradient(
            0, 0, 0, 0, radius * 1.2,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(
                primaryColor.getRed(),
                primaryColor.getGreen(),
                primaryColor.getBlue(),
                alpha
            )),
            new Stop(0.6, Color.color(
                accentColor.getRed(),
                accentColor.getGreen(),
                accentColor.getBlue(),
                alpha * 0.85
            )),
            new Stop(1, Color.color(
                primaryColor.getRed(),
                primaryColor.getGreen(),
                primaryColor.getBlue(),
                alpha * 0.5
            ))
        );

        gc.setFill(shapeGradient);
        gc.beginPath();
        gc.moveTo(xPts[0], yPts[0]);

        for (int i = 1; i < totalPoints; i++) {
            int prev = (i - 1 + totalPoints) % totalPoints;
            int next = (i + 1) % totalPoints;

            double cx1 = xPts[prev] + (xPts[i] - xPts[prev]) * 0.5;
            double cy1 = yPts[prev] + (yPts[i] - yPts[prev]) * 0.5;
            double cx2 = xPts[i] + (xPts[next] - xPts[i]) * 0.3;
            double cy2 = yPts[i] + (yPts[next] - yPts[i]) * 0.3;

            gc.bezierCurveTo(cx1, cy1, cx2, cy2, xPts[i], yPts[i]);
        }

        int last = totalPoints - 1;
        double cx1 = xPts[last] + (xPts[0] - xPts[last]) * 0.5;
        double cy1 = yPts[last] + (yPts[0] - yPts[last]) * 0.5;
        double cx2 = xPts[0] + (xPts[1] - xPts[0]) * 0.3;
        double cy2 = yPts[0] + (yPts[1] - yPts[0]) * 0.3;

        gc.bezierCurveTo(cx1, cy1, cx2, cy2, xPts[0], yPts[0]);
        gc.closePath();
        gc.fill();

        // Add a subtle white edge‐glow stroke to reinforce contrast
        gc.setStroke(Color.color(1, 1, 1, alpha * 0.3));
        gc.setLineWidth(1.5 + beatIntensity * 1.5);
        gc.stroke();
    }

    private void drawOuterRings(double beatIntensity) {
        gc.save();
        gc.translate(centerX, centerY);

        for (int ring = 0; ring < 3; ring++) {
            double ringRadius = 350 + ring * 50 + beatIntensity * 20;
            double ringPhase = animationPhase * (0.3 + ring * 0.1);

            Color ringColor = currentColors[ring % currentColors.length];
            double alpha = (0.3 - ring * 0.08) * beatIntensity;

            gc.setStroke(Color.color(
                ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), alpha
            ));
            gc.setLineWidth(2 + beatIntensity * 3);

            gc.beginPath();
            for (int i = 0; i <= 360; i += 2) {
                double angle = Math.toRadians(i) + ringPhase;
                double wave = Math.sin(angle * 8 + ringPhase * 3) * 15 * beatIntensity;
                double r = ringRadius + wave;

                double x = r * Math.cos(angle);
                double y = r * Math.sin(angle);

                if (i == 0) {
                    gc.moveTo(x, y);
                } else {
                    gc.lineTo(x, y);
                }
            }
            gc.closePath();
            gc.stroke();
        }

        gc.restore();
    }

    private void drawCentralGlow(double beatIntensity) {
        double glowRadius = 60 + beatIntensity * 60;

        for (int i = 0; i < 15; i++) {
            double layerAlpha = (1.0 - i / 15.0) * 0.4 * (0.5 + beatIntensity);
            Color baseGlow = currentColors[0];
            Color glowColor = Color.color(
                Math.min(1.0, baseGlow.getRed() + 0.3),
                Math.min(1.0, baseGlow.getGreen() + 0.3),
                Math.min(1.0, baseGlow.getBlue() + 0.3),
                layerAlpha
            );

            gc.setFill(glowColor);
            double currentRadius = glowRadius * (1 + i * 0.08);
            gc.fillOval(
                centerX - currentRadius / 2,
                centerY - currentRadius / 2,
                currentRadius,
                currentRadius
            );
        }

        RadialGradient coreGradient = new RadialGradient(
            0, 0, centerX, centerY, 25,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(1, 1, 1, 0.8 * beatIntensity)),
            new Stop(1, Color.color(
                currentColors[0].getRed(),
                currentColors[0].getGreen(),
                currentColors[0].getBlue(),
                0.3
            ))
        );

        gc.setFill(coreGradient);
        gc.fillOval(centerX - 25, centerY - 25, 50, 50);
    }

    public void updateVisualization() {
        if (isAnimating) {
            update();
            draw();
        }
    }
}