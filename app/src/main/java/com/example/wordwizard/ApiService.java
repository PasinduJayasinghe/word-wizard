package com.example.wordwizard;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiService {

    private static final String RANDOM_WORD_API = "https://random-word-api.herokuapp.com/word";
    private static final String THESAURUS_API = "https://api.api-ninjas.com/v1/thesaurus?word=";
    private static final String API_NINJAS_KEY = "YOUR_API_KEY"; // User should add their key
    
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public ApiService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Interface for callbacks
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // Get random word
    public void getRandomWord(final ApiCallback<String> callback) {
        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                        .url(RANDOM_WORD_API)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<String>>(){}.getType();
                    List<String> words = gson.fromJson(responseBody, listType);
                    
                    if (words != null && !words.isEmpty()) {
                        String word = words.get(0);
                        mainHandler.post(() -> callback.onSuccess(word));
                    } else {
                        mainHandler.post(() -> callback.onError("No word returned"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("API request failed"));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    // Get random word with minimum length (for difficulty levels)
    public void getRandomWordWithLength(int minLength, final ApiCallback<String> callback) {
        getRandomWord(new ApiCallback<String>() {
            @Override
            public void onSuccess(String word) {
                if (word.length() >= minLength) {
                    callback.onSuccess(word);
                } else {
                    // Try again if word is too short
                    getRandomWordWithLength(minLength, callback);
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // Get synonyms for hints (using thesaurus)
    public void getSynonyms(String word, final ApiCallback<List<String>> callback) {
        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                        .url(THESAURUS_API + word)
                        .addHeader("X-Api-Key", API_NINJAS_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    ThesaurusResponse thesaurusResponse = gson.fromJson(responseBody, ThesaurusResponse.class);
                    
                    if (thesaurusResponse != null && thesaurusResponse.synonyms != null && 
                        !thesaurusResponse.synonyms.isEmpty()) {
                        mainHandler.post(() -> callback.onSuccess(thesaurusResponse.synonyms));
                    } else {
                        mainHandler.post(() -> callback.onError("No synonyms found"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Thesaurus API request failed"));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    // Helper class for thesaurus response
    private static class ThesaurusResponse {
        List<String> synonyms;
        List<String> antonyms;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
