package com.example.wordwizard;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private TextView welcomeTextView, scoreTextView, attemptsTextView, timerTextView, levelTextView, feedbackTextView;
    private EditText guessEditText;
    private Button submitGuessButton, checkLetterButton, wordLengthButton, getHintButton, newGameButton, leaderboardButton;

    // Game State
    private String secretWord = "";
    private int score = 100;
    private int attemptsLeft = 10;
    private int level = 1;
    private long startTime;
    private boolean gameActive = false;
    private boolean hintUsed = false;
    private int wrongGuessCount = 0;

    // Services
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private Handler timerHandler;
    private Runnable timerRunnable;

    private static final String PREFS_NAME = "WordWizardPrefs";
    private static final String KEY_USER_NAME = "userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize services
        apiService = new ApiService();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        timerHandler = new Handler();

        // Initialize UI elements
        initializeViews();

        // Load user name
        loadUserName();

        // Setup timer
        setupTimer();

        // Setup button listeners
        setupButtonListeners();

        // Start new game
        startNewGame();
    }

    private void initializeViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        attemptsTextView = findViewById(R.id.attemptsTextView);
        timerTextView = findViewById(R.id.timerTextView);
        levelTextView = findViewById(R.id.levelTextView);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        guessEditText = findViewById(R.id.guessEditText);
        submitGuessButton = findViewById(R.id.submitGuessButton);
        checkLetterButton = findViewById(R.id.checkLetterButton);
        wordLengthButton = findViewById(R.id.wordLengthButton);
        getHintButton = findViewById(R.id.getHintButton);
        newGameButton = findViewById(R.id.newGameButton);
        leaderboardButton = findViewById(R.id.leaderboardButton);
    }

    private void loadUserName() {
        String userName = sharedPreferences.getString(KEY_USER_NAME, "Player");
        welcomeTextView.setText("Welcome, " + userName + "!");
    }

    private void setupTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameActive) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsedTime / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void setupButtonListeners() {
        submitGuessButton.setOnClickListener(v -> handleGuess());
        checkLetterButton.setOnClickListener(v -> showLetterCheckDialog());
        wordLengthButton.setOnClickListener(v -> handleWordLength());
        getHintButton.setOnClickListener(v -> handleGetHint());
        newGameButton.setOnClickListener(v -> startNewGame());
        leaderboardButton.setOnClickListener(v -> showLeaderboard());
    }

    private void startNewGame() {
        // Reset game state
        score = 100;
        attemptsLeft = 10;
        level = 1;
        hintUsed = false;
        wrongGuessCount = 0;
        gameActive = false;

        // Update UI
        updateUI();
        setFeedback("Loading new word...", Color.BLACK);
        guessEditText.setText("");
        guessEditText.setEnabled(false);
        disableButtons();

        // Get random word from API
        apiService.getRandomWord(new ApiService.ApiCallback<String>() {
            @Override
            public void onSuccess(String word) {
                secretWord = word.toLowerCase();
                gameActive = true;
                startTime = System.currentTimeMillis();
                timerHandler.post(timerRunnable);
                
                setFeedback("Game started! Make your guess.", Color.BLACK);
                guessEditText.setEnabled(true);
                enableButtons();
            }

            @Override
            public void onError(String error) {
                setFeedback("Error loading word: " + error, Color.RED);
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGuess() {
        if (!gameActive) return;

        String guess = guessEditText.getText().toString().trim().toLowerCase();
        
        if (guess.isEmpty()) {
            Toast.makeText(this, R.string.enter_valid_word, Toast.LENGTH_SHORT).show();
            return;
        }

        if (guess.equals(secretWord)) {
            // Correct guess!
            handleCorrectGuess();
        } else {
            // Wrong guess
            handleWrongGuess();
        }

        guessEditText.setText("");
    }

    private void handleCorrectGuess() {
        gameActive = false;
        long elapsedTime = System.currentTimeMillis() - startTime;
        int timeInSeconds = (int) (elapsedTime / 1000);

        setFeedback("🎉 " + getString(R.string.correct_guess), Color.parseColor("#27AE60"));
        
        // Submit to leaderboard
        submitScore(timeInSeconds);

        // Level up
        new Handler().postDelayed(() -> {
            level++;
            updateUI();
            loadNextLevelWord();
        }, 2000);
    }

    private void loadNextLevelWord() {
        setFeedback(getString(R.string.level_up), Color.parseColor("#FEB21A"));
        guessEditText.setEnabled(false);
        disableButtons();
        hintUsed = false;
        wrongGuessCount = 0;

        // Get word with increasing difficulty
        int minLength = 3 + level;
        apiService.getRandomWordWithLength(minLength, new ApiService.ApiCallback<String>() {
            @Override
            public void onSuccess(String word) {
                secretWord = word.toLowerCase();
                score = 100;
                attemptsLeft = 10;
                gameActive = true;
                startTime = System.currentTimeMillis();
                
                updateUI();
                setFeedback("Level " + level + " - New word loaded!", Color.BLACK);
                guessEditText.setEnabled(true);
                enableButtons();
            }

            @Override
            public void onError(String error) {
                setFeedback("Error loading word: " + error, Color.RED);
            }
        });
    }

    private void handleWrongGuess() {
        wrongGuessCount++;
        attemptsLeft--;
        score -= 10;

        if (score < 0) score = 0;

        updateUI();

        if (attemptsLeft <= 0 || score <= 0) {
            // Game over
            gameActive = false;
            setFeedback(getString(R.string.game_over, secretWord), Color.parseColor("#E74C3C"));
            disableButtons();
            
            new Handler().postDelayed(() -> startNewGame(), 3000);
        } else {
            setFeedback("❌ " + getString(R.string.wrong_guess) + " " + attemptsLeft + " attempts left.", 
                       Color.parseColor("#E74C3C"));
        }
    }

    private void showLetterCheckDialog() {
        if (!gameActive) return;
        
        if (score < 5) {
            Toast.makeText(this, R.string.insufficient_score, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.check_letter_title);
        
        final EditText input = new EditText(this);
        input.setHint(R.string.letter_hint);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        builder.setView(input);

        builder.setPositiveButton(R.string.check, (dialog, which) -> {
            String letter = input.getText().toString().toLowerCase();
            
            if (letter.isEmpty() || !Character.isLetter(letter.charAt(0))) {
                Toast.makeText(MainActivity.this, R.string.enter_valid_letter, Toast.LENGTH_SHORT).show();
                return;
            }

            // Count occurrences
            int count = 0;
            for (char c : secretWord.toCharArray()) {
                if (c == letter.charAt(0)) {
                    count++;
                }
            }

            score -= 5;
            if (score < 0) score = 0;
            updateUI();

            String message = getString(R.string.letter_count, letter, count);
            setFeedback(message, Color.parseColor("#FEB21A"));
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void handleWordLength() {
        if (!gameActive) return;
        
        if (score < 5) {
            Toast.makeText(this, R.string.insufficient_score, Toast.LENGTH_SHORT).show();
            return;
        }

        score -= 5;
        if (score < 0) score = 0;
        updateUI();

        String message = getString(R.string.word_length_info, secretWord.length());
        setFeedback(message, Color.parseColor("#FEB21A"));
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void handleGetHint() {
        if (!gameActive) return;
        
        if (wrongGuessCount < 5) {
            Toast.makeText(this, R.string.hint_available, Toast.LENGTH_SHORT).show();
            return;
        }

        if (hintUsed) {
            Toast.makeText(this, "Hint already used for this word!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (score < 5) {
            Toast.makeText(this, R.string.insufficient_score, Toast.LENGTH_SHORT).show();
            return;
        }

        // Deduct points and get hint
        score -= 5;
        if (score < 0) score = 0;
        hintUsed = true;
        updateUI();

        setFeedback("Getting hint...", Color.BLACK);

        apiService.getSynonyms(secretWord, new ApiService.ApiCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> synonyms) {
                if (!synonyms.isEmpty()) {
                    String hint = synonyms.get(0);
                    String message = getString(R.string.hint_info, hint);
                    setFeedback(message, Color.parseColor("#FEB21A"));
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    setFeedback("No hint available for this word.", Color.BLACK);
                }
            }

            @Override
            public void onError(String error) {
                // Fallback: give first and last letter
                String fallbackHint = "Starts with '" + secretWord.charAt(0) + 
                                     "' and ends with '" + secretWord.charAt(secretWord.length() - 1) + "'";
                setFeedback("Hint: " + fallbackHint, Color.parseColor("#FEB21A"));
                Toast.makeText(MainActivity.this, fallbackHint, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitScore(int timeInSeconds) {
        String userName = sharedPreferences.getString(KEY_USER_NAME, "Player");
        // TODO: Implement Dreamlo API integration for leaderboard
        // For now, just save locally
        Toast.makeText(this, "Score: " + score + " | Time: " + timeInSeconds + "s", Toast.LENGTH_LONG).show();
    }

    private void showLeaderboard() {
        // TODO: Implement leaderboard dialog with Dreamlo API
        Toast.makeText(this, "Leaderboard feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        scoreTextView.setText(String.valueOf(score));
        attemptsTextView.setText(String.valueOf(attemptsLeft));
        levelTextView.setText(String.valueOf(level));
    }

    private void setFeedback(String message, int color) {
        feedbackTextView.setText(message);
        feedbackTextView.setTextColor(color);
    }

    private void disableButtons() {
        submitGuessButton.setEnabled(false);
        checkLetterButton.setEnabled(false);
        wordLengthButton.setEnabled(false);
        getHintButton.setEnabled(false);
    }

    private void enableButtons() {
        submitGuessButton.setEnabled(true);
        checkLetterButton.setEnabled(true);
        wordLengthButton.setEnabled(true);
        getHintButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        if (apiService != null) {
            apiService.shutdown();
        }
    }
}
