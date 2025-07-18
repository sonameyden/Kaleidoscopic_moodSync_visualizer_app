// Main.java
package moodsync;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ui.*;
import core.*;

public class Main extends Application {
    private MoodInputPane moodInputPane;
    private AudioPlayerPane audioPlayerPane;
    private KaleidoscopeCanvas kaleidoscopeCanvas;
    private ExportPane exportPane;

    private MoodAnalyzer moodAnalyzer;
    private PlaylistFetcher playlistFetcher;
    private BeatDetector beatDetector;
    private VisualSyncEngine visualSyncEngine;

    @Override
    public void start(Stage primaryStage) {
        // Initialize core components
        moodAnalyzer = new MoodAnalyzer();
        playlistFetcher = new PlaylistFetcher();
        beatDetector = new BeatDetector();
        visualSyncEngine = new VisualSyncEngine();

       

        // Initialize UI components
        moodInputPane = new MoodInputPane(moodAnalyzer, playlistFetcher);
        audioPlayerPane = new AudioPlayerPane(beatDetector, playlistFetcher);
        kaleidoscopeCanvas = new KaleidoscopeCanvas(visualSyncEngine);
        exportPane = new ExportPane(kaleidoscopeCanvas);

        moodInputPane.setOnMoodDetected((mood, playlist) -> {
            audioPlayerPane.loadPlaylist(playlist);
            kaleidoscopeCanvas.setMoodColors(mood);
    
    
            // Change this line:
            // Platform.runLater(() -> audioPlayerPane.handlePlay());
            // To this:
           if (!playlist.isEmpty()) {
                Platform.runLater(() -> audioPlayerPane.handlePlay());
              }
        });

        beatDetector.setOnBeatDetected((tempo, intensity) -> {
            visualSyncEngine.updateBeat(tempo, intensity);
            kaleidoscopeCanvas.updateVisualization();
        });

        // Create main layout
        BorderPane root = new BorderPane();

        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.getChildren().addAll(
            new Label("🎵 MoodSync - Music & Visual Experience"),
            new Separator(),
            moodInputPane,
            new Separator(),
            audioPlayerPane,
            new Separator(),
            exportPane
        );
        leftPanel.setPrefWidth(350);

        root.setLeft(leftPanel);
        root.setCenter(kaleidoscopeCanvas);

        Scene scene = new Scene(root, 1200, 800);
        System.out.println("Resource URL: " + getClass().getResource("/styles.css"));
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("MoodSync - Mood-Based Music Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start visualization loop
        kaleidoscopeCanvas.startAnimation();
    }

    @Override
    public void stop() {
        if (kaleidoscopeCanvas != null) {
            kaleidoscopeCanvas.stopAnimation();
        }
        if (audioPlayerPane != null) {
            audioPlayerPane.cleanup();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
