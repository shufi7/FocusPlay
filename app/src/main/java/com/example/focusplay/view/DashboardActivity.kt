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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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

    private lateinit var tvAiRecapKosong: TextView
    private lateinit var containerAiRecap: LinearLayout

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

    data class RecapAi(
        val namaGame: String,
        val evaluasiAi: String,
        val tanggal: String,
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
        kosongkanRecapAi()
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

        tvAiRecapKosong = findViewById(R.id.tvAiRecapKosong)
        containerAiRecap = findViewById(R.id.containerAiRecap)

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

    private fun kosongkanRecapAi() {
        containerAiRecap.removeAllViews()
        tvAiRecapKosong.visibility = View.VISIBLE
        tvAiRecapKosong.text =
            "Belum ada evaluasi AI. Evaluasi akan muncul setelah anak menyelesaikan permainan."
    }

    private fun ambilProfilAnak() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            selectedAnakId = ""
            selectedNamaAnak = ""

            tvProfilAnakKosong.visibility = View.VISIBLE
            tvProfilAnakKosong.text = "Sesi login tidak ditemukan. Silakan login ulang."

            containerProfilAnakDashboard.removeAllViews()
            kosongkanGrafik()
            kosongkanRecapAi()
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
                    tvProfilAnakKosong.text =
                        "Belum ada profil anak. Tambahkan profil anak terlebih dahulu."

                    kosongkanGrafik()
                    kosongkanRecapAi()
                    return@addOnSuccessListener
                }

                tvProfilAnakKosong.visibility = View.GONE

                val selectedMasihAda = daftarAnak.any { it.idDokumen == selectedAnakId }

                if (selectedAnakId.isEmpty() || !selectedMasihAda) {
                    selectedAnakId = daftarAnak.first().idDokumen
                    selectedNamaAnak = daftarAnak.first().namaAnak
                }

                daftarAnak.forEachIndexed { index, anak ->
                    tambahCardProfilAnak(anak, index)
                }

                muatGrafikAnak(selectedAnakId)
                muatRecapAiAnak(selectedAnakId)
            }
            .addOnFailureListener { e ->
                selectedAnakId = ""
                selectedNamaAnak = ""

                tvProfilAnakKosong.visibility = View.VISIBLE
                tvProfilAnakKosong.text = "Gagal memuat profil anak."

                kosongkanGrafik()
                kosongkanRecapAi()

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

                refreshCardProfil()
                muatGrafikAnak(anak.idDokumen)
                muatRecapAiAnak(anak.idDokumen)
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

    private fun refreshCardProfil() {
        ambilProfilAnak()
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
                    val akurasi = ambilFloat(doc, "akurasi") ?: return@mapNotNull null
                    val timestamp = ambilTimestampMillis(doc)
                    Pair(timestamp, akurasi)
                }.sortedBy { it.first }

                if (daftarSesi.isEmpty()) {
                    kosongkanGrafik()
                    return@addOnSuccessListener
                }

                val formatHari = SimpleDateFormat("dd/MM", Locale("id", "ID"))

                val dataGrafik = daftarSesi
                    .groupBy { formatHari.format(it.first) }
                    .mapValues { item ->
                        item.value.map { it.second }.average().toFloat()
                    }
                    .toList()
                    .takeLast(7)
                    .map { item ->
                        DataGrafikHarian(
                            labelHari = item.first,
                            nilaiAkurasi = item.second
                        )
                    }

                chartWeekly.setData(dataGrafik)
            }
            .addOnFailureListener {
                kosongkanGrafik()
                Toast.makeText(this, "Gagal memuat grafik anak.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun muatRecapAiAnak(idAnak: String) {
        if (idAnak.isEmpty()) {
            kosongkanRecapAi()
            return
        }

        containerAiRecap.removeAllViews()
        tvAiRecapKosong.visibility = View.VISIBLE
        tvAiRecapKosong.text = "Memuat evaluasi AI..."

        db.collection("tb_riwayat")
            .whereEqualTo("id_anak", idAnak)
            .get()
            .addOnSuccessListener { result ->
                val daftarAi = result.documents.mapNotNull { doc ->
                    val evaluasi = doc.getString("evaluasi_ai")?.trim().orEmpty()

                    if (evaluasi.isEmpty()) return@mapNotNull null

                    val timestamp = ambilTimestampMillis(doc)

                    RecapAi(
                        namaGame = doc.getString("nama_game") ?: "Permainan",
                        evaluasiAi = evaluasi,
                        tanggal = formatTanggal(timestamp, doc.getString("tanggal")),
                        timestampMillis = timestamp
                    )
                }.sortedByDescending { it.timestampMillis }

                if (daftarAi.isEmpty()) {
                    tvAiRecapKosong.visibility = View.VISIBLE
                    tvAiRecapKosong.text =
                        "Belum ada evaluasi AI untuk $selectedNamaAnak. Evaluasi akan muncul setelah anak menyelesaikan permainan."
                    return@addOnSuccessListener
                }

                tvAiRecapKosong.visibility = View.GONE
                containerAiRecap.removeAllViews()

                daftarAi.forEachIndexed { index, recap ->
                    tambahCardRecapAi(recap, index)
                }
            }
            .addOnFailureListener { e ->
                containerAiRecap.removeAllViews()
                tvAiRecapKosong.visibility = View.VISIBLE
                tvAiRecapKosong.text = "Gagal memuat evaluasi AI."

                Toast.makeText(
                    this,
                    "Gagal memuat evaluasi AI: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun tambahCardRecapAi(recap: RecapAi, index: Int) {
        val warnaBg = when (index % 4) {
            0 -> "#F4EEFF"
            1 -> "#F0FBEA"
            2 -> "#EAF7FF"
            else -> "#FFF8E8"
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedDrawable(warnaBg, 20, "#E5EEF7")
            elevation = dp(1).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                dp(280),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, dp(12), 0)
            }
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = "AI"
            gravity = Gravity.CENTER
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = circleDrawable("#8DB52A")

            layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
        }

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dp(12), 0, 0, 0)
            }
        }

        val tvGame = TextView(this).apply {
            text = recap.namaGame
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#1F2937"))
        }

        val tvTanggal = TextView(this).apply {
            text = recap.tanggal
            textSize = 12f
            setTextColor(Color.parseColor("#6B7280"))
            setPadding(0, dp(3), 0, 0)
        }

        titleBox.addView(tvGame)
        titleBox.addView(tvTanggal)

        header.addView(icon)
        header.addView(titleBox)

        val tvEvaluasi = TextView(this).apply {
            text = recap.evaluasiAi
            textSize = 13f
            setTextColor(Color.parseColor("#374151"))
            setLineSpacing(4f, 1f)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(12), 0, 0)
            }
        }

        card.addView(header)
        card.addView(tvEvaluasi)

        containerAiRecap.addView(card)
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

            val intent = Intent(this, com.example.focusplay.view.RiwayatPermainanActivity::class.java)
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

            val intent = Intent(this, AuthChoiceActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun ambilTimestampMillis(doc: DocumentSnapshot): Long {
        return when (val value = doc.get("timestamp")) {
            is com.google.firebase.Timestamp -> value.toDate().time
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun formatTanggal(timestampMillis: Long, fallback: String?): String {
        return if (timestampMillis > 0) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(timestampMillis)
        } else {
            fallback ?: "-"
        }
    }

    private fun ambilFloat(doc: DocumentSnapshot, field: String): Float? {
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