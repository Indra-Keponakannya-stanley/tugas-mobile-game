package com.example.slidingpuzzlegame;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GamePerhitunganActivity extends AppCompatActivity {
    private TextView scoreText, timerText, angka1Text, angka2Text;
    private ImageView buah1Image, buah2Image;
    private ImageView jawaban1, jawaban2, jawaban3;
    private TextView angkaJawaban1, angkaJawaban2, angkaJawaban3;
    private ImageButton btnPause;

    private int score = 0;
    private CountDownTimer timer;
    private int hasilBenar;

    private ScoreDatabaseHelper dbHelper;

    private int[] gambarBuah = {
            R.drawable.buah1, R.drawable.buah10
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perhitungan);

        // Inisialisasi Database Helper
        dbHelper = new ScoreDatabaseHelper(this);

        // Inisialisasi semua view
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        angka1Text = findViewById(R.id.angka1Text);
        angka2Text = findViewById(R.id.angka2Text);
        buah1Image = findViewById(R.id.buah1Image);
        buah2Image = findViewById(R.id.buah2Image);
        jawaban1 = findViewById(R.id.jawaban1);
        jawaban2 = findViewById(R.id.jawaban2);
        jawaban3 = findViewById(R.id.jawaban3);
        angkaJawaban1 = findViewById(R.id.angkaJawaban1);
        angkaJawaban2 = findViewById(R.id.angkaJawaban2);
        angkaJawaban3 = findViewById(R.id.angkaJawaban3);
        btnPause = findViewById(R.id.btnPause);

        btnPause.setOnClickListener(v -> {
            if (timer != null) timer.cancel();

            View popupView = LayoutInflater.from(this).inflate(R.layout.popup_pause, null);
            AlertDialog dialog = new AlertDialog.Builder(GamePerhitunganActivity.this)
                    .setView(popupView)
                    .setCancelable(false)
                    .create();

            LinearLayout btnLanjut = popupView.findViewById(R.id.btnLanjut);
            LinearLayout btnUlang = popupView.findViewById(R.id.btnUlang);
            LinearLayout btnHome = popupView.findViewById(R.id.btnHome);

            btnLanjut.setOnClickListener(view -> {
                startTimer();
                dialog.dismiss();
            });

            btnUlang.setOnClickListener(view -> {
                score = 0;
                updateScore();
                generateSoal();
                startTimer();
                dialog.dismiss();
            });

            btnHome.setOnClickListener(view -> {
                simpanScoreKeDatabase(score); // simpan sebelum keluar
                dialog.dismiss();
                finish();
            });

            dialog.show();
        });

        startGame();
    }

    private void startGame() {
        score = 0;
        updateScore();
        startTimer();
        generateSoal();
    }

    private void startTimer() {
        timer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerText.setText("Time: 0");
                Toast.makeText(GamePerhitunganActivity.this, "Waktu Habis!", Toast.LENGTH_SHORT).show();
                simpanScoreKeDatabase(score); // simpan skor saat waktu habis
                finish();
            }
        }.start();
    }

    private void updateScore() {
        scoreText.setText("Score: " + score);
    }

    private void generateSoal() {
        Random rand = new Random();

        int angka1 = rand.nextInt(10);
        int angka2 = rand.nextInt(10);
        hasilBenar = angka1 + angka2;

        angka1Text.setText(String.valueOf(angka1));
        angka2Text.setText(String.valueOf(angka2));

        buah1Image.setImageResource(gambarBuah[rand.nextInt(gambarBuah.length)]);
        buah2Image.setImageResource(gambarBuah[rand.nextInt(gambarBuah.length)]);

        int jawabanBenarPosisi = rand.nextInt(3);
        int[] pilihan = new int[3];

        for (int i = 0; i < 3; i++) {
            if (i == jawabanBenarPosisi) {
                pilihan[i] = hasilBenar;
            } else {
                int salah;
                do {
                    salah = rand.nextInt(19) + 1;
                } while (salah == hasilBenar || contains(pilihan, salah));
                pilihan[i] = salah;
            }
        }

        setJawaban(jawaban1, angkaJawaban1, pilihan[0]);
        setJawaban(jawaban2, angkaJawaban2, pilihan[1]);
        setJawaban(jawaban3, angkaJawaban3, pilihan[2]);
    }

    private boolean contains(int[] arr, int val) {
        for (int j : arr) {
            if (j == val) return true;
        }
        return false;
    }

    private void setJawaban(ImageView imageView, TextView textView, int nilai) {
        imageView.setImageResource(gambarBuah[new Random().nextInt(gambarBuah.length)]);
        textView.setText(String.valueOf(nilai));
        imageView.setOnClickListener(v -> cekJawaban(nilai));
    }

    private void cekJawaban(int jawabanDipilih) {
        if (jawabanDipilih == hasilBenar) {
            score++;
            updateScore();
            generateSoal();
        } else {
            Toast.makeText(this, "Jawaban Salah!", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Simpan skor ke database
    private void simpanScoreKeDatabase(int nilai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("game_name", "Game Perhitungan");
        values.put("score", nilai);
        db.insert("scores", null, values);
        db.close();
        Toast.makeText(this, "Score tersimpan!", Toast.LENGTH_SHORT).show();
    }
}
