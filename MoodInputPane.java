package ui;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import core.MoodAnalyzer;
import core.PlaylistFetcher;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MoodInputPane extends VBox {
    private final MoodAnalyzer moodAnalyzer;
    private final PlaylistFetcher playlistFetcher;
    private BiConsumer<String, List<Map<String, String>>> onMoodDetected;

    private TextArea moodTextArea;
    private Label detectedMoodLabel;
    private Button analyzeButton;

    public MoodInputPane(MoodAnalyzer analyzer, PlaylistFetcher fetcher) {
        this.moodAnalyzer = analyzer;
        this.playlistFetcher = fetcher;
        initializeUI();
    }

    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));

        Label title = new Label("🎭 How are you feeling?");
        title.setFont(Font.font(16));
        title.setStyle("-fx-font-weight: bold;");

        Label textLabel = new Label("Describe your mood:");
        moodTextArea = new TextArea();
        moodTextArea.setPrefRowCount(4);
        moodTextArea.setPromptText("E.g., I'm feeling anxious and overwhelmed...");

        analyzeButton = new Button("🎵 Generate Playlist");
        analyzeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        analyzeButton.setOnAction(e -> analyzeMood());

        detectedMoodLabel = new Label("Detected mood: None");
        detectedMoodLabel.setStyle("-fx-font-style: italic;");

        getChildren().addAll(
            title,
            new Separator(),
            textLabel,
            moodTextArea,
            analyzeButton,
            detectedMoodLabel
        );
    }

     private void analyzeMood() {
        String textMood = moodTextArea.getText().trim();

        if (textMood.isEmpty()) {
            detectedMoodLabel.setText("Please enter a description of your mood.");
            return;
        }

        String detectedMood = moodAnalyzer.predictMood(textMood);
        detectedMoodLabel.setText("Detected mood: " + detectedMood);

        List<Map<String, String>> playlist = playlistFetcher.fetchLivePlaylist(detectedMood);

        if (onMoodDetected != null) {
            onMoodDetected.accept(detectedMood, playlist); // Pass the list directly
        }
    }
    
    public void setOnMoodDetected(BiConsumer<String, List<Map<String, String>>> callback) {
        this.onMoodDetected = callback;
    }
   
}
