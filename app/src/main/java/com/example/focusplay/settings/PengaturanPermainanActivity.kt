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

/**
 * Halaman pengaturan permainan dengan tampilan activity_pengaturan_permainan.xml.
 *
 * Pengaturan disimpan lokal di SharedPreferences "pengaturan_permainan".
 * GamePasangKartuActivity dan game lain mengambil nilai dari nama file dan key yang sama.
 */
class PengaturanPermainanActivity : AppCompatActivity() {

    // ==================== BAGIAN VARIABEL PENGATURAN ====================
    // Tombol kembali pada header.
    private lateinit var ivBack: ImageView
    // Input target waktu bermain dalam menit.
    private lateinit var etTargetWaktu: EditText
    // Switch untuk mengaktifkan atau menonaktifkan mode adaptif.
    private lateinit var switchAdaptif: Switch
    // Tombol gambar untuk menyimpan pengaturan.
    private lateinit var btnSimpanPengaturan: ImageView

    // Nama file SharedPreferences yang juga dibaca oleh Activity game.
    private val namaPrefs = "pengaturan_permainan"

    // ==================== BAGIAN INISIALISASI HALAMAN ====================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Memasang layout halaman pengaturan.
        setContentView(R.layout.activity_pengaturan_permainan)

        // Menyiapkan elemen tampilan.
        hubungkanView()
        // Mengisi form dengan pengaturan yang sudah tersimpan.
        muatPengaturanLokal()
        // Memasang aksi klik tombol.
        aturAksi()
    }

    // ==================== BAGIAN HUBUNGKAN ELEMEN XML ====================
    private fun hubungkanView() {
        // Mencari setiap elemen berdasarkan ID pada XML.
        ivBack = findViewById(R.id.ivBackPengaturan)
        etTargetWaktu = findViewById(R.id.etTargetWaktu)
        switchAdaptif = findViewById(R.id.switchAdaptif)
        btnSimpanPengaturan = findViewById(R.id.btnSimpanPengaturan)
    }

    // ==================== BAGIAN AMBIL SHARED PREFERENCES ====================
    private fun muatPengaturanLokal() {
        // Mengambil pengaturan yang sebelumnya sudah disimpan.
        // Membuka file SharedPreferences pengaturan permainan.
        val prefs = getSharedPreferences(namaPrefs, MODE_PRIVATE)

        // Mengambil target waktu dengan nilai awal 1 menit.
        val targetWaktu = prefs.getString("target_waktu", "1")
        // Mengambil status mode adaptif dengan nilai awal aktif.
        val modeAdaptif = prefs.getBoolean("mode_adaptif", true)

        // Menampilkan target waktu pada EditText.
        etTargetWaktu.setText(targetWaktu)
        // Menyesuaikan posisi switch dengan data yang tersimpan.
        switchAdaptif.isChecked = modeAdaptif

    }

    // ==================== BAGIAN SIMPAN PENGATURAN ====================
    private fun aturAksi() {
        // Menutup halaman ketika tombol kembali ditekan.
        ivBack.setOnClickListener {
            finish()
        }


        btnSimpanPengaturan.setOnClickListener {
            // Mengambil teks target waktu dan menghapus spasi tepi.
            val targetWaktu = etTargetWaktu.text.toString().trim()
            // Mengambil nilai aktif/nonaktif dari switch.
            val modeAdaptif = switchAdaptif.isChecked

            // Menghentikan proses jika input belum diisi.
            if (targetWaktu.isEmpty()) {
                Toast.makeText(this, "Target waktu belum diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mengubah input menjadi Int; hasil null jika bukan angka.
            val waktuInt = targetWaktu.toIntOrNull()
            // Target waktu harus berupa angka positif.
            if (waktuInt == null || waktuInt <= 0) {
                Toast.makeText(this, "Target waktu harus berupa angka lebih dari 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Key ini nanti dibaca lagi lewat bacaPengaturan() di Activity permainan.
            getSharedPreferences(namaPrefs, MODE_PRIVATE)
                // Membuka editor SharedPreferences.
                .edit()
                // Menyimpan target waktu sebagai String.
                .putString("target_waktu", targetWaktu)
                // Menyimpan status mode adaptif sebagai Boolean.
                .putBoolean("mode_adaptif", modeAdaptif)
                // Menerapkan perubahan pengaturan.
                .apply()

            // Menampilkan pesan berhasil.
            tampilkanToastSukses("Pengaturan berhasil disimpan")
            // Kembali ke halaman sebelumnya.
            finish()
        }
    }

    // ==================== BAGIAN TAMPILAN TOAST ====================
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
