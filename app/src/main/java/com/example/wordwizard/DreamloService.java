package com.example.wordwizard;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DreamloService {

    private static final String PRIVATE_CODE = "oBrzOc0jok6EaHxhCjWWoAYj2ICAN4zEioOoXMRSBfMw";
    private static final String PUBLIC_CODE = "68dee2538f40bb08d0afa554";
    private static final String BASE_URL = "http://dreamlo.com/lb/";

    private final OkHttpClient client;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public DreamloService() {
        this.client = new OkHttpClient();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface DreamloCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    /**
     * Submit a score to the leaderboard
     * @param playerName Player's name (no spaces or special characters)
     * @param score Player's score
     * @param seconds Time taken in seconds
     * @param level Current level reached
     */
    public void submitScore(String playerName, int score, int seconds, int level, DreamloCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // Clean player name (remove spaces and special chars, Dreamlo doesn't allow them)
                String cleanName = playerName.replaceAll("[^a-zA-Z0-9]", "");
                if (cleanName.isEmpty()) {
                    cleanName = "Player";
                }

                // Format: /add/NAME/SCORE/SECONDS/TEXT
                String text = "Level" + level;
                String url = BASE_URL + PRIVATE_CODE + "/add/" + cleanName + "/" + score + "/" + seconds + "/" + text;

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to submit score"));
                }
                response.close();
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Get top scores from leaderboard
     * @param limit Number of top scores to retrieve
     */
    public void getTopScores(int limit, DreamloCallback<List<LeaderboardEntry>> callback) {
        executorService.execute(() -> {
            try {
                // Using pipe format for easy parsing
                String url = BASE_URL + PUBLIC_CODE + "/pipe/" + limit;

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    List<LeaderboardEntry> entries = parsePipeFormat(responseBody);
                    mainHandler.post(() -> callback.onSuccess(entries));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to fetch leaderboard"));
                }
                response.close();
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Parse Dreamlo pipe-delimited format
     * Format: name|score|seconds|text|date
     */
    private List<LeaderboardEntry> parsePipeFormat(String data) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        if (data == null || data.trim().isEmpty()) {
            return entries;
        }

        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                try {
                    String name = parts.length > 0 ? parts[0] : "Unknown";
                    int score = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                    int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                    String text = parts.length > 3 ? parts[3] : "";
                    String date = parts.length > 4 ? parts[4] : "";

                    entries.add(new LeaderboardEntry(name, score, seconds, text, date));
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }

        return entries;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
