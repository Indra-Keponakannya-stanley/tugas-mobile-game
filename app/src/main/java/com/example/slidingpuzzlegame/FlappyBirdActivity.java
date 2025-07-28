package com.example.slidingpuzzlegame;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FlappyBirdActivity extends AppCompatActivity {

    private FlappyBirdView flappyBirdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flappy_bird);

        // ✅ Tambahkan FlappyBirdView ke FrameLayout
        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        flappyBirdView = new FlappyBirdView(this);
        gameContainer.addView(flappyBirdView);

        // ✅ Tombol Pause
        ImageView btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(v -> {
            flappyBirdView.pauseGame();
            showPausePopup();
        });
    }

    // ✅ POPUP PAUSE
    private void showPausePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_pause, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        // ✅ Lanjut (Resume dengan countdown)
        view.findViewById(R.id.btnLanjut).setOnClickListener(v -> {
            dialog.dismiss();
            flappyBirdView.startCountdown(); // 🔥 Lanjut pakai countdown
        });

        // ✅ Ulang (Restart game)
        view.findViewById(R.id.btnUlang).setOnClickListener(v -> {
            dialog.dismiss();
            recreate();
        });

        // ✅ Home (Keluar game)
        view.findViewById(R.id.btnHome).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
    }

    /**
     * ✅ Fungsi ini dipanggil dari FlappyBirdView ketika game over
     */
    public void onGameOver(int finalScore) {
        simpanSkorFlappy(finalScore);
    }

    /**
     * ✅ Simpan skor Flappy ke Firestore (hanya update jika skor baru lebih tinggi)
     */
    private void simpanSkorFlappy(int skorBaru) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) return;

        db.collection("skor").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    long skorLama = doc.contains("skor_flappy") ? doc.getLong("skor_flappy") : 0;
                    long skorPuzzle = doc.contains("skor_puzzle") ? doc.getLong("skor_puzzle") : 0;
                    long skorTebak  = doc.contains("skor_tebak") ? doc.getLong("skor_tebak") : 0;

                    if (skorBaru > skorLama) {
                        long total = skorBaru + skorPuzzle + skorTebak;

                        Map<String, Object> update = new HashMap<>();
                        update.put("email", user.getEmail());
                        update.put("skor_flappy", skorBaru);
                        update.put("skor_puzzle", skorPuzzle);
                        update.put("skor_tebak", skorTebak);
                        update.put("total", total);

                        db.collection("skor").document(user.getUid()).set(update);
                    }
                });
    }
}
