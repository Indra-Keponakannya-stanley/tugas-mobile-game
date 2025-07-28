package com.example.slidingpuzzlegame;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailLogin, edtPasswordLogin, edtEmailReg, edtPasswordReg;
    private Button btnLogin, btnRegister;
    private RadioGroup tabGroup;
    private LinearLayout loginForm, registerForm;
    private TextView tvLoginInfo;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // ✅ Jika sudah login, langsung ke MainActivity
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user_email", currentUser.getEmail());
            startActivity(intent);
            finish();
            return;
        }

        // Kalau belum login, tampilkan layout login-register
        setContentView(R.layout.dialog_login_register);

        // Inisialisasi UI
        edtEmailLogin = findViewById(R.id.loginEmail);
        edtPasswordLogin = findViewById(R.id.loginPassword);
        edtEmailReg = findViewById(R.id.registerEmail);
        edtPasswordReg = findViewById(R.id.registerPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tabGroup = findViewById(R.id.tabGroup);
        loginForm = findViewById(R.id.loginForm);
        registerForm = findViewById(R.id.registerForm);
        tvLoginInfo = findViewById(R.id.tvLoginInfo);

        // Klik tombol
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());

        // Tab login/register
        tabGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLogin) {
                loginForm.setVisibility(View.VISIBLE);
                registerForm.setVisibility(View.GONE);
            } else {
                loginForm.setVisibility(View.GONE);
                registerForm.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loginUser() {
        String email = edtEmailLogin.getText().toString().trim();
        String password = edtPasswordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                tampilkanInfoLogin(user.getEmail());
            } else {
                Toast.makeText(this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerUser() {
        String email = edtEmailReg.getText().toString().trim();
        String password = edtPasswordReg.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                tampilkanInfoLogin(user.getEmail());
            } else {
                Toast.makeText(this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Setelah login, tampilkan email pengguna & sembunyikan form
    private void tampilkanInfoLogin(String email) {
        tabGroup.setVisibility(View.GONE);
        loginForm.setVisibility(View.GONE);
        registerForm.setVisibility(View.GONE);

        tvLoginInfo.setText("✅ Kamu sudah login dengan Gmail:\n" + email);
        tvLoginInfo.setVisibility(View.VISIBLE);
    }
}
