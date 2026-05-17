package com.example.focusplay.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class PengaturanPermainanActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var spinnerFase: Spinner
    private lateinit var etTargetWaktu: EditText
    private lateinit var switchAdaptif: Switch
    private lateinit var btnSimpanPengaturan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengaturan_permainan)

        ivBack = findViewById(R.id.ivBackPengaturan)
        spinnerFase = findViewById(R.id.spinnerFase)
        etTargetWaktu = findViewById(R.id.etTargetWaktu)
        switchAdaptif = findViewById(R.id.switchAdaptif)
        btnSimpanPengaturan = findViewById(R.id.btnSimpanPengaturan)

        ivBack.setOnClickListener {
            finish()
        }

        val daftarFase = arrayOf(
            "Fase 1 - Mudah",
            "Fase 2 - Sedang",
            "Fase 3 - Sulit"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            daftarFase
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFase.adapter = adapter

        btnSimpanPengaturan.setOnClickListener {
            val faseDipilih = spinnerFase.selectedItem.toString()
            val targetWaktu = etTargetWaktu.text.toString().trim()
            val modeAdaptif = switchAdaptif.isChecked

            if (targetWaktu.isEmpty()) {
                Toast.makeText(this, "Target waktu belum diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Pengaturan disimpan: $faseDipilih, $targetWaktu menit",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}