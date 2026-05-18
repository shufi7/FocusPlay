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

    private lateinit var avatarRed: ImageView
    private lateinit var avatarBlue: ImageView
    private lateinit var avatarPurple: ImageView
    private lateinit var avatarStar: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedAvatar = "char_red"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_anak)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNamaAnak = findViewById(R.id.etNamaAnak)
        etUsiaAnak = findViewById(R.id.etUsiaAnak)
        btnSimpanAnak = findViewById(R.id.btnSimpanAnak)

        avatarRed = findViewById(R.id.avatarRed)
        avatarBlue = findViewById(R.id.avatarBlue)
        avatarPurple = findViewById(R.id.avatarPurple)
        avatarStar = findViewById(R.id.avatarStar)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        avatarRed.setOnClickListener {
            pilihAvatar("char_red")
        }

        avatarBlue.setOnClickListener {
            pilihAvatar("char_blue")
        }

        avatarPurple.setOnClickListener {
            pilihAvatar("char_purple")
        }

        avatarStar.setOnClickListener {
            pilihAvatar("char_star")
        }

        btnSimpanAnak.setOnClickListener {
            val nama = etNamaAnak.text.toString().trim()
            val usiaText = etUsiaAnak.text.toString().trim()

            if (nama.isEmpty() || usiaText.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usia = usiaText.toIntOrNull()
            if (usia == null) {
                Toast.makeText(this, "Usia harus berupa angka", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prosesSimpanAnakFirebase(nama, usia)
        }
    }

    private fun pilihAvatar(avatar: String) {
        selectedAvatar = avatar

        avatarRed.setBackgroundResource(R.drawable.bg_avatar_normal)
        avatarBlue.setBackgroundResource(R.drawable.bg_avatar_normal)
        avatarPurple.setBackgroundResource(R.drawable.bg_avatar_normal)
        avatarStar.setBackgroundResource(R.drawable.bg_avatar_normal)

        when (avatar) {
            "char_red" -> avatarRed.setBackgroundResource(R.drawable.bg_avatar_selected)
            "char_blue" -> avatarBlue.setBackgroundResource(R.drawable.bg_avatar_selected)
            "char_purple" -> avatarPurple.setBackgroundResource(R.drawable.bg_avatar_selected)
            "char_star" -> avatarStar.setBackgroundResource(R.drawable.bg_avatar_selected)
        }
    }

    private fun prosesSimpanAnakFirebase(nama: String, usia: Int) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Sesi login tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        val anakData = hashMapOf(
            "id_pendamping" to currentUser.uid,
            "nama_anak" to nama,
            "usia" to usia,
            "avatar" to selectedAvatar
        )

        db.collection("tb_anak")
            .add(anakData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil $nama berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}