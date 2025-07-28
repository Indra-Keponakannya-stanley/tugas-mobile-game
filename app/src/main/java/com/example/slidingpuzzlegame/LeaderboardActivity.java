package com.example.slidingpuzzlegame;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class LeaderboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TableLayout tableLeaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        db = FirebaseFirestore.getInstance();
        tableLeaderboard = findViewById(R.id.tableLeaderboard);

        listenLeaderboardRealtime();
    }

    /**
     * ✅ Dengarkan perubahan data leaderboard secara realtime dari Firestore
     */
    private void listenLeaderboardRealtime() {
        db.collection("skor")
                .orderBy("total", Query.Direction.DESCENDING) // Urutkan dari skor total tertinggi
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            showError("❌ Gagal mengambil leaderboard: " + error.getMessage());
                            return;
                        }

                        if (value == null || value.isEmpty()) {
                            showError("⚠️ Belum ada data leaderboard");
                            return;
                        }

                        // 🔄 Hapus tabel lama sebelum update
                        tableLeaderboard.removeAllViews();

                        // 🟦 Tambahkan header
                        addHeaderRow("Rank", "Email", "Flappy", "Puzzle", "Tebak", "Total");

                        int rank = 1;
                        for (QueryDocumentSnapshot doc : value) {
                            String email = doc.getString("email");
                            if (email == null) email = "-";

                            long flappy = doc.contains("skor_flappy") ? doc.getLong("skor_flappy") : 0;
                            long puzzle = doc.contains("skor_puzzle") ? doc.getLong("skor_puzzle") : 0;
                            long tebak  = doc.contains("skor_tebak") ? doc.getLong("skor_tebak") : 0;

                            // ✅ Ambil total dari Firestore jika ada, jika tidak hitung manual
                            long total = doc.contains("total") ? doc.getLong("total") : (flappy + puzzle + tebak);

                            addRow(rank, email, flappy, puzzle, tebak, total);
                            rank++;
                        }
                    }
                });
    }

    /**
     * ✅ Tampilkan pesan error di tabel leaderboard
     */
    private void showError(String message) {
        tableLeaderboard.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setPadding(20, 20, 20, 20);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.RED);
        tableLeaderboard.addView(tv);
    }

    /**
     * ✅ Tambahkan baris header
     */
    private void addHeaderRow(String rank, String email, String flappy, String puzzle, String tebak, String total) {
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createHeaderCell(rank));
        headerRow.addView(createHeaderCell(email));
        headerRow.addView(createHeaderCell(flappy));
        headerRow.addView(createHeaderCell(puzzle));
        headerRow.addView(createHeaderCell(tebak));
        headerRow.addView(createHeaderCell(total));
        tableLeaderboard.addView(headerRow);
    }

    /**
     * ✅ Tambahkan baris data user
     */
    private void addRow(int rank, String email, long flappy, long puzzle, long tebak, long total) {
        TableRow row = new TableRow(this);

        // 🔄 Warna selang-seling
        if (rank % 2 == 0) {
            row.setBackgroundColor(Color.parseColor("#F8F8F8"));
        } else {
            row.setBackgroundColor(Color.WHITE);
        }

        row.addView(createCell(String.valueOf(rank)));
        row.addView(createCell(email));
        row.addView(createCell(String.valueOf(flappy)));
        row.addView(createCell(String.valueOf(puzzle)));
        row.addView(createCell(String.valueOf(tebak)));
        row.addView(createCell(String.valueOf(total)));

        tableLeaderboard.addView(row);
    }

    /**
     * ✅ Membuat cell biasa
     */
    private TextView createCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 12, 16, 12);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(14);
        return tv;
    }

    /**
     * ✅ Membuat cell header (biru bold)
     */
    private TextView createHeaderCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 12, 16, 12);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(16);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.parseColor("#007BFF"));
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }
}
