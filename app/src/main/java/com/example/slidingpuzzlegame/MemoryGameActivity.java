package com.example.slidingpuzzlegame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameActivity extends AppCompatActivity {

    private CountDownTimer timer;
    private final long TOTAL_TIME = 60000; // 1 menit
    private final long PREVIEW_TIME = 3000; // 3 detik

    int[] imageResIds = {
            R.drawable.buah1, R.drawable.buah2, R.drawable.buah3, R.drawable.buah4, R.drawable.buah5,
            R.drawable.buah6, R.drawable.buah7, R.drawable.buah8, R.drawable.buah9, R.drawable.buah10
    };

    ArrayList<Integer> imageList = new ArrayList<>();
    ArrayList<ImageView> cardViews = new ArrayList<>();
    GridLayout gridLayout;
    TextView timerText, scoreText;
    ImageButton btnPause;
    int firstCardIndex = -1;
    int matchesFound = 0;
    int attempts = 0;
    boolean isClickable = false;
    int score = 0;

    // ✅ Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        gridLayout = findViewById(R.id.gridLayout);
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        btnPause = findViewById(R.id.btnPause);

        // ✅ Firebase Init
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnPause.setOnClickListener(v -> {
            if (timer != null) timer.cancel();

            View popupView = getLayoutInflater().inflate(R.layout.popup_pause, null);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(MemoryGameActivity.this)
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
                setupGame();
                previewCards();
                dialog.dismiss();
            });

            btnHome.setOnClickListener(view -> {
                dialog.dismiss();
                finish();
            });

            dialog.show();
        });

        setupGame();
        previewCards();
    }

    private void setupGame() {
        imageList.clear();
        gridLayout.removeAllViews();
        cardViews.clear();
        matchesFound = 0;
        attempts = 0;
        score = 0;
        firstCardIndex = -1;
        updateScore();

        for (int resId : imageResIds) {
            imageList.add(resId);
            imageList.add(resId);
        }
        Collections.shuffle(imageList);

        for (int i = 0; i < imageList.size(); i++) {
            final int index = i;
            ImageView card = new ImageView(this);
            card.setImageResource(imageList.get(i));
            card.setScaleType(ImageView.ScaleType.CENTER_CROP);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = 0;
            param.height = 0;
            param.rowSpec = GridLayout.spec(i / 4, 1f);
            param.columnSpec = GridLayout.spec(i % 4, 1f);
            card.setLayoutParams(param);

            card.setTag("hidden");
            cardViews.add(card);
            gridLayout.addView(card);

            card.setOnClickListener(v -> {
                if (!isClickable || "matched".equals(card.getTag()) || !"hidden".equals(card.getTag()))
                    return;
                handleCardClick(index);
            });
        }
    }

    private void previewCards() {
        isClickable = false;
        for (int i = 0; i < cardViews.size(); i++) {
            cardViews.get(i).setImageResource(imageList.get(i));
        }

        new CountDownTimer(PREVIEW_TIME, 1000) {
            public void onFinish() {
                for (ImageView card : cardViews) {
                    card.setImageResource(R.drawable.kartu);
                    card.setTag("hidden");
                }
                isClickable = true;
                startTimer();
            }

            public void onTick(long millisUntilFinished) {
                timerText.setText("Preview: " + millisUntilFinished / 1000);
            }
        }.start();
    }

    private void startTimer() {
        timer = new CountDownTimer(TOTAL_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        };
        timer.start();
    }

    private void handleCardClick(int index) {
        ImageView clickedCard = cardViews.get(index);
        clickedCard.setImageResource(imageList.get(index));
        clickedCard.setTag("flipped");

        if (firstCardIndex == -1) {
            firstCardIndex = index;
        } else {
            isClickable = false;
            ImageView firstCard = cardViews.get(firstCardIndex);
            int firstImage = imageList.get(firstCardIndex);
            int secondImage = imageList.get(index);

            if (firstImage == secondImage) {
                matchesFound++;
                attempts++;

                score += 100; // ✅ Tambah poin
                updateScore();

                clickedCard.setTag("matched");
                firstCard.setTag("matched");

                firstCardIndex = -1;
                isClickable = true;

                if (matchesFound == imageList.size() / 2) {
                    endGame();
                }

            } else {
                attempts++;
                new CountDownTimer(500, 500) {
                    public void onFinish() {
                        clickedCard.setImageResource(R.drawable.kartu);
                        firstCard.setImageResource(R.drawable.kartu);
                        clickedCard.setTag("hidden");
                        firstCard.setTag("hidden");
                        firstCardIndex = -1;
                        isClickable = true;
                    }

                    public void onTick(long millisUntilFinished) {}
                }.start();
            }
        }
    }

    private void endGame() {
        if (timer != null) timer.cancel();
        isClickable = false;

        for (int i = 0; i < cardViews.size(); i++) {
            if (!"matched".equals(cardViews.get(i).getTag())) {
                cardViews.get(i).setImageResource(imageList.get(i));
            }
        }

        // ✅ Simpan skor ke Firebase
        saveScoreToFirebase(score);

        showScoreDialog();
    }

    private void showScoreDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView text = new TextView(this);
        text.setText("Selesai! Skor Anda: " + score);
        text.setTextSize(18);
        layout.addView(text);

        Button retry = new Button(this);
        retry.setText("Lanjut");

        Button back = new Button(this);
        back.setText("Kembali");

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();

        retry.setOnClickListener(v -> {
            dialog.dismiss();
            setupGame();
            previewCards();
        });

        back.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        layout.addView(retry);
        layout.addView(back);
        dialog.show();
    }

    private void updateScore() {
        scoreText.setText("Score: " + score);
    }

    // ✅ Fungsi Simpan Skor ke Firebase
    private void saveScoreToFirebase(int skorMengingat) {
        String email = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getEmail() : "guest";

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("skor_mengingat", skorMengingat);

        // ✅ Update ke Firestore (merge dengan data lama)
        db.collection("skor").document(email)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // ✅ Update total skor juga
                    db.collection("skor").document(email).get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            long total = 0;
                            if (doc.contains("skor_flappy")) total += doc.getLong("skor_flappy");
                            if (doc.contains("skor_puzzle")) total += doc.getLong("skor_puzzle");
                            if (doc.contains("skor_tebak")) total += doc.getLong("skor_tebak");
                            total += skorMengingat;
                            if (doc.contains("skor_Perhitungan")) total += doc.getLong("skor_Perhitungan");

                            db.collection("skor").document(email)
                                    .update("total", total);
                        }
                    });
                });
    }
}
