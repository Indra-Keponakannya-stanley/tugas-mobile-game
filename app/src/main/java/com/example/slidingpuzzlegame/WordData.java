package com.example.slidingpuzzlegame;


import java.util.ArrayList;
import java.util.List;

public class WordData {

    public static List<WordItem> getAllWords() {
        List<WordItem> words = new ArrayList<>();
        words.add(new WordItem("JERUK", R.drawable.jeruk));
        words.add(new WordItem("PISANG", R.drawable.pisang));
        words.add(new WordItem("NAGA", R.drawable.buah_naga));
        words.add(new WordItem("MANGGA", R.drawable.mangga));
        words.add(new WordItem("NANAS", R.drawable.nanas));
        words.add(new WordItem("SALAK", R.drawable.salak));
        words.add(new WordItem("SEMANGKA", R.drawable.semangka));

        // Tambahkan buah lainnya di sini
        return words;
    }
}