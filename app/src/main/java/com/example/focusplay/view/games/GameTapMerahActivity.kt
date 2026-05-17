package com.example.focusplay.view.games

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlin.random.Random

class GameTapMerahActivity : AppCompatActivity() {

    private lateinit var tvSkor: TextView
    private lateinit var tvWaktu: TextView
    private lateinit var viewTargetMerah: View
    private lateinit var viewTargetBiru: View
    private lateinit var layStatus: View

    private var skor = 0
    private var timer: CountDownTimer? = null
    private var isGameRunning = false
    private var idAnak = "" // Tempat menyimpan ID Anak

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gerakOtomatis: Runnable

    private var kecepatanLompat: Long = 1500
    private val kecepatanMaksimal: Long = 600

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tap_merah)

        // Tangkap ID Anak yang dikirim oleh Dashboard Anak
        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        viewTargetMerah = findViewById(R.id.viewTargetMerah)
        viewTargetBiru = findViewById(R.id.viewTargetBiru)
        layStatus = findViewById(R.id.layStatus)

        gerakOtomatis = Runnable {
            if (isGameRunning) {
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        viewTargetMerah.setOnClickListener {
            if (isGameRunning) {
                skor++
                tvSkor.text = "Skor: $skor"
                if (kecepatanLompat > kecepatanMaksimal) {
                    kecepatanLompat -= 30
                }
                handler.removeCallbacks(gerakOtomatis)
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        viewTargetBiru.setOnClickListener {
            if (isGameRunning) {
                skor--
                tvSkor.text = "Skor: $skor"
                handler.removeCallbacks(gerakOtomatis)
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        mulaiPermainan()
    }

    private fun mulaiPermainan() {
        skor = 0
        kecepatanLompat = 1500
        tvSkor.text = "Skor: 0"
        isGameRunning = true

        handler.postDelayed(gerakOtomatis, kecepatanLompat)

        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvWaktu.text = "Sisa: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isGameRunning = false
                tvWaktu.text = "Waktu Habis!"
                viewTargetMerah.visibility = View.GONE
                viewTargetBiru.visibility = View.GONE

                handler.removeCallbacks(gerakOtomatis)

                // JALANKAN FUNGSI SIMPAN SKOR KE FIRESTORE SEBELUM KELUAR
                simpanSkorKeFirebase()
            }
        }.start()
    }

    private fun simpanSkorKeFirebase() {
        if (idAnak.isEmpty()) {
            Toast.makeText(this, "Gagal menyimpan: ID Anak tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Siapkan cetakan bungkus data skor
        val dataSkor = hashMapOf(
            "id_anak" to idAnak,
            "nama_game" to "Tap si Merah",
            "skor" to skor,
            "tanggal_main" to Date() // Menyimpan waktu real-time saat ini
        )

        // Tembak ke koleksi "tb_riwayat_game"
        db.collection("tb_riwayat_game")
            .add(dataSkor)
            .addOnSuccessListener {
                Toast.makeText(this, "Skor game berhasil dicatat di Cloud Firebase!", Toast.LENGTH_SHORT).show()
                finish() // Menutup game otomatis dan kembali ke halaman anak
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mencatat skor: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun pindahkanTargetSecaraAcak() {
        val rootLayout = findViewById<View>(android.R.id.content)
        val batasKanan = rootLayout.width - viewTargetMerah.width
        val batasBawah = rootLayout.height - viewTargetMerah.height - layStatus.height

        if (batasKanan > 0 && batasBawah > 0) {
            val merahX = Random.nextInt(0, batasKanan).toFloat()
            val merahY = Random.nextInt(layStatus.height, batasBawah + layStatus.height).toFloat()
            val biruX = Random.nextInt(0, batasKanan).toFloat()
            val biruY = Random.nextInt(layStatus.height, batasBawah + layStatus.height).toFloat()

            viewTargetMerah.x = merahX
            viewTargetMerah.y = merahY
            viewTargetBiru.x = biruX
            viewTargetBiru.y = biruY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        handler.removeCallbacks(gerakOtomatis)
    }
}