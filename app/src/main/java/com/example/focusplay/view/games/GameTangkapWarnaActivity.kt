package com.example.focusplay.view.games

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class GameTangkapWarnaActivity : AppCompatActivity() {

    private lateinit var tvSkor: TextView
    private lateinit var tvWaktu: TextView
    private lateinit var tvTargetWarna: TextView
    private lateinit var gridLayoutWarna: GridLayout

    private var skor = 0
    private var timer: CountDownTimer? = null
    private var isGameRunning = false
    private var warnaTargetSaatIni: String = ""
    private var idAnak = ""

    private val kamusWarna = mapOf(
        "MERAH" to "#F44336", "BIRU" to "#2196F3", "HIJAU" to "#4CAF50",
        "KUNING" to "#FFEB3B", "UNGU" to "#9C27B0", "ORANYE" to "#FF9800",
        "COKELAT" to "#795548", "HITAM" to "#212121"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tangkap_warna)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        tvTargetWarna = findViewById(R.id.tvTargetWarna)
        gridLayoutWarna = findViewById(R.id.gridLayoutWarna)

        mulaiPermainan()
    }

    private fun mulaiPermainan() {
        skor = 0
        tvSkor.text = "Skor: 0"
        isGameRunning = true
        buatSoalBaru()

        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvWaktu.text = "Sisa: ${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                isGameRunning = false
                tvWaktu.text = "Waktu Habis!"
                gridLayoutWarna.visibility = View.GONE
                tvTargetWarna.text = "Selesai!"

                simpanSkorKeFirebase("Tangkap Warna", skor)
            }
        }.start()
    }

    private fun buatSoalBaru() {
        gridLayoutWarna.removeAllViews()
        val empatWarnaAcak = kamusWarna.keys.shuffled().take(4)
        warnaTargetSaatIni = empatWarnaAcak.random()
        tvTargetWarna.text = warnaTargetSaatIni

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val totalRuangKosong = (64 * displayMetrics.density).toInt()
        val ukuranKotak = (screenWidth - totalRuangKosong) / 2
        val marginKotak = (8 * displayMetrics.density).toInt()

        for (namaWarna in empatWarnaAcak) {
            val kotak = View(this)
            val params = GridLayout.LayoutParams()
            params.width = ukuranKotak
            params.height = ukuranKotak
            params.setMargins(marginKotak, marginKotak, marginKotak, marginKotak)
            params.setGravity(Gravity.CENTER)
            kotak.layoutParams = params

            val kodeHex = kamusWarna[namaWarna]
            kotak.setBackgroundColor(Color.parseColor(kodeHex))
            kotak.elevation = 6f

            kotak.setOnClickListener {
                if (isGameRunning) {
                    if (namaWarna == warnaTargetSaatIni) {
                        skor++
                        tvSkor.text = "Skor: $skor"
                        buatSoalBaru()
                    } else {
                        Toast.makeText(this, "Itu bukan warna $warnaTargetSaatIni!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            gridLayoutWarna.addView(kotak)
        }
    }

    private fun simpanSkorKeFirebase(namaGame: String, skorAkhir: Int) {
        if (idAnak.isEmpty()) { finish(); return }
        val db = FirebaseFirestore.getInstance()
        val dataSkor = hashMapOf("id_anak" to idAnak, "nama_game" to namaGame, "skor" to skorAkhir, "tanggal_main" to Date())
        db.collection("tb_riwayat_game").add(dataSkor).addOnSuccessListener { finish() }.addOnFailureListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}