package com.example.focusplay.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class TambahAnakActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var etNamaAnak: EditText
    private lateinit var etUsiaAnak: EditText
    private lateinit var btnSimpanAnak: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_anak)

        ivBack = findViewById(R.id.ivBack)
        etNamaAnak = findViewById(R.id.etNamaAnak)
        etUsiaAnak = findViewById(R.id.etUsiaAnak)
        btnSimpanAnak = findViewById(R.id.btnSimpanAnak)

        // Tombol kembali (ikon panah)
        ivBack.setOnClickListener {
            finish() // Tutup halaman ini, kembali ke Dashboard
        }

        // Tombol simpan
        btnSimpanAnak.setOnClickListener {
            val nama = etNamaAnak.text.toString().trim()
            val usia = etUsiaAnak.text.toString().trim()

            if (nama.isEmpty() || usia.isEmpty()) {
                Toast.makeText(this, "Nama dan usia anak harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pesan sementara sebelum kita hubungkan ke API PHP
            Toast.makeText(this, "Data $nama siap dikirim ke server!", Toast.LENGTH_SHORT).show()
            // Nanti setelah API dibuat, kita panggil fungsi Retrofit di sini
        }
    }
}