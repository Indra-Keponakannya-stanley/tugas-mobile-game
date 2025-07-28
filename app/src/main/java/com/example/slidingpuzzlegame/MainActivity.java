package com.example.slidingpuzzlegame;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button btnExit, btnLeaderboard;
    ImageButton btnVolumeToggle, btnAccount;
    MediaPlayer mediaPlayer;
    boolean isVolumeOn = true;
    private float volume = 1.0f;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvUserEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnExit = findViewById(R.id.btnExit);
        btnVolumeToggle = findViewById(R.id.btnVolumeToggle);
        btnAccount = findViewById(R.id.btnAccount);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvUserEmail.setText("Login sebagai: " + currentUser.getEmail());
            ensureUserInFirestore(currentUser);
        } else {
            tvUserEmail.setText("Belum login");
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.bg_musik);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.start();

        btnExit.setOnClickListener(v -> finishAffinity());

        btnVolumeToggle.setOnClickListener(v -> {
            isVolumeOn = !isVolumeOn;
            if (isVolumeOn) {
                mediaPlayer.setVolume(volume, volume);
                btnVolumeToggle.setImageResource(R.drawable.volume_on);
            } else {
                mediaPlayer.setVolume(0f, 0f);
                btnVolumeToggle.setImageResource(R.drawable.volume_off);
            }
        });

        btnAccount.setOnClickListener(v -> showLoginRegisterDialog());

        btnLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
        });

        Button btnOpenGameMenu = findViewById(R.id.btnOpenGameMenu);
        btnOpenGameMenu.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.popup_menu_game, null);

            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            Button btnPuzzle = popupView.findViewById(R.id.popupBtnPuzzle);
            Button btnPerhitungan = popupView.findViewById(R.id.popupBtnPerhitungan);
            Button btnMemory = popupView.findViewById(R.id.popupBtnMemory);
            Button btnTebak = popupView.findViewById(R.id.popupBtnTebak);
            Button btnFlappy = popupView.findViewById(R.id.popupBtnFlappy);

            btnFlappy.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, FlappyBirdActivity.class));
                popupWindow.dismiss();
            });

            btnPuzzle.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, PuzzleGameActivity.class));
                popupWindow.dismiss();
            });

            btnPerhitungan.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, GamePerhitunganActivity.class));
                popupWindow.dismiss();
            });

            btnMemory.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, MemoryGameActivity.class));
                popupWindow.dismiss();
            });

            btnTebak.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, TebakActivity.class));
                popupWindow.dismiss();
            });
        });
    }

    private void showLoginRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_login_register, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText loginEmail = dialogView.findViewById(R.id.loginEmail);
        EditText loginPassword = dialogView.findViewById(R.id.loginPassword);
        Button btnLogin = dialogView.findViewById(R.id.btnLogin);

        EditText registerEmail = dialogView.findViewById(R.id.registerEmail);
        EditText registerPassword = dialogView.findViewById(R.id.registerPassword);
        Button btnRegister = dialogView.findViewById(R.id.btnRegister);

        RadioButton radioLogin = dialogView.findViewById(R.id.radioLogin);
        RadioButton radioRegister = dialogView.findViewById(R.id.radioRegister);
        View loginForm = dialogView.findViewById(R.id.loginForm);
        View registerForm = dialogView.findViewById(R.id.registerForm);

        radioLogin.setOnClickListener(v -> {
            loginForm.setVisibility(View.VISIBLE);
            registerForm.setVisibility(View.GONE);
        });

        radioRegister.setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
        });

        btnLogin.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String pass = loginPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            tvUserEmail.setText("Login sebagai: " + user.getEmail());
                            ensureUserInFirestore(user);
                        } else {
                            Toast.makeText(this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            String email = registerEmail.getText().toString().trim();
            String pass = registerPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            tvUserEmail.setText("Login sebagai: " + user.getEmail());
                            ensureUserInFirestore(user);
                        } else {
                            Toast.makeText(this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // ✅ Fungsi untuk memastikan user punya data di Firestore
    private void ensureUserInFirestore(FirebaseUser user) {
        db.collection("skor").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("email", user.getEmail());
                        data.put("skor_flappy", 0);
                        data.put("skor_puzzle", 0);
                        data.put("skor_tebak", 0);
                        data.put("total", 0);
                        db.collection("skor").document(user.getUid()).set(data);
                    }
                });
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
