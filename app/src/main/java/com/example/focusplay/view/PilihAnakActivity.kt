package com.example.focusplay.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PilihAnakActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var containerProfilAnak: LinearLayout
    private lateinit var btnTambahProfilAnak: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var ivBack: ImageView

    data class Anak(
        val idDokumen: String,
        val namaAnak: String,
        val usia: Int,
        val avatar: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_anak)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        containerProfilAnak = findViewById(R.id.containerProfilAnak)
        btnTambahProfilAnak = findViewById(R.id.btnTambahProfilAnak)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        ivBack = findViewById(R.id.ivBackPilihAnak)

        ivBack.setOnClickListener {
            finish()
        }

        btnTambahProfilAnak.setOnClickListener {
            startActivity(Intent(this, TambahAnakActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        ambilDataAnak()
    }

    private fun ambilDataAnak() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Sesi login tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        containerProfilAnak.removeAllViews()

        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val daftarAnak = result.documents.mapNotNull { doc ->
                    val nama = doc.getString("nama_anak") ?: return@mapNotNull null
                    val usiaLong = doc.getLong("usia") ?: 0L
                    val avatar = doc.getString("avatar") ?: "char_red"
                    Anak(
                        idDokumen = doc.id,
                        namaAnak = nama,
                        usia = usiaLong.toInt(),
                        avatar = avatar
                    )
                }

                if (daftarAnak.isEmpty()) {
                    tvEmptyState.visibility = TextView.VISIBLE
                } else {
                    tvEmptyState.visibility = TextView.GONE
                    daftarAnak.forEachIndexed { index, anak ->
                        tambahCardAnak(anak, index)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data anak: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun tambahCardAnak(anak: Anak, index: Int) {
        val warnaCard = when (index % 4) {
            0 -> "#F4EEFF"
            1 -> "#F0FBEA"
            2 -> "#EAF7FF"
            else -> "#FFF3EA"
        }

        val karakterAnak = when (anak.avatar) {
            "char_blue" -> R.drawable.char_blue
            "char_purple" -> R.drawable.char_purple
            "char_star" -> R.drawable.char_star
            else -> R.drawable.char_red
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedDrawable(warnaCard, 22f, "#E5EEF7")
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(92)
            )
            params.setMargins(0, 0, 0, dp(12))
            layoutParams = params
        }

        val avatar = ImageView(this).apply {
            setImageResource(karakterAnak)
            contentDescription = "Karakter ${anak.namaAnak}"
            layoutParams = LinearLayout.LayoutParams(dp(62), dp(62))
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            params.setMargins(dp(14), 0, dp(10), 0)
            layoutParams = params
        }

        val tvNama = TextView(this).apply {
            text = anak.namaAnak
            textSize = 18f
            setTextColor(Color.parseColor("#1F2937"))
            typeface = Typeface.DEFAULT_BOLD
        }

        val tvUsia = TextView(this).apply {
            text = "${anak.usia} tahun"
            textSize = 13f
            setTextColor(Color.parseColor("#6B7280"))
            setPadding(0, dp(4), 0, 0)
        }

        textBox.addView(tvNama)
        textBox.addView(tvUsia)

        val arrow = ImageView(this).apply {
            setImageResource(R.drawable.ic_arrow_right)
            background = circleDrawable("#8DB52A")
            contentDescription = "Lanjut"
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(dp(34), dp(34))
        }

        card.addView(avatar)
        card.addView(textBox)
        card.addView(arrow)

        card.setOnClickListener {
            val intent = Intent(this, DashboardAnakActivity::class.java)

            intent.putExtra("ID_ANAK", anak.idDokumen)
            intent.putExtra("NAMA_ANAK", anak.namaAnak)
            intent.putExtra("USIA_ANAK", anak.usia)

            intent.putExtra("id_anak", anak.idDokumen)
            intent.putExtra("nama_anak", anak.namaAnak)
            intent.putExtra("usia_anak", anak.usia)

            startActivity(intent)
        }

        containerProfilAnak.addView(card)
    }

    private fun roundedDrawable(color: String, radius: Float, strokeColor: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius.toInt()).toFloat()
            setStroke(dp(1), Color.parseColor(strokeColor))
        }
    }

    private fun circleDrawable(color: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(color))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}