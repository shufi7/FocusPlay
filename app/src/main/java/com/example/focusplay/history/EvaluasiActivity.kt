package com.example.focusplay.history

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardActivity
import com.example.focusplay.dashboard.DashboardAnakActivity
import com.example.focusplay.games.GameDescriptionActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvaluasiActivity : AppCompatActivity() {

    private var idAnak = ""
    private var namaAnak = "Anak"
    private var usiaAnak = 0
    private var namaGame = "Antar Si Domba"
    private var gameKey = "antar_rumah"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluasi)

        ambilData()
        tampilkanHasil()
        aturTombol()
    }

    private fun ambilData() {
        idAnak = intent.getStringExtra("ID_ANAK") ?: ""
        namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak"
        usiaAnak = intent.getIntExtra("USIA_ANAK", 0)
        namaGame = intent.getStringExtra("NAMA_GAME") ?: "Antar Si Domba"
        gameKey = intent.getStringExtra("GAME_KEY") ?: "antar_rumah"
    }

    private fun tampilkanHasil() {
        val skor = intent.getIntExtra("SKOR", 0)
        val akurasi = intent.getIntExtra("AKURASI", 0)
        val durasiDetik = intent.getIntExtra("DURASI_DETIK", 0)
        val fase = intent.getIntExtra("FASE_AKHIR", 1)
        val hasilAI = intent.getStringExtra("EVALUASI_LANGSUNG")

        findViewById<TextView>(R.id.tvJudulHasil).text =
            "${namaAnak.lowercase().replaceFirstChar { it.titlecase() }} selesai bermain."
        findViewById<TextView>(R.id.tvRingkasanHasil).text =
            "Ringkasan sesi $namaGame sudah tersimpan. Gunakan hasil ini untuk melihat skor, akurasi, fase akhir, dan catatan pendampingan."
        findViewById<TextView>(R.id.tvNamaAnakEvaluasi).text = namaAnak
        findViewById<TextView>(R.id.tvInisialAnak).text =
            namaAnak.trim().firstOrNull()?.uppercase() ?: "A"
        findViewById<TextView>(R.id.tvInfoGame).text = "Game       $namaGame"
        findViewById<TextView>(R.id.tvInfoTanggal).text =
            "Tanggal    ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}"

        findViewById<TextView>(R.id.tvEvaluasiSkor).text = skor.toString()
        findViewById<TextView>(R.id.tvEvaluasiAkurasi).text = "$akurasi%"
        findViewById<TextView>(R.id.tvEvaluasiDurasi).text = "${durasiDetik}s"
        findViewById<TextView>(R.id.tvEvaluasiFase).text = fase.toString()

        val fokus = when {
            akurasi >= 80 -> "Baik"
            akurasi >= 60 -> "Cukup"
            else -> "Berlatih"
        }
        val rekomendasi = if (akurasi >= 80 && fase < 3) "Naik" else "Tetap"
        val rekomendasiDetail =
            if (rekomendasi == "Naik") "Coba tantangan berikutnya" else "Pertahankan ritme bermain"

        findViewById<TextView>(R.id.tvFokus).text = fokus
        findViewById<TextView>(R.id.tvRekomendasi).text = rekomendasi
        findViewById<TextView>(R.id.tvRekomendasiDetail).text = rekomendasiDetail

        val loading = findViewById<View>(R.id.layLoadingAI)
        val catatan = findViewById<TextView>(R.id.tvHasilAI)
        loading.visibility = View.GONE
        catatan.visibility = View.VISIBLE
        catatan.text = hasilAI.orEmpty()
    }

    private fun aturTombol() {
        val mainLagi: () -> Unit = {
            startActivity(Intent(this, GameDescriptionActivity::class.java).apply {
                putExtra("ID_ANAK", idAnak)
                putExtra("NAMA_ANAK", namaAnak)
                putExtra("USIA_ANAK", usiaAnak)
                putExtra("GAME_KEY", gameKey)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
            finish()
        }

        findViewById<TextView>(R.id.btnMainGameLagiAtas).jadiTombol(mainLagi)
        findViewById<TextView>(R.id.btnMainLagi).jadiTombol(mainLagi)
        findViewById<TextView>(R.id.btnPilihGameLain).jadiTombol { bukaDashboardAnak() }
        findViewById<TextView>(R.id.btnLihatDashboard).jadiTombol { bukaDashboardOrangTua() }
    }

    private fun bukaDashboardAnak() {
        startActivity(Intent(this, DashboardAnakActivity::class.java).apply {
            putExtra("ID_ANAK", idAnak)
            putExtra("NAMA_ANAK", namaAnak)
            putExtra("USIA_ANAK", usiaAnak)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }

    private fun bukaDashboardOrangTua() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }

    private fun View.jadiTombol(onClick: () -> Unit) {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(45).start()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(55)
                        .withEndAction(onClick).start()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(55).start()
                    true
                }
                else -> true
            }
        }
    }
}
