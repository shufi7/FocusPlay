package com.example.focusplay.dashboard

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.focusplay.utils.SessionManager
import com.example.focusplay.view.AuthChoiceActivity
import com.example.focusplay.settings.PengaturanPermainanActivity
import com.example.focusplay.history.RiwayatPermainanActivity
import com.example.focusplay.profile.TambahAnakActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.focusplay.utils.DashboardTutorialOverlay

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivBackDashboard: ImageView
    private lateinit var tvProfilAnakKosong: TextView
    private lateinit var containerProfilAnakDashboard: LinearLayout

    private lateinit var tvAiRecapKosong: TextView
    private lateinit var containerAiRecap: LinearLayout

    private lateinit var btnRiwayatPermainan: View
    private lateinit var btnPengaturanPermainan: View
    private lateinit var btnLogout: ImageView

    private lateinit var chartWeekly: LineChart

    private var selectedAnakId: String = ""
    private var selectedNamaAnak: String = ""
    private val daftarAnakDashboard = mutableListOf<AnakDashboard>()
    private lateinit var cardProfilAnak: View
    private lateinit var cardGrafikDashboard: View
    private lateinit var cardRecapAi: View


    data class AnakDashboard(
        val idDokumen: String,
        val namaAnak: String,
        val usia: Int,
        val avatar: String
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
        konfigurasiGrafikPlugin()
        kosongkanGrafik()
        kosongkanRecapAi()
        aturAksiTombol()

        tampilkanTutorialDashboardJikaPertama()
    }

    override fun onResume() {
        super.onResume()
        ambilProfilAnak()
    }

    private fun hubungkanView() {
        ivBackDashboard = findViewById(R.id.ivBackDashboard)

        tvProfilAnakKosong = findViewById(R.id.tvProfilAnakKosong)
        containerProfilAnakDashboard = findViewById(R.id.containerProfilAnakDashboard)

        cardProfilAnak = findViewById(R.id.cardProfilAnak)
        cardGrafikDashboard = findViewById(R.id.cardGrafikDashboard)
        cardRecapAi = findViewById(R.id.cardRecapAi)

        tvAiRecapKosong = findViewById(R.id.tvAiRecapKosong)
        containerAiRecap = findViewById(R.id.containerAiRecap)

        btnRiwayatPermainan = findViewById(R.id.btnRiwayatPermainan)
        btnPengaturanPermainan = findViewById(R.id.btnPengaturanPermainan)
        btnLogout = findViewById(R.id.btnLogout)

        chartWeekly = findViewById(R.id.chartWeekly)
    }

    private fun tampilkanTutorialDashboardJikaPertama() {
        val prefs = getSharedPreferences("tutorial_dashboard", MODE_PRIVATE)
        val sudahTampil = prefs.getBoolean("sudah_tampil_spotlight", false)

        if (sudahTampil) return

        window.decorView.post {
            val overlay = DashboardTutorialOverlay(this)

            overlay.setSteps(
                listOf(
                    DashboardTutorialOverlay.TutorialStep(
                        target = cardProfilAnak,
                        title = "Profil Anak",
                        message = "Pilih profil anak yang ingin dipantau. Grafik, riwayat, dan evaluasi AI akan berubah sesuai anak yang dipilih."
                    ),
                    DashboardTutorialOverlay.TutorialStep(
                        target = cardGrafikDashboard,
                        title = "Grafik Perkembangan",
                        message = "Bagian ini menampilkan perkembangan akurasi anak dari hasil sesi bermain yang sudah tersimpan."
                    ),
                    DashboardTutorialOverlay.TutorialStep(
                        target = cardRecapAi,
                        title = "Recap Evaluasi AI",
                        message = "Di sini orang tua bisa melihat ringkasan analisis AI dari setiap sesi bermain. Geser ke samping untuk melihat semua evaluasi."
                    ),
                    DashboardTutorialOverlay.TutorialStep(
                        target = btnRiwayatPermainan,
                        title = "Riwayat Permainan",
                        message = "Menu ini menampilkan daftar sesi bermain anak, seperti nama game, skor, akurasi, durasi, dan tanggal bermain."
                    ),
                    DashboardTutorialOverlay.TutorialStep(
                        target = btnPengaturanPermainan,
                        title = "Pengaturan Permainan",
                        message = "Menu ini digunakan untuk mengatur target waktu bermain dan mode adaptif."
                    ),
                    DashboardTutorialOverlay.TutorialStep(
                        target = btnLogout,
                        title = "Logout",
                        message = "Gunakan tombol ini untuk keluar dari akun orang tua dan kembali ke halaman login."
                    )
                )
            )

            overlay.start()
        }
    }

    private fun konfigurasiGrafikPlugin() {
        chartWeekly.description.isEnabled = false
        chartWeekly.legend.isEnabled = false
        chartWeekly.axisRight.isEnabled = false
        chartWeekly.setNoDataText("Belum ada data sesi bermain")
        chartWeekly.setTouchEnabled(true)
        chartWeekly.setDragEnabled(false)
        chartWeekly.setScaleEnabled(false)
        chartWeekly.setPinchZoom(false)
        chartWeekly.setExtraOffsets(dp(8).toFloat(), dp(8).toFloat(), dp(18).toFloat(), dp(10).toFloat())

        chartWeekly.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = Color.parseColor("#6B7C8F")
            textSize = 10f
            yOffset = 8f
            valueFormatter = IndexAxisValueFormatter(emptyList<String>())
        }

        chartWeekly.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            setLabelCount(6, true)
            setDrawAxisLine(false)
            gridColor = Color.parseColor("#D8E5F5")
            textColor = Color.parseColor("#6B7C8F")
            textSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
    }

    private fun kosongkanGrafik() {
        chartWeekly.clear()
        chartWeekly.xAxis.valueFormatter = IndexAxisValueFormatter(emptyList<String>())
        chartWeekly.invalidate()
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
                        usia = usia,
                        avatar = doc.getString("avatar") ?: "char_red"
                    )
                }

                daftarAnakDashboard.clear()
                daftarAnakDashboard.addAll(daftarAnak)

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

                renderCardProfilAnak()

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

    private fun renderCardProfilAnak() {
        containerProfilAnakDashboard.removeAllViews()
        tambahCardTambahAnakMini()

        daftarAnakDashboard.forEach { anak ->
            tambahCardProfilAnak(anak)
        }
    }

    private fun tambahCardTambahAnakMini() {
        val outerCard = FrameLayout(this).apply {
            isClickable = true
            isFocusable = true
            elevation = dp(3).toFloat()
            background = roundedDrawable("#FFFFFF", 28, "#DDEBFF", 1.5f)

            layoutParams = LinearLayout.LayoutParams(dp(132), dp(132)).apply {
                setMargins(dp(12), dp(8), dp(12), dp(8))
            }

            setOnClickListener {
                startActivity(Intent(this@DashboardActivity, TambahAnakActivity::class.java))
            }
        }

        // Dekorasi Bulat Organik (Blue for consistence)
        val circle1 = View(this).apply {
            background = circleDrawable("#E3F2FD")
            alpha = 0.3f
            layoutParams = FrameLayout.LayoutParams(dp(80), dp(80)).apply {
                gravity = Gravity.END or Gravity.TOP
                setMargins(0, dp(-35), dp(-35), 0)
            }
        }
        val circle2 = View(this).apply {
            background = circleDrawable("#EAF7FF")
            alpha = 0.2f
            layoutParams = FrameLayout.LayoutParams(dp(60), dp(60)).apply {
                gravity = Gravity.START or Gravity.BOTTOM
                setMargins(dp(-25), 0, 0, dp(-25))
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val plus = TextView(this).apply {
            text = "+"
            gravity = Gravity.CENTER
            textSize = 28f
            setTextColor(Color.parseColor("#5E7FE0"))
            typeface = Typeface.DEFAULT_BOLD
            background = roundedDrawable("#F2FAFF", 18, "#E3F2FD")
            includeFontPadding = false

            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
        }

        val label = TextView(this).apply {
            text = "Tambah\nAnak"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#1F2937"))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(12), 0, 0)
            }
        }

        content.addView(plus)
        content.addView(label)
        
        outerCard.addView(circle1)
        outerCard.addView(circle2)
        outerCard.addView(content)
        
        containerProfilAnakDashboard.addView(outerCard)
    }

    private fun getAvatarResource(avatar: String): Int {
        return when (avatar) {
            "char_red" -> R.drawable.char_red
            "char_blue" -> R.drawable.char_blue
            "char_purple" -> R.drawable.char_purple
            "char_star" -> R.drawable.char_star

            // Cadangan untuk data lama yang masih memakai nama avatar lama
            "char_moon_purple" -> R.drawable.char_moon_purple
            "char_cucumber" -> R.drawable.char_cucumber
            "char_cloud_blue" -> R.drawable.char_cloud_blue
            "char_heart" -> R.drawable.char_heart
            "char_diamond_orange" -> R.drawable.char_diamond_orange

            else -> R.drawable.char_red
        }
    }
    private fun tambahCardProfilAnak(anak: AnakDashboard) {
        val sedangDipilih = anak.idDokumen == selectedAnakId
        val karakter = getAvatarResource(anak.avatar)

        val outerCard = FrameLayout(this).apply {
            isClickable = true
            isFocusable = true
            elevation = dp(3).toFloat()
            background = roundedDrawable(
                "#FFFFFF",
                28,
                if (sedangDipilih) "#5E7FE0" else "#DDEBFF",
                if (sedangDipilih) 2f else 1.5f
            )

            layoutParams = LinearLayout.LayoutParams(dp(132), dp(132)).apply {
                setMargins(0, dp(8), dp(12), dp(8))
            }

            setOnClickListener {
                if (selectedAnakId == anak.idDokumen) return@setOnClickListener

                selectedAnakId = anak.idDokumen
                selectedNamaAnak = anak.namaAnak

                renderCardProfilAnak()
                muatGrafikAnak(anak.idDokumen)
                muatRecapAiAnak(anak.idDokumen)
            }
            setOnLongClickListener {
                tampilkanDialogHapusAnak(anak)
                true
            }
        }

        // Dekorasi Bulat Organik (Blue for Profile Cards)
        val circle1 = View(this).apply {
            background = circleDrawable("#E3F2FD")
            alpha = 0.3f
            layoutParams = FrameLayout.LayoutParams(dp(80), dp(80)).apply {
                gravity = Gravity.END or Gravity.TOP
                setMargins(0, dp(-35), dp(-35), 0)
            }
        }
        val circle2 = View(this).apply {
            background = circleDrawable("#EAF7FF")
            alpha = 0.2f
            layoutParams = FrameLayout.LayoutParams(dp(60), dp(60)).apply {
                gravity = Gravity.START or Gravity.BOTTOM
                setMargins(dp(-25), 0, 0, dp(-25))
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val avatar = ImageView(this).apply {
            setImageResource(karakter)
            contentDescription = "Profil ${anak.namaAnak}"
            background = roundedDrawable("#F2FAFF", 18, "#E3F2FD")
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(dp(62), dp(62))
        }

        val nama = TextView(this).apply {
            text = anak.namaAnak
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
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
            text = if (sedangDipilih) "${anak.usia} tahun - dipilih" else "${anak.usia} tahun"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(if (sedangDipilih) Color.parseColor("#5E7FE0") else Color.parseColor("#6B7280"))
            background = roundedDrawable(if (sedangDipilih) "#EEF4FF" else "#F3F4F6", 16, if (sedangDipilih) "#DDEBFF" else "#E5E7EB")
            setPadding(dp(10), dp(4), dp(10), dp(4))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(6), 0, 0)
            }
        }

        content.addView(avatar)
        content.addView(nama)
        content.addView(usia)

        outerCard.addView(circle1)
        outerCard.addView(circle2)
        outerCard.addView(content)

        containerProfilAnakDashboard.addView(outerCard)
    }

    private fun tampilkanDialogHapusAnak(anak: AnakDashboard) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(18))
            background = roundedDrawable("#FFFFFF", 26, "#D8E5F5")
        }

        val icon = TextView(this).apply {
            text = "!"
            gravity = Gravity.CENTER
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = circleDrawable("#E95A6A")

            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        val title = TextView(this).apply {
            text = "Hapus Profil Anak?"
            gravity = Gravity.CENTER
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#263342"))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(14), 0, 0)
            }
        }

        val message = TextView(this).apply {
            text = "Profil ${anak.namaAnak} akan dihapus dari daftar anak. Data profil ini tidak akan tampil lagi di dashboard."
            gravity = Gravity.CENTER
            textSize = 13.5f
            setTextColor(Color.parseColor("#6B7C8F"))
            setLineSpacing(4f, 1f)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(8), 0, 0)
            }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(20), 0, 0)
            }
        }

        val btnBatal = TextView(this).apply {
            text = "Batal"
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6B7C8F"))
            background = roundedDrawable("#F2FAFF", 18, "#D8E5F5")

            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                setMargins(0, 0, dp(8), 0)
            }
        }

        val btnHapus = TextView(this).apply {
            text = "Hapus"
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = roundedDrawable("#E95A6A", 18, "#E95A6A")

            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                setMargins(dp(8), 0, 0, 0)
            }
        }

        buttonRow.addView(btnBatal)
        buttonRow.addView(btnHapus)

        container.addView(icon)
        container.addView(title)
        container.addView(message)
        container.addView(buttonRow)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(container)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnHapus.setOnClickListener {
            btnHapus.text = "Menghapus..."
            btnHapus.isEnabled = false
            hapusProfilAnakTanpaReload(anak, dialog)
        }

        dialog.show()
    }

    private fun hapusProfilAnakTanpaReload(
        anak: AnakDashboard,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        db.collection("tb_anak")
            .document(anak.idDokumen)
            .delete()
            .addOnSuccessListener {
                dialog.dismiss()

                daftarAnakDashboard.removeAll { it.idDokumen == anak.idDokumen }

                if (selectedAnakId == anak.idDokumen) {
                    if (daftarAnakDashboard.isNotEmpty()) {
                        selectedAnakId = daftarAnakDashboard.first().idDokumen
                        selectedNamaAnak = daftarAnakDashboard.first().namaAnak

                        muatGrafikAnak(selectedAnakId)
                        muatRecapAiAnak(selectedAnakId)
                    } else {
                        selectedAnakId = ""
                        selectedNamaAnak = ""

                        tvProfilAnakKosong.visibility = View.VISIBLE
                        tvProfilAnakKosong.text =
                            "Belum ada profil anak. Tambahkan profil anak terlebih dahulu."

                        kosongkanGrafik()
                        kosongkanRecapAi()
                    }
                }

                renderCardProfilAnak()
                tampilkanToastProfil("Profil ${anak.namaAnak} dihapus")
            }
            .addOnFailureListener { e ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Gagal menghapus profil: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
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

                    if (timestamp <= 0L) return@mapNotNull null

                    Pair(timestamp, akurasi.coerceIn(0f, 100f))
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

                tampilkanGrafikPlugin(dataGrafik)
            }
            .addOnFailureListener {
                kosongkanGrafik()
                Toast.makeText(this, "Gagal memuat grafik anak.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun tampilkanGrafikPlugin(dataGrafik: List<Pair<String, Float>>) {
        if (dataGrafik.isEmpty()) {
            kosongkanGrafik()
            return
        }

        val labels = dataGrafik.map { it.first }
        val entries = dataGrafik.mapIndexed { index, item ->
            Entry(index.toFloat(), item.second.coerceIn(0f, 100f))
        }

        val dataSet = LineDataSet(entries, "Akurasi").apply {
            color = Color.parseColor("#5E7FE0")
            setCircleColor(Color.parseColor("#5E7FE0"))
            lineWidth = 3f
            circleRadius = 4.5f
            circleHoleRadius = 2.2f
            setDrawCircleHole(true)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#DDEBFF")
            setFillAlpha(120)
            highLightColor = Color.parseColor("#456ECF")
        }

        chartWeekly.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            setLabelCount(labels.size, false)
            axisMinimum = -0.15f
            axisMaximum = (labels.size - 1).coerceAtLeast(0).toFloat() + 0.15f
        }

        chartWeekly.data = LineData(dataSet)
        chartWeekly.animateX(500)
        chartWeekly.invalidate()
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
        val warnaBorder = if (index % 2 == 0) "#CFE0FF" else "#D8EAFE"
        val warnaIcon = if (index % 2 == 0) "#5E7FE0" else "#4DA3D9"
        val warnaChip = if (index % 2 == 0) "#EEF4FF" else "#EAF7FF"

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = roundedDrawable("#FFFFFF", 22, warnaBorder)
            elevation = dp(2).toFloat()

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
            background = circleDrawable(warnaIcon)
            includeFontPadding = false

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
            setTextColor(Color.parseColor("#263342"))
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }

        val tvTanggal = TextView(this).apply {
            text = recap.tanggal
            textSize = 11.5f
            setTextColor(Color.parseColor("#6B7C8F"))
            setPadding(0, dp(3), 0, 0)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }

        titleBox.addView(tvGame)
        titleBox.addView(tvTanggal)

        header.addView(icon)
        header.addView(titleBox)

        val labelFeedback = TextView(this).apply {
            text = "Feedback permainan"
            textSize = 11.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#5E7FE0"))
            background = roundedDrawable(warnaChip, 16, "#D8E5F5")
            setPadding(dp(10), dp(5), dp(10), dp(5))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(13), 0, 0)
            }
        }

        val boxEvaluasi = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(12), dp(13), dp(12))
            background = roundedDrawable("#F6FAFF", 18, "#E1ECFA")

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(10), 0, 0)
            }
        }

        val tvEvaluasi = TextView(this).apply {
            text = recap.evaluasiAi
            textSize = 12.5f
            setTextColor(Color.parseColor("#374151"))
            setLineSpacing(4f, 1f)
            maxLines = 7
            ellipsize = TextUtils.TruncateAt.END

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        boxEvaluasi.addView(tvEvaluasi)

        card.addView(header)
        card.addView(labelFeedback)
        card.addView(boxEvaluasi)

        containerAiRecap.addView(card)
    }

    private fun tampilkanToastProfil(pesan: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = roundedDrawable("#F2FAFF", 18, "#D8E5F5")
            elevation = dp(4).toFloat()
        }

        val text = TextView(this).apply {
            this.text = pesan
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#263342"))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(0), 0, 0, 0)
            }
        }

        layout.addView(text)

        val toast = Toast(this)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(90))
        toast.show()
    }

    private fun aturAksiTombol() {
        ivBackDashboard.setOnClickListener {
            finish()
        }

        btnRiwayatPermainan.jadiTombolCepat {
            if (selectedAnakId.isEmpty()) {
                Toast.makeText(this, "Pilih profil anak terlebih dahulu.", Toast.LENGTH_SHORT).show()
                return@jadiTombolCepat
            }

            val intent = Intent(this, RiwayatPermainanActivity::class.java)
            intent.putExtra("ID_ANAK", selectedAnakId)
            intent.putExtra("NAMA_ANAK", selectedNamaAnak)
            startActivity(intent)
        }

        btnPengaturanPermainan.jadiTombolCepat {
            startActivity(Intent(this, PengaturanPermainanActivity::class.java))
        }

        btnLogout.jadiTombolCepat {
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
            is Timestamp -> value.toDate().time
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

    private fun roundedDrawable(color: String, radius: Int, strokeColor: String, strokeWidth: Float = 1f): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(strokeWidth.toInt()), Color.parseColor(strokeColor))
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

    private fun View.jadiTombolCepat(onClick: () -> Unit) {
        isClickable = true
        isFocusable = true

        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .alpha(0.85f)
                        .setDuration(40)
                        .start()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(50)
                        .withEndAction {
                            onClick()
                        }
                        .start()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(50)
                        .start()
                    true
                }

                else -> false
            }
        }
    }
}
