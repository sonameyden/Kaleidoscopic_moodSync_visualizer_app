package ui;
 // if needed

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.Imagesaver;
import java.io.File;

public class ExportPane extends VBox {
    private KaleidoscopeCanvas canvas;
    private Button screenshotButton;
    private Button shareButton;
    private Button zenModeButton;
    private CheckBox autoSaveCheckBox;
    private Label statusLabel;
    
    private boolean zenModeActive = false;

    public ExportPane(KaleidoscopeCanvas canvas) {
        this.canvas = canvas;
        initializeUI();
    }
    
    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        
        Label title = new Label("💾 Export & Share");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        screenshotButton = new Button("📸 Save Screenshot");
        screenshotButton.setOnAction(e -> saveScreenshot());
        screenshotButton.setPrefWidth(200);
        
        shareButton = new Button("🌐 Share (Mock)");
        shareButton.setOnAction(e -> simulateShare());
        shareButton.setPrefWidth(200);
        
        zenModeButton = new Button("🧘 Zen Mode");
        zenModeButton.setOnAction(e -> toggleZenMode());
        zenModeButton.setPrefWidth(200);
        
        autoSaveCheckBox = new CheckBox("Auto-save every 30s");
        autoSaveCheckBox.setOnAction(e -> toggleAutoSave());
        
        statusLabel = new Label("Ready to export");
        statusLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        
        getChildren().addAll(
            title,
            screenshotButton,
            shareButton,
            zenModeButton,
            autoSaveCheckBox,
            statusLabel
        );
    }
    
    private void saveScreenshot() {
        try {
            WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Screenshot");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png")
            );
            fileChooser.setInitialFileName("moodsync_" + System.currentTimeMillis() + ".png");
            
            Stage stage = (Stage) getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                Imagesaver.saveImage(SwingFXUtils.fromFXImage(image, null), file);
                statusLabel.setText("Screenshot saved: " + file.getName());
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error saving screenshot: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void simulateShare() {
        // Mock social sharing
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Share Your Creation");
        alert.setHeaderText("Ready to share your mood visualization!");
        alert.setContentText("Your beautiful kaleidoscope visualization would be shared to:\n\n" +
                           "• Instagram Stories\n" +
                           "• Twitter\n" +
                           "• Facebook\n" +
                           "• TikTok\n\n" +
                           "(This is a mock interface - actual sharing would require API integration)");
        alert.showAndWait();
        
        statusLabel.setText("Share completed (mock)");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }
    
    private void toggleZenMode() {
        zenModeActive = !zenModeActive;
        
        if (zenModeActive) {
            zenModeButton.setText("🚪 Exit Zen Mode");
            zenModeButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white;");
            statusLabel.setText("Zen mode activated - enjoy the peaceful visuals");
            statusLabel.setStyle("-fx-text-fill: purple;");
            
            // In a full implementation, this would:
            // - Hide UI elements
            // - Play ambient sounds
            // - Use softer colors
            // - Slower, more meditative patterns
            canvas.setMoodColors("peaceful");
            
        } else {
            zenModeButton.setText("🧘 Zen Mode");
            zenModeButton.setStyle("");
            statusLabel.setText("Zen mode deactivated");
            statusLabel.setStyle("-fx-text-fill: gray;");
        }
    }
    
    private void toggleAutoSave() {
        if (autoSaveCheckBox.isSelected()) {
            statusLabel.setText("Auto-save enabled");
            // In a full implementation, start auto-save timer
        } else {
            statusLabel.setText("Auto-save disabled");
            // Stop auto-save timer
        }
    }
}