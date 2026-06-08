package com.example.focusplay.settings

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class PengaturanPermainanActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var etTargetWaktu: EditText
    private lateinit var switchAdaptif: Switch
    private lateinit var btnSimpanPengaturan: ImageView

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

            tampilkanToastSukses("Pengaturan berhasil disimpan")
            finish()
        }
    }

    private fun tampilkanToastSukses(pesan: String) {

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 0)

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val isi = TextView(this).apply {
            text = pesan
            textSize = 12f
            setTextColor(Color.parseColor("#4E6B4F"))
            setPadding(0,0, 0, 0)
        }

        textBox.addView(isi)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(12), dp(16), dp(12))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(Color.parseColor("#F1F8E9"))
                setStroke(dp(1), Color.parseColor("#C5E1A5"))
            }
            elevation = dp(6).toFloat()
            addView(textBox)
        }

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(28))
            show()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

}
