package com.example.focusplay.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

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

    private var selectedAnakId: String = ""
    private var selectedNamaAnak: String = ""

    data class AnakDashboard(
        val idDokumen: String,
        val namaAnak: String,
        val usia: Int
    )

    data class RiwayatSesi(
        val idAnak: String,
        val namaAnak: String,
        val namaGame: String,
        val skor: Int,
        val akurasi: Float,
        val durasiMenit: Int,
        val tanggalLabel: String,
        val timestampMillis: Long
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        hubungkanView()
        tampilkanNamaUser()
        kosongkanGrafik()
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

    private fun kosongkanGrafik() {
        chartWeekly.setData(emptyList())
    }

    private fun ambilProfilAnak() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            tvProfilAnakKosong.visibility = View.VISIBLE
            tvProfilAnakKosong.text = "Sesi login tidak ditemukan. Silakan login ulang."
            containerProfilAnakDashboard.removeAllViews()
            kosongkanGrafik()
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
                    val usia = doc.getLong("usia")?.toInt() ?: 0

                    AnakDashboard(
                        idDokumen = doc.id,
                        namaAnak = nama,
                        usia = usia
                    )
                }

                if (daftarAnak.isEmpty()) {
                    selectedAnakId = ""
                    selectedNamaAnak = ""
                    tvProfilAnakKosong.visibility = View.VISIBLE
                    tvProfilAnakKosong.text = "Belum ada profil anak. Tambahkan profil anak terlebih dahulu."
                    kosongkanGrafik()
                    return@addOnSuccessListener
                }

                tvProfilAnakKosong.visibility = View.GONE

                if (selectedAnakId.isEmpty()) {
                    selectedAnakId = daftarAnak.first().idDokumen
                    selectedNamaAnak = daftarAnak.first().namaAnak
                }

                daftarAnak.forEachIndexed { index, anak ->
                    tambahCardProfilAnak(anak, index)
                }

                muatGrafikAnak(selectedAnakId)
            }
            .addOnFailureListener { e ->
                tvProfilAnakKosong.visibility = View.VISIBLE
                tvProfilAnakKosong.text = "Gagal memuat profil anak."
                kosongkanGrafik()
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
        val sedangDipilih = anak.idDokumen == selectedAnakId

        val warnaBg = if (sedangDipilih) {
            "#F0FBEA"
        } else {
            when (index % 4) {
                0 -> "#F4EEFF"
                1 -> "#F0FBEA"
                2 -> "#EAF7FF"
                else -> "#FFF3EA"
            }
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
            background = roundedDrawable(
                warnaBg,
                22,
                if (sedangDipilih) "#8DB52A" else "#E5EEF7"
            )
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()

            layoutParams = LinearLayout.LayoutParams(dp(128), dp(132)).apply {
                setMargins(0, 0, dp(12), 0)
            }

            setOnClickListener {
                selectedAnakId = anak.idDokumen
                selectedNamaAnak = anak.namaAnak

                Toast.makeText(
                    this@DashboardActivity,
                    "Menampilkan data ${anak.namaAnak}",
                    Toast.LENGTH_SHORT
                ).show()

                ambilProfilAnak()
                muatGrafikAnak(anak.idDokumen)
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
            text = if (sedangDipilih) {
                "${anak.usia} tahun • dipilih"
            } else {
                "${anak.usia} tahun"
            }
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

    private fun muatGrafikAnak(idAnak: String) {
        if (idAnak.isEmpty()) {
            kosongkanGrafik()
            return
        }

        db.collection("tb_riwayat")
            .whereEqualTo("id_anak", idAnak)
            .get()
            .addOnSuccessListener { result ->
                val daftarSesi = result.documents.mapNotNull { doc ->
                    val akurasi = ambilFloat(doc, "akurasi")
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time
                        ?: doc.getLong("timestamp")
                        ?: 0L

                    if (akurasi == null) return@mapNotNull null

                    Pair(timestamp, akurasi)
                }

                if (daftarSesi.isEmpty()) {
                    kosongkanGrafik()
                    return@addOnSuccessListener
                }

                val formatHari = SimpleDateFormat("dd/MM", Locale("id", "ID"))

                val rataAkurasiPerHari = daftarSesi
                    .groupBy { formatHari.format(it.first) }
                    .mapValues { item ->
                        item.value.map { it.second }.average().toFloat()
                    }
                    .toList()
                    .takeLast(7)
                    .map { it.second }

                setDataGrafik(rataAkurasiPerHari)
            }
            .addOnFailureListener {
                kosongkanGrafik()
                Toast.makeText(this, "Gagal memuat grafik anak.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setDataGrafik(dataAkurasi: List<Float>) {
        try {
            val method = chartWeekly.javaClass.getMethod("setData", List::class.java)
            method.invoke(chartWeekly, dataAkurasi)
        } catch (e: Exception) {
            chartWeekly.setData(emptyList())
        }
    }

    private fun aturAksiTombol() {
        ivBackDashboard.setOnClickListener {
            finish()
        }

        btnRiwayatPermainan.setOnClickListener {
            if (selectedAnakId.isEmpty()) {
                Toast.makeText(this, "Pilih profil anak terlebih dahulu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, RiwayatPermainanActivity::class.java)
            intent.putExtra("ID_ANAK", selectedAnakId)
            intent.putExtra("NAMA_ANAK", selectedNamaAnak)
            startActivity(intent)
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

    private fun ambilFloat(doc: com.google.firebase.firestore.DocumentSnapshot, field: String): Float? {
        return when (val value = doc.get(field)) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull()
            else -> null
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