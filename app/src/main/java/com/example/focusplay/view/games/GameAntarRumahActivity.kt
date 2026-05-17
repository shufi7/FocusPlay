package com.example.focusplay.view.games

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class GameAntarRumahActivity : AppCompatActivity() {

    private lateinit var viewMobil: TextView
    private lateinit var viewRumah: TextView
    private lateinit var tvInstruksi: TextView
    private lateinit var layStatus: View

    private var dX = 0f
    private var dY = 0f
    private var posisiAwalX = 0f
    private var posisiAwalY = 0f

    private var idAnak = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_antar_rumah)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        viewMobil = findViewById(R.id.viewMobil)
        viewRumah = findViewById(R.id.viewRumah)
        tvInstruksi = findViewById(R.id.tvInstruksi)
        layStatus = findViewById(R.id.layStatus)

        viewMobil.post {
            posisiAwalX = viewMobil.x
            posisiAwalY = viewMobil.y
        }

        viewMobil.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    view.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                }
                MotionEvent.ACTION_UP -> {
                    cekKeberhasilan()
                }
                else -> return@setOnTouchListener false
            }
            true
        }
    }

    private fun cekKeberhasilan() {
        val mobilTengahX = viewMobil.x + (viewMobil.width / 2)
        val mobilTengahY = viewMobil.y + (viewMobil.height / 2)

        val rumahKiri = viewRumah.x
        val rumahKanan = viewRumah.x + viewRumah.width
        val rumahAtas = viewRumah.y
        val rumahBawah = viewRumah.y + viewRumah.height

        if (mobilTengahX >= rumahKiri && mobilTengahX <= rumahKanan &&
            mobilTengahY >= rumahAtas && mobilTengahY <= rumahBawah) {

            tvInstruksi.text = "Berhasil! 🚗 sudah di rumah! 🎉"
            layStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            tvInstruksi.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))

            viewMobil.setOnTouchListener(null)

            // Jeda 1,5 detik agar anak bisa melihat hasilnya sebelum keluar
            Handler(Looper.getMainLooper()).postDelayed({
                simpanSkorKeFirebase("Antar ke Rumah", 100)
            }, 1500)

        } else {
            viewMobil.animate().x(posisiAwalX).y(posisiAwalY).setDuration(300).start()
        }
    }

    private fun simpanSkorKeFirebase(namaGame: String, skorAkhir: Int) {
        if (idAnak.isEmpty()) { finish(); return }

        val db = FirebaseFirestore.getInstance()
        val dataSkor = hashMapOf(
            "id_anak" to idAnak,
            "nama_game" to namaGame,
            "skor" to skorAkhir,
            "tanggal_main" to Date()
        )

        db.collection("tb_riwayat_game").add(dataSkor)
            .addOnSuccessListener {
                Toast.makeText(this, "Skor disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { finish() }
    }
}