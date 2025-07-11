package com.example.slidingpuzzlegame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class PerhitunganActivity extends AppCompatActivity {

    TextView tvQuestion, tvScore;
    Button btnOption1, btnOption2, btnOption3;
    int correctAnswer;
    int score = 0;
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perhitungan);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);

        generateQuestion();

        View.OnClickListener answerClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button clicked = (Button) v;
                int selectedAnswer = Integer.parseInt(clicked.getText().toString());
                if (selectedAnswer == correctAnswer) {
                    score++;
                    tvScore.setText("Skor: " + score);
                }
                generateQuestion();
            }
        };

        btnOption1.setOnClickListener(answerClickListener);
        btnOption2.setOnClickListener(answerClickListener);
        btnOption3.setOnClickListener(answerClickListener);
    }

    private void generateQuestion() {
        int a = random.nextInt(10);
        int b = random.nextInt(10);
        correctAnswer = a + b;
        tvQuestion.setText("Berapakah " + a + " + " + b + "?");

        int wrong1 = correctAnswer + random.nextInt(5) + 1;
        int wrong2 = correctAnswer - (random.nextInt(4) + 1);
        while (wrong2 == correctAnswer) wrong2--;

        int position = random.nextInt(3);
        if (position == 0) {
            btnOption1.setText(String.valueOf(correctAnswer));
            btnOption2.setText(String.valueOf(wrong1));
            btnOption3.setText(String.valueOf(wrong2));
        } else if (position == 1) {
            btnOption1.setText(String.valueOf(wrong1));
            btnOption2.setText(String.valueOf(correctAnswer));
            btnOption3.setText(String.valueOf(wrong2));
        } else {
            btnOption1.setText(String.valueOf(wrong1));
            btnOption2.setText(String.valueOf(wrong2));
            btnOption3.setText(String.valueOf(correctAnswer));
        }
    }
}
