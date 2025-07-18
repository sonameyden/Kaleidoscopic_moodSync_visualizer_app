package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import core.BeatDetector;
import core.PlaylistFetcher;

import java.util.List;
import java.util.Map;

public class AudioPlayerPane extends VBox {
    private MediaPlayer mediaPlayer;
    private List<Map<String, String>> currentPlaylist;
    private int currentIndex = 0;

    private final BeatDetector beatDetector;
    private final PlaylistFetcher playlistFetcher;

    // UI Controls
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Slider volumeSlider;
    private ProgressBar progressBar;
    private ListView<String> playlistView;
    private ImageView albumCoverView;
    private Label nowPlayingLabel;

    private ScrollPane scrollPane;
    private VBox contentBox;

    public AudioPlayerPane(BeatDetector detector, PlaylistFetcher fetcher) {
        this.beatDetector = detector;
        this.playlistFetcher = fetcher;

        initializeUI();
        setupMediaHandlers();
    }

    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));

        contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.getStyleClass().add("player-content");

        nowPlayingLabel = new Label("Now Playing: -");

        playButton = new Button("Play");
        pauseButton = new Button("Pause");
        nextButton = new Button("Next");

        volumeSlider = new Slider(0, 100, 50);
        progressBar = new ProgressBar(0);

        playlistView = new ListView<>();
        albumCoverView = new ImageView();
        albumCoverView.setFitWidth(150);
        albumCoverView.setPreserveRatio(true);

        pauseButton.setDisable(true);

        contentBox.getChildren().addAll(
            nowPlayingLabel,
            playButton, pauseButton, nextButton,
            new Label("Volume"), volumeSlider,
            new Label("Progress"), progressBar,
            albumCoverView,
            new Label("Playlist"), playlistView
        );

        scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(new Insets(5));

        getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void setupMediaHandlers() {
        playButton.setOnAction(e -> handlePlay());
        pauseButton.setOnAction(e -> handlePause());
        nextButton.setOnAction(e -> handleNext());

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });
    }

    public void loadPlaylist(List<Map<String, String>> newPlaylist) {
        this.currentPlaylist = newPlaylist;
        this.currentIndex = 0;

        playlistView.getItems().clear();
        if (currentPlaylist != null) {
            for (Map<String, String> track : currentPlaylist) {
                String title = track.getOrDefault("title", "Unknown Title");
                String artist = track.getOrDefault("artist", "Unknown Artist");
                playlistView.getItems().add(title + " - " + artist);
            }
        }

        if (!playlistView.getItems().isEmpty()) {
            playlistView.getSelectionModel().select(currentIndex);
        }
    }

    private void resetMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void updateAlbumCover(String url) {
        Platform.runLater(() -> {
            if (url != null && !url.isEmpty()) {
                albumCoverView.setImage(new Image(url));
            } else {
                albumCoverView.setImage(null);
            }
        });
    }

    private void updateNowPlayingLabel(String title, String artist) {
        Platform.runLater(() -> nowPlayingLabel.setText("Now Playing: " + title + " - " + artist));
    }

    public void handlePlay() {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) return;

        resetMediaPlayer();

        Map<String, String> track = currentPlaylist.get(currentIndex);
        String previewUrl = track.get("preview");
        if (previewUrl == null || previewUrl.isEmpty()) {
            System.out.println("No preview URL. Skipping...");
            handleNext();
            return;
        }

        updateAlbumCover(track.get("album_cover"));
        updateNowPlayingLabel(
            track.getOrDefault("title", "Unknown Title"),
            track.getOrDefault("artist", "Unknown Artist")
        );

        try {
            Media media = new Media(previewUrl);
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                playButton.setDisable(true);
                pauseButton.setDisable(false);
                setupProgressUpdates();
                updateBeatDetection();
            });

            mediaPlayer.setOnEndOfMedia(this::handleNext);

            mediaPlayer.setOnError(() -> {
                System.err.println("Media error: " + mediaPlayer.getError());
                handleNext();
            });

        } catch (Exception e) {
            System.err.println("Error playing track: " + e.getMessage());
            handleNext();
        }

        playlistView.getSelectionModel().select(currentIndex);
    }

    private void setupProgressUpdates() {
        if (mediaPlayer == null) return;

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (mediaPlayer.getTotalDuration() != null && mediaPlayer.getTotalDuration().toMillis() > 0) {
                double progress = newTime.toMillis() / mediaPlayer.getTotalDuration().toMillis();
                Platform.runLater(() -> progressBar.setProgress(progress));
            }
        });
    }

    private void updateBeatDetection() {
        if (mediaPlayer != null && beatDetector != null) {
            mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
                beatDetector.analyzeAudio(magnitudes);
            });
        }
    }

    public void handlePause() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
        } else {
            mediaPlayer.play();
            playButton.setDisable(true);
            pauseButton.setDisable(false);
        }
    }

    public void handleNext() {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) return;

        currentIndex = (currentIndex + 1) % currentPlaylist.size();
        playlistView.getSelectionModel().select(currentIndex);
        handlePlay();
    }

    public void cleanup() {
        resetMediaPlayer();
    }
}
