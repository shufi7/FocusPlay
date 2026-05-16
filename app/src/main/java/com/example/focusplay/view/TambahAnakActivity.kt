package com.example.focusplay.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TambahAnakActivity : AppCompatActivity() {

    private lateinit var etNamaAnak: EditText
    private lateinit var etUsiaAnak: EditText
    private lateinit var btnSimpanAnak: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_anak)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNamaAnak = findViewById(R.id.etNamaAnak)
        etUsiaAnak = findViewById(R.id.etUsiaAnak)
        btnSimpanAnak = findViewById(R.id.btnSimpanAnak)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        btnSimpanAnak.setOnClickListener {
            val nama = etNamaAnak.text.toString().trim()
            val usia = etUsiaAnak.text.toString().trim()

            if (nama.isEmpty() || usia.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prosesSimpanAnakFirebase(nama, usia.toInt())
        }
    }

    private fun prosesSimpanAnakFirebase(nama: String, usia: Int) {
        // Pastikan ada user yang sedang login
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Sesi login tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        // Siapkan data yang akan disimpan ke Firestore
        val anakData = hashMapOf(
            "id_pendamping" to currentUser.uid, // Menggunakan UID unik dari akun Google
            "nama_anak" to nama,
            "usia" to usia
        )

        // Simpan ke koleksi "tb_anak" di Firestore
        db.collection("tb_anak")
            .add(anakData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil $nama berhasil disimpan ke awan!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke Dashboard
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}