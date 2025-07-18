package core;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PlaylistFetcher {
    private static final String API_URL = "https://api.deezer.com/search";
    private Map<String, String[]> moodToGenres;

    public PlaylistFetcher() {
        initializeMoodGenres();
    }

    private void initializeMoodGenres() {
        moodToGenres = new HashMap<>();
        moodToGenres.put("happy", new String[]{"pop", "dance", "funk", "disco", "upbeat"});
        moodToGenres.put("sad", new String[]{"blues", "ballad", "indie", "folk", "acoustic"});
        moodToGenres.put("angry", new String[]{"rock", "metal", "punk", "hardcore", "alternative"});
        moodToGenres.put("calm", new String[]{"ambient", "chillout", "new age", "classical", "meditation"});
        moodToGenres.put("energetic", new String[]{"electronic", "house", "techno", "drum and bass", "workout"});
        moodToGenres.put("love", new String[]{"r&b", "soul", "romantic", "smooth jazz", "love songs"});
    }

    public List<Map<String, String>> fetchLivePlaylist(String mood) {
        List<Map<String, String>> tracks = new ArrayList<>();
        String[] genres = moodToGenres.getOrDefault(mood.toLowerCase(), new String[]{"pop"});

        try {
            for (String genre : genres) {
                System.out.println("Searching for genre: " + genre);
                List<Map<String, String>> fetchedTracks = getDeezerTracks(genre);
                if (!fetchedTracks.isEmpty()) {
                    tracks.addAll(fetchedTracks);
                }
                if (tracks.size() >= 10) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tracks;
    }

    private List<Map<String, String>> getDeezerTracks(String genre) throws Exception {
        List<Map<String, String>> tracks = new ArrayList<>();
        String query = API_URL + "?q=" + URLEncoder.encode(genre, StandardCharsets.UTF_8.toString());
        URL url = new URL(query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Debug print for raw response
            System.out.println("Deezer API response for genre '" + genre + "':\n" + response + "\n");

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray data = json.getAsJsonArray("data");

            if (data == null || data.size() == 0) {
                System.out.println("No tracks found for genre: " + genre);
                return tracks;
            }

            int limit = Math.min(5, data.size());
            List<Integer> indices = getRandomIndices(data.size(), limit);
            for (int i : indices) {
                JsonObject track = data.get(i).getAsJsonObject();
                Map<String, String> trackInfo = new HashMap<>();

                trackInfo.put("title", track.get("title").getAsString());
                trackInfo.put("artist", track.getAsJsonObject("artist").get("name").getAsString());
                trackInfo.put("url", track.get("link").getAsString());
                trackInfo.put("preview", track.has("preview") ? track.get("preview").getAsString() : "");
                trackInfo.put("album_cover", track.getAsJsonObject("album").get("cover_medium").getAsString());

                tracks.add(trackInfo);
                System.out.println("Fetched: " + trackInfo.get("title") + " by " + trackInfo.get("artist"));
            }
        } else {
            System.out.println("Failed to fetch from API. HTTP status: " + status);
        }

        return tracks;
    }

    private List<Integer> getRandomIndices(int max, int count) {
        List<Integer> indices = new ArrayList<>();
        Random rand = new Random();
        while (indices.size() < count) {
            int index = rand.nextInt(max);
            if (!indices.contains(index)) {
                indices.add(index);
            }
        }
        return indices;
    }

    public String getRepresentativeGenre(String mood) {
        String[] genres = moodToGenres.getOrDefault(mood.toLowerCase(), new String[]{"pop"});
        return genres[0];
    }
}
