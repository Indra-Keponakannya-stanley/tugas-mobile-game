package com.example.slidingpuzzlegame;

public class WordItem {
    private String word;
    private int imageResId;

    public WordItem(String word, int imageResId) {
        this.word = word;
        this.imageResId = imageResId;
    }

    public String getWord() {
        return word;
    }

    public int getImageResId() {
        return imageResId;
    }
}