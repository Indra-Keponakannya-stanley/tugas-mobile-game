package com.example.slidingpuzzlegame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class MemoryGameActivity extends AppCompatActivity {

    private CountDownTimer timer;
    private final long TOTAL_TIME = 60000; // 1 menit
    private final long PREVIEW_TIME = 3000; // 3 detik

    int[] imageResIds = {
            R.drawable.mangga, R.drawable.salak,
            R.drawable.buah_naga, R.drawable.anggur,
            R.drawable.nanas, R.drawable.semangka,
            R.drawable.jeruk, R.drawable.pisang
    };

    ArrayList<Integer> imageList = new ArrayList<>();
    ArrayList<ImageView> cardViews = new ArrayList<>();
    GridLayout gridLayout;
    TextView timerText;
    int firstCardIndex = -1;
    int matchesFound = 0;
    int attempts = 0;
    boolean isClickable = false;
    int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        gridLayout = findViewById(R.id.gridLayout);
        timerText = findViewById(R.id.timerText);

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

        for (int resId : imageResIds) {
            imageList.add(resId);
            imageList.add(resId);
        }
        Collections.shuffle(imageList);

        for (int i = 0; i < imageList.size(); i++) {
            final int index = i;
            ImageView card = new ImageView(this);
            card.setImageResource(imageList.get(i)); // preview sementara
            card.setScaleType(ImageView.ScaleType.CENTER_CROP);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = 0;
            param.height = 0;
            param.rowSpec = GridLayout.spec(i / 4, 1f);
            param.columnSpec = GridLayout.spec(i % 4, 1f);
            card.setLayoutParams(param);

            card.setTag("hidden"); // status awal
            cardViews.add(card);
            gridLayout.addView(card);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isClickable || "matched".equals(card.getTag()) || card.getDrawable() != null && !"hidden".equals(card.getTag()))
                        return;
                    handleCardClick(index);
                }
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

                    public void onTick(long millisUntilFinished) {
                    }
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

        score = (matchesFound * 100) / (attempts == 0 ? 1 : attempts);
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

        // Buat dialog
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Tutup dialog dulu
                setupGame();
                previewCards();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Tutup dialog
                finish(); // kembali ke MainActivity
            }
        });

        layout.addView(retry);
        layout.addView(back);
        dialog.show();
    }
}
