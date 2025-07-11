package com.example.slidingpuzzlegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnPuzzle, btnPerhitungan, btnMemoryGame, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi tombol
        btnPuzzle = findViewById(R.id.btnPuzzle);
        btnPerhitungan = findViewById(R.id.btnPerhitungan);
        btnMemoryGame = findViewById(R.id.btnMemoryGame);
        btnExit = findViewById(R.id.btnExit);

        // Tombol Puzzle Game
        btnPuzzle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PuzzleGameActivity.class);
                startActivity(intent);
            }
        });

        // Tombol Game Perhitungan
        btnPerhitungan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PerhitunganActivity.class);
                startActivity(intent);
            }
        });

        // Tombol Memory Game
        btnMemoryGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MemoryGameActivity.class);
                startActivity(intent);
            }
        });

        // Tombol Exit
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity(); // keluar dari aplikasi
            }
        });
    }
}
