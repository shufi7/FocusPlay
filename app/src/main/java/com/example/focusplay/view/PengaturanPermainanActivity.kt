package com.example.focusplay.view

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class PengaturanPermainanActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var spinnerFase: Spinner
    private lateinit var etTargetWaktu: EditText
    private lateinit var seekBarKecepatanItem: SeekBar
    private lateinit var tvKecepatanItem: TextView
    private lateinit var switchAdaptif: Switch
    private lateinit var btnSimpanPengaturan: Button

    private val namaPrefs = "pengaturan_permainan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengaturan_permainan)

        hubungkanView()
        aturSpinnerFase()
        muatPengaturanLokal()
        aturAksi()
    }

    private fun hubungkanView() {
        ivBack = findViewById(R.id.ivBackPengaturan)
        spinnerFase = findViewById(R.id.spinnerFase)
        etTargetWaktu = findViewById(R.id.etTargetWaktu)
        seekBarKecepatanItem = findViewById(R.id.seekBarKecepatanItem)
        tvKecepatanItem = findViewById(R.id.tvKecepatanItem)
        switchAdaptif = findViewById(R.id.switchAdaptif)
        btnSimpanPengaturan = findViewById(R.id.btnSimpanPengaturan)
    }

    private fun aturSpinnerFase() {
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
    }

    private fun muatPengaturanLokal() {
        val prefs = getSharedPreferences(namaPrefs, MODE_PRIVATE)

        val faseIndex = prefs.getInt("fase_index", 0)
        val targetWaktu = prefs.getString("target_waktu", "")
        val modeAdaptif = prefs.getBoolean("mode_adaptif", true)
        val kecepatanItem = prefs.getInt("kecepatan_item", 1)

        spinnerFase.setSelection(faseIndex)
        etTargetWaktu.setText(targetWaktu)
        switchAdaptif.isChecked = modeAdaptif
        seekBarKecepatanItem.progress = kecepatanItem
        tvKecepatanItem.text = labelKecepatan(kecepatanItem)
    }

    private fun aturAksi() {
        ivBack.setOnClickListener {
            finish()
        }

        seekBarKecepatanItem.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    tvKecepatanItem.text = labelKecepatan(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        btnSimpanPengaturan.setOnClickListener {
            val faseIndex = spinnerFase.selectedItemPosition
            val faseDipilih = spinnerFase.selectedItem.toString()
            val targetWaktu = etTargetWaktu.text.toString().trim()
            val modeAdaptif = switchAdaptif.isChecked
            val kecepatanItem = seekBarKecepatanItem.progress

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
                .putInt("fase_index", faseIndex)
                .putString("fase_nama", faseDipilih)
                .putString("target_waktu", targetWaktu)
                .putBoolean("mode_adaptif", modeAdaptif)
                .putInt("kecepatan_item", kecepatanItem)
                .putString("kecepatan_item_label", labelKecepatan(kecepatanItem))
                .apply()

            Toast.makeText(
                this,
                "Pengaturan berhasil disimpan",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun labelKecepatan(progress: Int): String {
        return when (progress) {
            0 -> "Lambat"
            1 -> "Normal"
            else -> "Cepat"
        }
    }
}