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

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivBackDashboard: ImageView
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
        val umur: Int,
        val avatar: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        hubungkanView()
        tampilkanNamaUser()
        tampilkanGrafikSesi()
        aturAksiTombol()
    }

    override fun onResume() {
        super.onResume()
        ambilProfilAnak()
    }

    private fun hubungkanView() {
        ivBackDashboard = findViewById(R.id.ivBackDashboard)
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

    private fun tampilkanGrafikSesi() {
        chartWeekly.setData(emptyList())
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
                    val avatar = doc.getString("avatar") ?: "char_red"
                    // --- SOLUSI SAKTI PENGAMBILAN UMUR ---
                    // Coba ambil "umur", kalau tidak ada coba "usia".
                    // Ubah jadi String dulu apa pun bentuknya, baru dipaksa jadi Angka (Int).
                    val umurMentah = doc.get("umur") ?: doc.get("usia")
                    val umur = umurMentah?.toString()?.toIntOrNull() ?: 0

                    AnakDashboard(
                        idDokumen = doc.id,
                        namaAnak = nama,
                        umur = umur,
                        avatar = avatar
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
                Toast.makeText(this, "Gagal memuat anak: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun tambahCardTambahAnakMini() {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedDrawable("#FFF8E8", 22, "#FFE2A8")
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()

            layoutParams = LinearLayout.LayoutParams(dp(116), dp(132)).apply {
                setMargins(0, 0, dp(12), 0)
            }

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

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(10), 0, 0)
            }
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

        val karakter = when (anak.avatar) {
            "char_blue" -> R.drawable.char_blue
            "char_purple" -> R.drawable.char_purple
            "char_star" -> R.drawable.char_star
            "char_moon_purple" -> R.drawable.char_moon_purple
            "char_cucumber" -> R.drawable.char_cucumber
            "char_cloud_blue" -> R.drawable.char_cloud_blue
            "char_heart" -> R.drawable.char_heart
            "char_diamond_orange" -> R.drawable.char_diamond_orange
            else -> R.drawable.char_red
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedDrawable(warnaBg, 22, "#E5EEF7")
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()

            layoutParams = LinearLayout.LayoutParams(dp(128), dp(132)).apply {
                setMargins(0, 0, dp(12), 0)
            }

            setOnClickListener {
                tampilkanPilihanMenu(anak)
            }
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

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(8), 0, 0)
            }
        }

        val usia = TextView(this).apply {
            text = "${anak.umur} tahun"
            gravity = Gravity.CENTER
            textSize = 12f
            setTextColor(Color.parseColor("#6B7280"))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
        ivBackDashboard.setOnClickListener {
            finish()
        }

        btnRiwayatPermainan.setOnClickListener {
            startActivity(Intent(this, RiwayatPermainanActivity::class.java))
        }

        btnPengaturanPermainan.setOnClickListener {
            startActivity(Intent(this, PengaturanPermainanActivity::class.java))
        }

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