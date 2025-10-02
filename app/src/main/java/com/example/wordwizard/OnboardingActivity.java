package com.example.wordwizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    private EditText nameEditText;
    private Button startButton;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "WordWizardPrefs";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_IS_FIRST_TIME = "isFirstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user has already onboarded
        boolean isFirstTime = sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true);
        if (!isFirstTime) {
            // User has already onboarded, go directly to MainActivity
            navigateToMainActivity();
            return;
        }

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        startButton = findViewById(R.id.startButton);

        // Set click listener for start button
        startButton.setOnClickListener(v -> saveNameAndProceed());
    }

    private void saveNameAndProceed() {
        String userName = nameEditText.getText().toString().trim();

        if (userName.isEmpty()) {
            Toast.makeText(this, R.string.name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Save user name and mark as not first time
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putBoolean(KEY_IS_FIRST_TIME, false);
        editor.apply();

        // Navigate to main activity
        navigateToMainActivity();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
