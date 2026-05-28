package com.example.focusplay.settings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class PengaturanPermainanActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var etTargetWaktu: EditText
    private lateinit var switchAdaptif: Switch
    private lateinit var btnSimpanPengaturan: Button

    private val namaPrefs = "pengaturan_permainan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengaturan_permainan)

        hubungkanView()
        muatPengaturanLokal()
        aturAksi()
    }

    private fun hubungkanView() {
        ivBack = findViewById(R.id.ivBackPengaturan)
        etTargetWaktu = findViewById(R.id.etTargetWaktu)
        switchAdaptif = findViewById(R.id.switchAdaptif)
        btnSimpanPengaturan = findViewById(R.id.btnSimpanPengaturan)
    }

    private fun muatPengaturanLokal() {
        val prefs = getSharedPreferences(namaPrefs, MODE_PRIVATE)

        val targetWaktu = prefs.getString("target_waktu", "1")
        val modeAdaptif = prefs.getBoolean("mode_adaptif", true)

        etTargetWaktu.setText(targetWaktu)
        switchAdaptif.isChecked = modeAdaptif

    }

    private fun aturAksi() {
        ivBack.setOnClickListener {
            finish()
        }


        btnSimpanPengaturan.setOnClickListener {
            val targetWaktu = etTargetWaktu.text.toString().trim()
            val modeAdaptif = switchAdaptif.isChecked

            if (targetWaktu.isEmpty()) {
                Toast.makeText(this, "Target waktu belum diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val waktuInt = targetWaktu.toIntOrNull()
            if (waktuInt == null || waktuInt <= 0) {
                Toast.makeText(this, "Target waktu harus berupa angka lebih dari 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getSharedPreferences(namaPrefs, MODE_PRIVATE)
                .edit()
                .putString("target_waktu", targetWaktu)
                .putBoolean("mode_adaptif", modeAdaptif)
                .apply()

            Toast.makeText(this, "Pengaturan berhasil disimpan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


}