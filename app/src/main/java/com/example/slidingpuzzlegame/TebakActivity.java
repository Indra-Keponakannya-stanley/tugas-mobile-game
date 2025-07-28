package com.example.slidingpuzzlegame;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class TebakActivity extends AppCompatActivity {

    private ImageView imageViewQuestion;
    private LinearLayout layoutAnswerSlots, layoutLetters;
    private Button btnCheck, btnNext;
    private TextView tvScore, tvTimer;
    private ImageButton btnPause;

    private WordItem currentItem;
    private String currentWord;
    private List<TextView> answerSlots = new ArrayList<>();
    private List<TextView> letterViews = new ArrayList<>();
    private int clueIndex = -1;
    private boolean answeredCorrectly = false;
    private int score = 0;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 60000;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tebak);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imageViewQuestion = findViewById(R.id.imageViewQuestion);
        layoutAnswerSlots = findViewById(R.id.layoutAnswerSlots);
        layoutLetters = findViewById(R.id.layoutLetters);
        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        btnPause = findViewById(R.id.btnPause);

        btnCheck.setOnClickListener(view -> checkAnswer());
        btnNext.setOnClickListener(view -> {
            answeredCorrectly = false;
            setupGame();
        });
        btnPause.setOnClickListener(view -> pauseGame());

        updateScoreDisplay();
        setupGame();
    }

    private void setupGame() {
        List<WordItem> words = WordData.getAllWords();
        currentItem = words.get(new Random().nextInt(words.size()));
        currentWord = currentItem.getWord();

        imageViewQuestion.setImageResource(currentItem.getImageResId());

        layoutAnswerSlots.removeAllViews();
        layoutLetters.removeAllViews();
        answerSlots.clear();
        letterViews.clear();

        clueIndex = new Random().nextInt(currentWord.length());
        char clueChar = currentWord.charAt(clueIndex);

        for (int i = 0; i < currentWord.length(); i++) {
            TextView slot = createLetterSlot(i == clueIndex ? String.valueOf(clueChar) : "_");
            if (i == clueIndex) slot.setTag("clue");
            layoutAnswerSlots.addView(slot);
            answerSlots.add(slot);
        }

        List<Character> shuffledLetters = new ArrayList<>();
        for (char c : currentWord.toCharArray()) shuffledLetters.add(c);
        Collections.shuffle(shuffledLetters);

        boolean clueUsed = false;
        for (char c : shuffledLetters) {
            TextView letter = createLetterSlot(String.valueOf(c));
            if (!clueUsed && c == clueChar) {
                clueUsed = true;
                letter.setVisibility(View.GONE);
            }
            letter.setOnClickListener(v -> fillNextSlot(c, letter));
            layoutLetters.addView(letter);
            letterViews.add(letter);
        }

        btnNext.setVisibility(View.GONE);
        resetTimer();
        startTimer();
    }

    private TextView createLetterSlot(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(24);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(8, 8, 8, 8);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(Color.TRANSPARENT);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(12, 12, 12, 12);
        tv.setLayoutParams(params);
        return tv;
    }

    private void fillNextSlot(char c, TextView originView) {
        for (TextView slot : answerSlots) {
            if (!"clue".equals(slot.getTag()) && slot.getText().equals("_")) {
                slot.setText(String.valueOf(c));
                originView.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void checkAnswer() {
        StringBuilder builder = new StringBuilder();
        for (TextView slot : answerSlots) builder.append(slot.getText().toString());

        if (builder.toString().equals(currentWord)) {
            Toast.makeText(this, "Benar! 🎉", Toast.LENGTH_SHORT).show();
            answeredCorrectly = true;
            score += 10;
            updateScoreDisplay();
            saveScoreToFirestore(score);
            showWinPopup();
            countDownTimer.cancel();
        } else {
            Toast.makeText(this, "Salah! Coba lagi!", Toast.LENGTH_SHORT).show();
            resetJawaban();
        }

        Animation blink = AnimationUtils.loadAnimation(this, R.anim.blink);
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);

        for (TextView slot : answerSlots) {
            if (!"clue".equals(slot.getTag())) slot.startAnimation(shake);
            slot.startAnimation(blink);
        }
    }

    private void resetJawaban() {
        for (TextView slot : answerSlots) {
            if (!"clue".equals(slot.getTag())) slot.setText("_");
        }
        for (TextView letter : letterViews) letter.setVisibility(View.VISIBLE);
    }

    private void updateScoreDisplay() {
        tvScore.setText("Skor: " + score);
    }

    private void showWinPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_win, null);
        builder.setView(view).setCancelable(false);

        AlertDialog dialog = builder.create();
        Button btnNextLevel = view.findViewById(R.id.btnNextLevel);
        btnNextLevel.setOnClickListener(v -> {
            dialog.dismiss();
            setupGame();
        });
        dialog.show();
    }

    private void pauseGame() {
        if (countDownTimer != null) countDownTimer.cancel();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_pause, null);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnLanjut).setOnClickListener(v -> {
            dialog.dismiss();
            startTimer();
        });

        view.findViewById(R.id.btnUlang).setOnClickListener(v -> {
            dialog.dismiss();
            setupGame();
        });

        view.findViewById(R.id.btnHome).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        dialog.show();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int secondsLeft = (int) millisUntilFinished / 1000;
                tvTimer.setText("Waktu: " + secondsLeft);
            }

            public void onFinish() {
                showTimeUpPopup();
            }
        }.start();
    }

    private void resetTimer() {
        timeLeftInMillis = 60000;
    }

    private void showTimeUpPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_timeup, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        Button btnUlang = view.findViewById(R.id.btnUlang);
        Button btnHome = view.findViewById(R.id.btnHome);

        btnUlang.setOnClickListener(v -> {
            dialog.dismiss();
            setupGame();
        });

        btnHome.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        dialog.show();
    }

    // ✅ Simpan skor ke Firestore
    private void saveScoreToFirestore(int skor) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            data.put("skor", skor);
            data.put("waktu", System.currentTimeMillis());

            db.collection("skor")
                    .add(data)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(this, "Skor tersimpan di Firestore", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Gagal simpan skor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
