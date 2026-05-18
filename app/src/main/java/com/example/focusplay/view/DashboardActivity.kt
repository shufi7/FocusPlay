package com.example.focusplay.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvProfilAnakKosong: TextView
    private lateinit var containerProfilAnakDashboard: LinearLayout

    private lateinit var btnRiwayatPermainan: LinearLayout
    private lateinit var btnPengaturanPermainan: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var chartWeekly: WeeklyLineChartView

    data class AnakDashboard(
        val idDokumen: String,
        val namaAnak: String,
        val umur: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        hubungkanView()
        tampilkanNamaUser()
        aturAksiTombol()
    }

    override fun onResume() {
        super.onResume()
        ambilProfilAnak()
        ambilDataRiwayatDanGrafikRealtime()
    }

    private fun hubungkanView() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        tvProfilAnakKosong = findViewById(R.id.tvProfilAnakKosong)
        containerProfilAnakDashboard = findViewById(R.id.containerProfilAnakDashboard)

        btnRiwayatPermainan = findViewById(R.id.btnRiwayatPermainan)
        btnPengaturanPermainan = findViewById(R.id.btnPengaturanPermainan)
        btnLogout = findViewById(R.id.btnLogout)

        chartWeekly = findViewById(R.id.chartWeekly)
    }

    private fun tampilkanNamaUser() {
        val namaUser = session.getNamaUser()
        tvWelcomeName.text = if (namaUser.isNotEmpty()) {
            "Halo, $namaUser!"
        } else {
            "Halo, Orang Tua!"
        }
    }

    // --- FUNGSI UTAMA: AMBIL DATA GRAFIK DARI FIRESTORE ---
    private fun ambilDataRiwayatDanGrafikRealtime() {
        val currentUser = auth.currentUser ?: return

        db.collection("tb_riwayat")
            .whereEqualTo("id_pendamping", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING) // Ambil dari yang terbaru
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                if (snapshots != null && !snapshots.isEmpty) {
                    val listSesiMentah = mutableListOf<DataGrafikHarian>()

                    for (doc in snapshots) {
                        val akurasi = doc.getLong("akurasi")?.toInt() ?: 0
                        val tanggalStr = doc.getString("tanggal") ?: ""

                        // Potong string tanggal untuk label grafik (Misal: "18 May 2026..." diambil "18 May")
                        val labelGrafik = if (tanggalStr.length >= 6) tanggalStr.substring(0, 6) else "Sesi"
                        listSesiMentah.add(DataGrafikHarian(labelGrafik, akurasi.toFloat()))
                    }

                    // Update Grafik Mingguan (Ambil maksimal 6 sesi terbaru, lalu balik urutannya)
                    // Dibalik (.reversed()) agar grafik berjalan kronologis dari kiri (lama) ke kanan (terbaru)
                    val dataGrafikSelesai = listSesiMentah.take(6).reversed()
                    chartWeekly.setData(dataGrafikSelesai)

                } else {
                    // Jika data di tb_riwayat masih kosong melompong
                    chartWeekly.setData(emptyList())
                }
            }
    }

    private fun ambilProfilAnak() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            tvProfilAnakKosong.visibility = TextView.VISIBLE
            tvProfilAnakKosong.text = "Sesi login tidak ditemukan."
            return
        }

        containerProfilAnakDashboard.removeAllViews()
        tambahCardTambahAnakMini()

        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val daftarAnak = result.documents.mapNotNull { doc ->
                    val nama = doc.getString("nama_anak") ?: return@mapNotNull null
                    val umurMentah = doc.get("umur") ?: doc.get("usia")
                    val umur = umurMentah?.toString()?.toIntOrNull() ?: 0

                    AnakDashboard(
                        idDokumen = doc.id,
                        namaAnak = nama,
                        umur = umur
                    )
                }

                if (daftarAnak.isEmpty()) {
                    tvProfilAnakKosong.visibility = TextView.VISIBLE
                    tvProfilAnakKosong.text = "Belum ada profil anak. Tambahkan profil anak terlebih dahulu."
                } else {
                    tvProfilAnakKosong.visibility = TextView.GONE
                    daftarAnak.forEachIndexed { index, anak ->
                        tambahCardProfilAnak(anak, index)
                    }
                }
            }
            .addOnFailureListener { e ->
                tvProfilAnakKosong.visibility = TextView.VISIBLE
                tvProfilAnakKosong.text = "Gagal memuat profil anak."
            }
    }

    private fun tambahCardTambahAnakMini() {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedDrawable("#FFFFFF", 22, "#E5EEF7")
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()
            layoutParams = LinearLayout.LayoutParams(dp(116), dp(132)).apply { setMargins(0, 0, dp(12), 0) }
            setOnClickListener {
                startActivity(Intent(this@DashboardActivity, TambahAnakActivity::class.java))
            }
        }

        val plus = TextView(this).apply {
            text = "+"
            gravity = Gravity.CENTER
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = circleDrawable("#8DB52A")
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        }

        val label = TextView(this).apply {
            text = "Tambah\nAnak"
            gravity = Gravity.CENTER
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#1F2937"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, dp(10), 0, 0) }
        }

        card.addView(plus)
        card.addView(label)
        containerProfilAnakDashboard.addView(card)
    }

    private fun tambahCardProfilAnak(anak: AnakDashboard, index: Int) {
        val warnaBg = when (index % 4) {
            0 -> "#F4EEFF"
            1 -> "#F0FBEA"
            2 -> "#EAF7FF"
            else -> "#FFF3EA"
        }

        val karakter = when (index % 5) {
            0 -> R.drawable.char_moon_purple
            1 -> R.drawable.char_cucumber
            2 -> R.drawable.char_cloud_blue
            3 -> R.drawable.char_heart
            else -> R.drawable.char_diamond_orange
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedDrawable(warnaBg, 22, "#E5EEF7")
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()
            layoutParams = LinearLayout.LayoutParams(dp(128), dp(132)).apply { setMargins(0, 0, dp(12), 0) }
            setOnClickListener { tampilkanPilihanMenu(anak) }
        }

        val avatar = ImageView(this).apply {
            setImageResource(karakter)
            contentDescription = "Profil ${anak.namaAnak}"
            layoutParams = LinearLayout.LayoutParams(dp(58), dp(58))
        }

        val nama = TextView(this).apply {
            text = anak.namaAnak
            gravity = Gravity.CENTER
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#1F2937"))
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, dp(8), 0, 0) }
        }

        val usia = TextView(this).apply {
            text = "${anak.umur} tahun"
            gravity = Gravity.CENTER
            textSize = 12f
            setTextColor(Color.parseColor("#6B7280"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        card.addView(avatar)
        card.addView(nama)
        card.addView(usia)
        containerProfilAnakDashboard.addView(card)
    }

    private fun tampilkanPilihanMenu(anak: AnakDashboard) {
        val opsi = arrayOf("Masuk Area Anak 🎮", "Lihat Evaluasi AI 🤖")
        AlertDialog.Builder(this)
            .setTitle("Pilih Menu untuk ${anak.namaAnak}")
            .setItems(opsi) { _, index ->
                when (index) {
                    0 -> {
                        val intent = Intent(this, DashboardAnakActivity::class.java)
                        intent.putExtra("ID_ANAK", anak.idDokumen)
                        intent.putExtra("NAMA_ANAK", anak.namaAnak)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this, EvaluasiActivity::class.java)
                        intent.putExtra("ID_ANAK", anak.idDokumen)
                        intent.putExtra("NAMA_ANAK", anak.namaAnak)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun aturAksiTombol() {
        // Asumsi tombol back tidak ada lagi di header dashboard utama
        btnRiwayatPermainan.setOnClickListener { startActivity(Intent(this, RiwayatPermainanActivity::class.java)) }
        btnPengaturanPermainan.setOnClickListener { startActivity(Intent(this, PengaturanPermainanActivity::class.java)) }
        btnLogout.setOnClickListener {
            auth.signOut()
            session.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun roundedDrawable(color: String, radius: Int, strokeColor: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), Color.parseColor(strokeColor))
        }
    }

    private fun circleDrawable(color: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(color))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}