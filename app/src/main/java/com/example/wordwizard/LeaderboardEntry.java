package com.example.wordwizard;

public class LeaderboardEntry {
    private String name;
    private int score;
    private int seconds;
    private String text;
    private String date;

    public LeaderboardEntry(String name, int score, int seconds, String text, String date) {
        this.name = name;
        this.score = score;
        this.seconds = seconds;
        this.text = text;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public String getFormattedTime() {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
