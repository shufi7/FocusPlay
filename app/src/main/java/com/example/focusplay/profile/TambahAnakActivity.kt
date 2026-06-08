package com.example.focusplay.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.AvatarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TambahAnakActivity : AppCompatActivity() {

    private lateinit var etNamaAnak: EditText
    private lateinit var etUsiaAnak: EditText
    private lateinit var btnSimpanAnak: Button
    private lateinit var containerAvatarAnak: LinearLayout

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedAvatar = "char_red"
    private val avatarViews = mutableMapOf<String, ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_anak)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNamaAnak = findViewById(R.id.etNamaAnak)
        etUsiaAnak = findViewById(R.id.etUsiaAnak)
        btnSimpanAnak = findViewById(R.id.btnSimpanAnak)
        containerAvatarAnak = findViewById(R.id.containerAvatarAnak)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            finish()
        }

        tampilkanPilihanAvatar()
        pilihAvatar("char_red")

        btnSimpanAnak.setOnClickListener {
            if (!btnSimpanAnak.isEnabled) return@setOnClickListener

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

    private fun tampilkanPilihanAvatar() {
        containerAvatarAnak.removeAllViews()
        avatarViews.clear()

        AvatarHelper.avatarIds.forEach { avatar ->
            val avatarView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(72), dp(72)).apply {
                    setMargins(0, 0, dp(12), 0)
                }
                setBackgroundResource(R.drawable.bg_avatar_normal)
                setImageResource(AvatarHelper.getAvatarResource(avatar))
                contentDescription = "Pilih karakter ${avatar.toLabelAvatar()}"
                isClickable = true
                isFocusable = true
                setPadding(dp(10), dp(10), dp(10), dp(10))
                scaleType = ImageView.ScaleType.FIT_CENTER
                setOnClickListener {
                    pilihAvatar(avatar)
                }
            }

            avatarViews[avatar] = avatarView
            containerAvatarAnak.addView(avatarView)
        }
    }

    private fun pilihAvatar(avatar: String) {
        selectedAvatar = avatar

        avatarViews.forEach { (avatarId, avatarView) ->
            val background = if (avatarId == avatar) {
                R.drawable.bg_avatar_selected
            } else {
                R.drawable.bg_avatar_normal
            }
            avatarView.setBackgroundResource(background)
        }

        avatarViews[avatar]?.let { selectedView ->
            selectedView.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(60)
                .withEndAction {
                    selectedView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start()
                }
                .start()
        }
    }

    private fun prosesSimpanAnakFirebase(nama: String, usia: Int) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(
                this,
                "Sesi login tidak ditemukan. Silakan login ulang.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val anakData = hashMapOf(
            "id_pendamping" to currentUser.uid,
            "nama_anak" to nama,
            "usia" to usia,
            "avatar" to selectedAvatar
        )

        btnSimpanAnak.isEnabled = false
        btnSimpanAnak.text = "Menyimpan..."

        db.collection("tb_anak")
            .add(anakData)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Profil $nama berhasil disimpan!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnSimpanAnak.isEnabled = true
                btnSimpanAnak.text = "Simpan Profil Anak"
                Toast.makeText(
                    this,
                    "Gagal menyimpan: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun String.toLabelAvatar(): String {
        return removePrefix("char_").replace("_", " ")
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
