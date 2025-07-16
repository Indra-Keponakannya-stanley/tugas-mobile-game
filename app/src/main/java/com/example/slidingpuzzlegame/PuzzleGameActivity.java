package com.example.slidingpuzzlegame;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PuzzleGameActivity extends AppCompatActivity {

    private GridLayout puzzleGrid;
    private TextView timerText, scoreText;
    private ImageButton btnPause;
    private ImageView imagePreview;

    private CountDownTimer timer;
    private static final long TOTAL_TIME = 60000;
    private int score = 0;

    private ArrayList<ImageView> slots = new ArrayList<>();
    private ArrayList<Bitmap> correctPieces;

    private AlertDialog resultDialog;
    private AlertDialog pauseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);

        puzzleGrid = findViewById(R.id.puzzleGrid);
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        btnPause = findViewById(R.id.btnPause);
        imagePreview = findViewById(R.id.imagePreview); // ✅ Referensi gambar buah

        setupPuzzle();
        startTimer();

        btnPause.setOnClickListener(v -> pauseGame());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPuzzle() {
        int[] buahImages = {
                R.drawable.buah1, R.drawable.buah2, R.drawable.buah3, R.drawable.buah4,
                R.drawable.buah5, R.drawable.buah6, R.drawable.buah7, R.drawable.buah8,
                R.drawable.buah9, R.drawable.buah10
        };

        int randomImageResId = buahImages[new Random().nextInt(buahImages.length)];
        imagePreview.setImageResource(randomImageResId); // ✅ Tampilkan gambar referensi buah

        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), randomImageResId);
        correctPieces = new ArrayList<>();
        for (Bitmap piece : splitImage(originalBitmap)) {
            correctPieces.add(addWhiteBackgroundAndBorder(piece));
        }

        ArrayList<Bitmap> shuffled = new ArrayList<>(correctPieces);
        Collections.shuffle(shuffled);

        puzzleGrid.removeAllViews();
        slots.clear();

        for (int i = 0; i < shuffled.size(); i++) {
            ImageView slot = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 200;
            params.height = 200;
            slot.setLayoutParams(params);
            slot.setScaleType(ImageView.ScaleType.CENTER_CROP);
            slot.setImageBitmap(shuffled.get(i));
            slot.setTag(shuffled.get(i));

            slot.setOnTouchListener((v, event) -> {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(null, shadowBuilder, v, 0);
                return true;
            });

            slot.setOnDragListener((targetView, event) -> {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    View draggedView = (View) event.getLocalState();
                    if (draggedView != targetView &&
                            draggedView instanceof ImageView &&
                            targetView instanceof ImageView) {

                        ImageView from = (ImageView) draggedView;
                        ImageView to = (ImageView) targetView;

                        BitmapDrawable fromDrawable = (BitmapDrawable) from.getDrawable();
                        BitmapDrawable toDrawable = (BitmapDrawable) to.getDrawable();

                        Bitmap fromBitmap = fromDrawable.getBitmap();
                        Bitmap toBitmap = toDrawable.getBitmap();

                        from.setImageBitmap(toBitmap);
                        from.setTag(toBitmap);

                        to.setImageBitmap(fromBitmap);
                        to.setTag(fromBitmap);

                        if (checkIfSolved()) {
                            if (timer != null) timer.cancel();
                            score++;
                            scoreText.setText("Score: " + score);
                            showResultDialog(true);
                        }
                    }
                }
                return true;
            });

            puzzleGrid.addView(slot);
            slots.add(slot);
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(TOTAL_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                showResultDialog(false);
            }
        };
        timer.start();
    }

    private boolean checkIfSolved() {
        for (int i = 0; i < correctPieces.size(); i++) {
            ImageView slot = slots.get(i);
            Bitmap current = ((BitmapDrawable) slot.getDrawable()).getBitmap();
            if (!current.sameAs(correctPieces.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void showResultDialog(boolean isWin) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView resultText = new TextView(this);
        resultText.setText(isWin ? "Selamat! Puzzle selesai!" : "Waktu habis! Kamu kalah.");
        resultText.setTextSize(18);
        layout.addView(resultText);

        Button btnRetry = new Button(this);
        btnRetry.setText("Lanjut");
        btnRetry.setOnClickListener(v -> {
            if (resultDialog != null) resultDialog.dismiss();
            setupPuzzle();
            startTimer();
        });
        layout.addView(btnRetry);

        Button btnBack = new Button(this);
        btnBack.setText("Kembali");
        btnBack.setOnClickListener(v -> {
            if (resultDialog != null) resultDialog.dismiss();
            finish();
        });
        layout.addView(btnBack);

        resultDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();
        resultDialog.show();
    }

    private void pauseGame() {
        if (timer != null) timer.cancel();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.WHITE);

        TextView pausedText = new TextView(this);
        pausedText.setText("Permainan Dijeda");
        pausedText.setTextSize(20);
        pausedText.setTextColor(Color.BLACK);
        pausedText.setPadding(0, 0, 0, 32);
        layout.addView(pausedText);

        layout.addView(createButtonRow(R.drawable.lanjutkan, "Kembali Main", v -> {
            pauseDialog.dismiss();
            startTimer();
        }));

        layout.addView(createButtonRow(R.drawable.ulangi, "Ulangi", v -> {
            pauseDialog.dismiss();
            setupPuzzle();
            startTimer();
        }));

        layout.addView(createButtonRow(R.drawable.home, "Keluar Game", v -> {
            pauseDialog.dismiss();
            finish();
        }));

        pauseDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();
        pauseDialog.show();
    }

    private LinearLayout createButtonRow(int imageResId, String text, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(24, 24, 24, 24);
        row.setBackgroundResource(R.drawable.black_border);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 20, 0, 20);
        row.setLayoutParams(rowParams);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageButton btn = new ImageButton(this);
        btn.setImageResource(imageResId);
        btn.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(200, 200);
        btnParams.setMargins(0, 0, 40, 0);
        btn.setLayoutParams(btnParams);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        btn.setAdjustViewBounds(true);

        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(22);
        label.setTextColor(Color.BLACK);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setGravity(android.view.Gravity.CENTER_VERTICAL);

        row.addView(btn);
        row.addView(label);
        row.setOnClickListener(listener);

        return row;
    }

    private ArrayList<Bitmap> splitImage(Bitmap image) {
        int rows = 3, cols = 3;
        int pieceWidth = image.getWidth() / cols;
        int pieceHeight = image.getHeight() / rows;
        ArrayList<Bitmap> pieces = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * pieceWidth;
                int y = row * pieceHeight;
                Bitmap piece = Bitmap.createBitmap(image, x, y, pieceWidth, pieceHeight);
                pieces.add(piece);
            }
        }
        return pieces;
    }

    private Bitmap addWhiteBackgroundAndBorder(Bitmap piece) {
        int borderSize = 4;

        Bitmap output = Bitmap.createBitmap(
                piece.getWidth() + borderSize * 2,
                piece.getHeight() + borderSize * 2,
                Bitmap.Config.ARGB_8888
        );

        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        android.graphics.Paint paint = new android.graphics.Paint();

        paint.setColor(android.graphics.Color.WHITE);
        paint.setStyle(android.graphics.Paint.Style.FILL);
        canvas.drawRect(0, 0, output.getWidth(), output.getHeight(), paint);

        canvas.drawBitmap(piece, borderSize, borderSize, null);

        paint.setStyle(android.graphics.Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(android.graphics.Color.BLACK);
        canvas.drawRect(
                1.5f, 1.5f,
                output.getWidth() - 1.5f,
                output.getHeight() - 1.5f,
                paint
        );

        return output;
    }
}
