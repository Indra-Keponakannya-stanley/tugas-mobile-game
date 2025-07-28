package com.example.slidingpuzzlegame;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
     * ✅ Mendengarkan data leaderboard secara realtime
     */
    private void listenLeaderboardRealtime() {
        db.collection("skor")
                .orderBy("total", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showError("❌ Gagal mengambil leaderboard: " + error.getMessage());
                        return;
                    }
                    if (value == null || value.isEmpty()) {
                        showError("⚠️ Belum ada data leaderboard");
                        return;
                    }

                    tableLeaderboard.removeAllViews();

                    // ✅ Tambahkan Header (8 kolom)
                    addHeaderRow("Rank", "Email", "Flappy", "Puzzle", "Tebak", "Mengingat", "Perhitungan", "Total");

                    int rank = 1;
                    for (QueryDocumentSnapshot doc : value) {
                        String email = doc.getString("email");
                        if (email == null) email = "-";

                        long flappy = doc.contains("skor_flappy") ? doc.getLong("skor_flappy") : 0;
                        long puzzle = doc.contains("skor_puzzle") ? doc.getLong("skor_puzzle") : 0;
                        long tebak  = doc.contains("skor_tebak") ? doc.getLong("skor_tebak") : 0;
                        long mengingat = doc.contains("skor_mengingat") ? doc.getLong("skor_mengingat") : 0;
                        long perhitungan = doc.contains("skor_Perhitungan") ? doc.getLong("skor_Perhitungan") : 0;

                        long total = doc.contains("total") ? doc.getLong("total")
                                : (flappy + puzzle + tebak + mengingat + perhitungan);

                        // ✅ Tambahkan row dengan semua skor
                        addRow(rank, email, flappy, puzzle, tebak, mengingat, perhitungan, total);
                        rank++;
                    }
                });
    }

    /**
     * ✅ Menampilkan pesan error jika gagal load data
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
     * ✅ Tambahkan header tabel
     */
    private void addHeaderRow(String rank, String email, String flappy, String puzzle, String tebak,
                              String mengingat, String perhitungan, String total) {
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createHeaderCell(rank));
        headerRow.addView(createHeaderCell(email));
        headerRow.addView(createHeaderCell(flappy));
        headerRow.addView(createHeaderCell(puzzle));
        headerRow.addView(createHeaderCell(tebak));
        headerRow.addView(createHeaderCell(mengingat));
        headerRow.addView(createHeaderCell(perhitungan));
        headerRow.addView(createHeaderCell(total));
        tableLeaderboard.addView(headerRow);
    }

    /**
     * ✅ Tambahkan baris data pengguna
     */
    private void addRow(int rank, String email, long flappy, long puzzle, long tebak,
                        long mengingat, long perhitungan, long total) {
        TableRow row = new TableRow(this);
        row.setBackgroundColor(rank % 2 == 0 ? Color.parseColor("#F8F8F8") : Color.WHITE);

        row.addView(createCell(String.valueOf(rank)));
        row.addView(createCell(email));
        row.addView(createCell(String.valueOf(flappy)));
        row.addView(createCell(String.valueOf(puzzle)));
        row.addView(createCell(String.valueOf(tebak)));
        row.addView(createCell(String.valueOf(mengingat)));
        row.addView(createCell(String.valueOf(perhitungan)));
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
