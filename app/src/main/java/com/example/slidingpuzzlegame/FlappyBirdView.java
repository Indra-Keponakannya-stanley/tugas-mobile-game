package com.example.slidingpuzzlegame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FlappyBirdView extends View {
    private int lastPipeX = 0;
    private long gameStartTime;
    private Random random = new Random();
    private boolean isCountingDown = false;
    private int countdownValue = 0;

    // Waktu
    private long startTime;
    private long elapsedTime;
    private long firstPipeDelay = 3000;

    // Sound
    private SoundPool soundPool;
    private int soundHit;
    private int soundWing;
    private boolean isPaused = false;
    private boolean isGameStarted = false;

    // Paint
    private Paint paint;

    // Bird
    private int birdY = 500;
    private int velocity = 0;
    private int gravity = 2;
    private int jump = -30;

    // Score
    private int score = 0;
    private int bestScore = 0;
    private long bestTime = 0;
    private boolean gameOver = false;

    // Pipes
    private ArrayList<Rect> pipes;
    private ArrayList<Boolean> passedPipes;
    private int pipeWidth = 200;
    private int pipeGap = 400;
    private int pipeSpeed = 12;

    // Pipe Bitmap
    private Bitmap pipeTopBitmap;
    private Bitmap pipeBottomBitmap;

    // Fruit
    private Bitmap fruitBitmap;
    private int fruitSize = 100;
    private Rect fruitRect;

    // Background
    private Bitmap backgroundBitmap;
    private int bgX1 = 0;
    private int bgX2;
    private int bgSpeed = 5;

    // Ground
    private Bitmap groundBitmap;
    private int groundX1 = 0;
    private int groundX2;
    private int groundHeight;

    // SharedPreferences
    private SharedPreferences prefs;

    // ✅ Firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public FlappyBirdView(Context context) {
        super(context);
        paint = new Paint();
        paint.setTextSize(70);
        paint.setAntiAlias(true);

        prefs = context.getSharedPreferences("FlappyBirdPrefs", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("bestScore", 0);
        bestTime = prefs.getLong("bestTime", 0);

        pipes = new ArrayList<>();
        passedPipes = new ArrayList<>();

        // ✅ Init Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Sound
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundHit = soundPool.load(context, R.raw.hit, 1);
        soundWing = soundPool.load(context, R.raw.wing, 1);

        // Pipes Bitmap
        Bitmap pTop = BitmapFactory.decodeResource(getResources(), R.drawable.top_pipe);
        Bitmap pBottom = BitmapFactory.decodeResource(getResources(), R.drawable.bottom_pipe);
        pipeTopBitmap = Bitmap.createScaledBitmap(pTop, pipeWidth, pTop.getHeight(), true);
        pipeBottomBitmap = Bitmap.createScaledBitmap(pBottom, pipeWidth, pBottom.getHeight(), true);

        // Buah pertama
        loadRandomFruit(context);

        // Background
        Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.background_day);
        backgroundBitmap = Bitmap.createScaledBitmap(bg, getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels, true);
        bgX2 = backgroundBitmap.getWidth();

        // Ground
        Bitmap base = BitmapFactory.decodeResource(getResources(), R.drawable.base);
        groundHeight = base.getHeight();
        groundBitmap = Bitmap.createScaledBitmap(base, getResources().getDisplayMetrics().widthPixels,
                groundHeight, true);
        groundX2 = groundBitmap.getWidth();

        startTime = System.currentTimeMillis();
    }

    private void loadRandomFruit(Context context) {
        int fruitResId = getResources().getIdentifier(
                "buah" + (random.nextInt(10) + 1),
                "drawable",
                context.getPackageName()
        );
        Bitmap original = BitmapFactory.decodeResource(getResources(), fruitResId);
        fruitBitmap = Bitmap.createScaledBitmap(original, fruitSize, fruitSize, true);

        fruitRect = new Rect(200 - fruitSize / 2, birdY - fruitSize / 2,
                200 + fruitSize / 2, birdY + fruitSize / 2);
    }

    // ================= GAME DRAWING =======================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isCountingDown) {
            paint.setColor(Color.RED);
            paint.setTextSize(150);
            paint.setTextAlign(Paint.Align.CENTER);
            String text = (countdownValue == 0) ? "GO!" : String.valueOf(countdownValue);
            canvas.drawText(text, getWidth() / 2f, getHeight() / 2f, paint);
            return;
        }

        // Scroll background
        if (!isPaused && !gameOver && isGameStarted) {
            bgX1 -= bgSpeed;
            bgX2 -= bgSpeed;
            if (bgX1 + backgroundBitmap.getWidth() < 0) bgX1 = bgX2 + backgroundBitmap.getWidth();
            if (bgX2 + backgroundBitmap.getWidth() < 0) bgX2 = bgX1 + backgroundBitmap.getWidth();
        }

        canvas.drawBitmap(backgroundBitmap, bgX1, 0, null);
        canvas.drawBitmap(backgroundBitmap, bgX2, 0, null);

        fruitRect.set(200 - fruitSize / 2, birdY - fruitSize / 2,
                200 + fruitSize / 2, birdY + fruitSize / 2);
        canvas.drawBitmap(fruitBitmap, fruitRect.left, fruitRect.top, null);

        if (!gameOver && !isPaused && isGameStarted) {
            birdY += velocity;
            velocity += gravity;
        }

        // Gerakan pipa
        ArrayList<Rect> newPipes = new ArrayList<>();
        ArrayList<Boolean> newPassed = new ArrayList<>();

        for (int i = 0; i < pipes.size(); i += 2) {
            Rect top = pipes.get(i);
            Rect bottom = pipes.get(i + 1);

            if (!isPaused && !gameOver && isGameStarted) {
                top.offset(-pipeSpeed, 0);
                bottom.offset(-pipeSpeed, 0);
            }

            canvas.drawBitmap(pipeTopBitmap, null, top, null);
            canvas.drawBitmap(pipeBottomBitmap, null, bottom, null);

            if (top.right > 0) {
                newPipes.add(top);
                newPipes.add(bottom);
                newPassed.add(passedPipes.get(i / 2));
            }
        }
        pipes = newPipes;
        passedPipes = newPassed;

        if (isGameStarted && (System.currentTimeMillis() - startTime) > firstPipeDelay) {
            if (pipes.isEmpty() || (getWidth() - lastPipeX) > 500) {
                lastPipeX = getWidth();
                addPipe(lastPipeX);
            }
        }

        if (!isPaused && !gameOver && isGameStarted) {
            for (int i = 0; i < pipes.size(); i += 2) {
                Rect top = pipes.get(i);
                Rect bottom = pipes.get(i + 1);
                if (checkPixelCollision(top) || checkPixelCollision(bottom)) triggerGameOver();
                int pairIndex = i / 2;
                if (!passedPipes.get(pairIndex) && fruitRect.left > top.right) {
                    score++;
                    passedPipes.set(pairIndex, true);
                }
            }
            if (birdY > getHeight() - fruitSize / 2 - groundHeight) triggerGameOver();
        }

        // Ground
        if (!isPaused && !gameOver && isGameStarted) {
            groundX1 -= pipeSpeed;
            groundX2 -= pipeSpeed;
            if (groundX1 + groundBitmap.getWidth() < 0) groundX1 = groundX2 + groundBitmap.getWidth();
            if (groundX2 + groundBitmap.getWidth() < 0) groundX2 = groundX1 + groundBitmap.getWidth();
        }
        int groundY = getHeight() - groundHeight;
        canvas.drawBitmap(groundBitmap, groundX1, groundY, null);
        canvas.drawBitmap(groundBitmap, groundX2, groundY, null);

        paint.setColor(Color.BLACK);
        paint.setTextSize(70);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Score: " + score, 50, 100, paint);

        if (isGameStarted && !isPaused && !gameOver) elapsedTime = (System.currentTimeMillis() - startTime);
        long seconds = elapsedTime / 1000;
        canvas.drawText("Waktu: " + seconds + "s", 50, 180, paint);

        if (!isGameStarted && !gameOver) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.BLUE);
            canvas.drawText("TAP TO START", getWidth() / 2f, getHeight() / 2f, paint);
        }

        if (!gameOver) invalidate();
    }

    private void addPipe(int x) {
        int gap = (score <= 10) ? 500 : (score <= 30) ? 420 : (score <= 50) ? 350 : 300;
        int centerY = random.nextInt(getHeight() - groundHeight - 400) + 200;

        Rect top1 = new Rect(x, 0, x + pipeWidth, centerY - gap / 2);
        Rect bottom1 = new Rect(x, centerY + gap / 2, x + pipeWidth, getHeight());

        pipes.add(top1);
        pipes.add(bottom1);
        passedPipes.add(false);
    }

    private boolean checkPixelCollision(Rect pipe) {
        return Rect.intersects(pipe, fruitRect);
    }

    // ================= GAME OVER =======================
    private void triggerGameOver() {
        if (!gameOver) {
            gameOver = true;
            soundPool.play(soundHit, 1, 1, 0, 0, 1);

            long currentTime = elapsedTime / 1000;

            if (score > bestScore) bestScore = score;
            if (currentTime > bestTime) bestTime = currentTime;

            prefs.edit().putInt("bestScore", bestScore).putLong("bestTime", bestTime).apply();

            // ✅ Simpan skor ke Firestore
            saveFlappyScoreToFirestore(score);

            showGameOverDialog(score, currentTime, bestScore, bestTime);
        }
    }

    private void showGameOverDialog(int score, long time, int bestScore, long bestTime) {
        new AlertDialog.Builder(getContext())
                .setTitle("Game Over")
                .setMessage("Score: " + score +
                        "\nWaktu: " + time + "s" +
                        "\n\n🏆 Best Score: " + bestScore +
                        "\n⏳ Waktu Terlama: " + bestTime + "s")
                .setCancelable(false)
                .setPositiveButton("Main Ulang", (dialog, which) -> restartGame())
                .setNegativeButton("Keluar", (dialog, which) -> ((android.app.Activity) getContext()).finish())
                .show();
    }

    // ================= FIRESTORE SAVE =======================
    private void saveFlappyScoreToFirestore(int skorFlappy) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User belum login!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();

        firestore.collection("skor").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> data;
                    if (doc.exists()) {
                        data = doc.getData();
                        if (data == null) data = new HashMap<>();
                        data.put("skor_flappy", skorFlappy);
                        int total = skorFlappy
                                + toInt(data.get("skor_Perhitungan"))
                                + toInt(data.get("skor_tebak"))
                                + toInt(data.get("skor_mengingat"))
                                + toInt(data.get("skor_puzzle"));
                        data.put("total", total);
                    } else {
                        data = new HashMap<>();
                        data.put("email", email);
                        data.put("skor_flappy", skorFlappy);
                        data.put("skor_Perhitungan", 0);
                        data.put("skor_tebak", 0);
                        data.put("skor_mengingat", 0);
                        data.put("skor_puzzle", 0);
                        data.put("total", skorFlappy);
                    }

                    firestore.collection("skor").document(uid).set(data)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "✅ Skor Flappy Bird tersimpan!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "❌ Gagal simpan skor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "❌ Gagal akses Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int toInt(Object obj) {
        try {
            return obj instanceof Number ? ((Number) obj).intValue() : Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }
    }

    // ================= CONTROL =======================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isGameStarted) {
                isGameStarted = true;
                startTime = System.currentTimeMillis();
            }
            if (!gameOver && isGameStarted) {
                velocity = jump;
                soundPool.play(soundWing, 1, 1, 0, 0, 1);
            }
        }
        return true;
    }

    private void restartGame() {
        birdY = 500;
        velocity = 0;
        score = 0;
        gameOver = false;
        isGameStarted = false;
        pipes.clear();
        passedPipes.clear();
        loadRandomFruit(getContext());
        bgX1 = 0;
        bgX2 = backgroundBitmap.getWidth();
        groundX1 = 0;
        groundX2 = groundBitmap.getWidth();

        startTime = System.currentTimeMillis();
        invalidate();
    }

    public void pauseGame() { isPaused = true; }
    public void resumeGame() { isPaused = false; invalidate(); }

    public void startCountdown() {
        isCountingDown = true;
        countdownValue = 3;

        new Thread(() -> {
            try {
                while (countdownValue >= 0) {
                    postInvalidate();
                    Thread.sleep(1000);
                    countdownValue--;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isCountingDown = false;
            resumeGame();
            postInvalidate();
        }).start();
    }
}
