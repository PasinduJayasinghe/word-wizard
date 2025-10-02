# WordWizard - Word Guessing Game

A comprehensive Android word guessing game built with Java and Android Studio, featuring multiple difficulty levels, scoring system, and various hint mechanisms.

## Features Implemented

### ✅ Core Features (60 marks)
1. **Onboarding (5 marks)** - First-time user name input with SharedPreferences
2. **Guess the Word (25 marks)** - Main guessing mechanic with 100 points and 10 attempts
3. **Letter Occurrence Check (5 marks)** - Check how many times a letter appears (-5 points)
4. **Word Length Query (5 marks)** - Ask for word length (-5 points)
5. **Hint System (5 marks)** - Get synonym/similar word hint after 5th attempt (-5 points)
6. **Timer (5 marks)** - Tracks time taken to guess correctly
7. **Leaderboard (10 marks)** - Framework ready for Dreamlo API integration

### 🎨 UI/UX Design (20 marks)
- Beautiful Material Design interface with custom color scheme
- Intuitive card-based layout
- Clear visual feedback for all actions
- Responsive design for different screen sizes
- Custom drawable resources for buttons and inputs

### 📱 Technical Implementation

#### Color Scheme (As Provided)
- Primary Blue: `#134686`
- Accent Red: `#ED3F27`
- Accent Yellow: `#FEB21A`
- Background Cream: `#FDF4E3`

#### Architecture
```
OnboardingActivity (First Launch)
    ↓
MainActivity (Game Screen)
    - ApiService for network calls
    - SharedPreferences for data persistence
    - Handler-based timer
    - Dialog-based interactions
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/wordwizard/
│   │   ├── OnboardingActivity.java    # First-time user setup
│   │   ├── MainActivity.java          # Main game logic
│   │   └── ApiService.java            # API integration
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_onboarding.xml
│   │   │   └── activity_main.xml
│   │   ├── drawable/
│   │   │   ├── button_primary.xml
│   │   │   ├── button_secondary.xml
│   │   │   ├── edit_text_background.xml
│   │   │   └── card_background.xml
│   │   ├── values/
│   │   │   ├── colors.xml
│   │   │   ├── strings.xml
│   │   │   └── themes.xml
│   │   └── xml/
│   │       └── network_security_config.xml
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 24 (Android 7.0) or higher
- Internet connection for API calls

### Installation Steps

1. **Clone/Open the project in Android Studio**

2. **Sync Gradle files**
   - Click "Sync Now" when prompted
   - Wait for dependencies to download

3. **Configure API Key (Optional for Hint Feature)**
   - Open `ApiService.java`
   - Replace `YOUR_API_KEY` with your API Ninjas key
   - Get free key from: https://api-ninjas.com/

4. **Build and Run**
   - Connect Android device or start emulator
   - Click Run (Shift+F10)
   - Grant internet permissions when prompted

## Game Rules

### Starting the Game
1. Enter your name on first launch
2. Game automatically loads a random word from API
3. You have 100 points and 10 attempts to guess

### Scoring System
- **Start:** 100 points
- **Wrong Guess:** -10 points
- **Letter Check:** -5 points
- **Word Length:** -5 points
- **Hint Request:** -5 points (available after 5 wrong attempts)

### Winning & Progression
- Guess correctly to level up
- Each level increases word difficulty (longer words)
- Score and attempts reset on each level
- Timer tracks your total time

### Game Over
- Occurs when attempts reach 0 or score reaches 0
- Game automatically restarts with a new word after 3 seconds

## API Integration

### Random
