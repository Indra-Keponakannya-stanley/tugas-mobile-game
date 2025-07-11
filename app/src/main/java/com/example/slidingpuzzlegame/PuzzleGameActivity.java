package com.example.slidingpuzzlegame;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class PuzzleGameActivity extends AppCompatActivity {

    private GridLayout puzzleGrid;
    private final int[] pieceDrawables = {
            R.drawable.puzzle2_piece_0, R.drawable.puzzle2_piece_1, R.drawable.puzzle2_piece_2,
            R.drawable.puzzle2_piece_3, R.drawable.puzzle2_piece_4, R.drawable.puzzle2_piece_5,
            R.drawable.puzzle2_piece_6, R.drawable.puzzle2_piece_7, R.drawable.puzzle2_piece_8
    };

    private ArrayList<ImageView> slots = new ArrayList<>();
    private ArrayList<Integer> shuffledPieces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);

        puzzleGrid = findViewById(R.id.puzzleGrid);

        setupPuzzle();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPuzzle() {
        shuffledPieces.clear();
        Collections.addAll(shuffledPieces, pieceDrawables);
        Collections.shuffle(shuffledPieces);

        puzzleGrid.removeAllViews();
        slots.clear();

        for (int i = 0; i < 9; i++) {
            ImageView slot = new ImageView(this);
            slot.setLayoutParams(new GridLayout.LayoutParams());
            slot.getLayoutParams().width = 300;
            slot.getLayoutParams().height = 300;
            slot.setScaleType(ImageView.ScaleType.CENTER_CROP);
            slot.setBackgroundResource(android.R.color.darker_gray);
            slot.setImageResource(shuffledPieces.get(i));
            slot.setTag(shuffledPieces.get(i));

            slot.setOnTouchListener((v, event) -> {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(null, shadowBuilder, v, 0);
                return true;
            });

            slot.setOnDragListener((targetView, event) -> {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    View draggedView = (View) event.getLocalState();
                    if (draggedView != targetView && draggedView instanceof ImageView && targetView instanceof ImageView) {
                        ImageView from = (ImageView) draggedView;
                        ImageView to = (ImageView) targetView;

                        Integer fromTag = (Integer) from.getTag();
                        Integer toTag = (Integer) to.getTag();

                        int fromImage = fromTag;
                        int toImage = toTag;

                        from.setImageResource(toImage);
                        from.setTag(toImage);

                        to.setImageResource(fromImage);
                        to.setTag(fromImage);

                        if (checkIfSolved()) {
                            showWinDialog(); // ⬅️ panggil dialog menang
                        }
                    }
                }
                return true;
            });

            puzzleGrid.addView(slot);
            slots.add(slot);
        }
    }

    private boolean checkIfSolved() {
        for (int i = 0; i < pieceDrawables.length; i++) {
            ImageView slot = slots.get(i);
            Integer tag = (Integer) slot.getTag();
            if (tag == null || tag != pieceDrawables[i]) {
                return false;
            }
        }
        return true;
    }

    private void showWinDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView scoreText = new TextView(this);
        scoreText.setText("Selamat! Puzzle selesai!");
        scoreText.setTextSize(18);
        layout.addView(scoreText);

        Button btnRetry = new Button(this);
        btnRetry.setText("Lanjut");
        btnRetry.setOnClickListener(v -> setupPuzzle());
        layout.addView(btnRetry);

        Button btnBack = new Button(this);
        btnBack.setText("Kembali");
        btnBack.setOnClickListener(v -> finish());
        layout.addView(btnBack);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();
        dialog.show();
    }
}
