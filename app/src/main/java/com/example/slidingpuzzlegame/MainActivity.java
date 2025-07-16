package com.example.slidingpuzzlegame;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnPuzzle, btnPerhitungan, btnMemoryGame, btnExit;
    ImageButton btnVolumeToggle;
    MediaPlayer mediaPlayer;
    boolean isVolumeOn = true;
    private float volume = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi tombol
        btnPuzzle = findViewById(R.id.btnPuzzle);
        btnPerhitungan = findViewById(R.id.btnPerhitungan);
        btnMemoryGame = findViewById(R.id.btnMemoryGame);
        btnExit = findViewById(R.id.btnExit);
        btnVolumeToggle = findViewById(R.id.btnVolumeToggle);

        // Inisialisasi musik
        mediaPlayer = MediaPlayer.create(this, R.raw.bg_musik);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.start();

        // Tombol volume
        btnVolumeToggle.setOnClickListener(v -> {
            if (isVolumeOn) {
                mediaPlayer.setVolume(0f, 0f);
                btnVolumeToggle.setImageResource(R.drawable.volume_off);
            } else {
                mediaPlayer.setVolume(volume, volume);
                btnVolumeToggle.setImageResource(R.drawable.volume_on);
            }
            isVolumeOn = !isVolumeOn;
        });

        // Puzzle Buah
        btnPuzzle.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PuzzleGameActivity.class))
        );

        // Perhitungan
        btnPerhitungan.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this,  GamePerhitunganActivity.class))
        );

        // Memory Game
        btnMemoryGame.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MemoryGameActivity.class))
        );

        // Keluar
        btnExit.setOnClickListener(v -> finishAffinity());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isVolumeOn) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
