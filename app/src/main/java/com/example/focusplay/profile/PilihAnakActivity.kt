package com.example.focusplay.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardAnakActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class PilihAnakActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var containerProfilAnak: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var ivBack: ImageView

    private val daftarAnakCache = mutableListOf<Anak>()
    private var perluRefreshData = true

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
        tvEmptyState = findViewById(R.id.tvEmptyState)
        ivBack = findViewById(R.id.ivBackPilihAnak)

        ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        if (daftarAnakCache.isNotEmpty() && !perluRefreshData) {
            tampilkanDaftarAnak(daftarAnakCache)
        } else {
            ambilDataAnak()
        }
    }

    private fun ambilDataAnak() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(
                this,
                "Sesi login tidak ditemukan. Silakan login ulang.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        containerProfilAnak.removeAllViews()
        tvEmptyState.visibility = TextView.VISIBLE
        tvEmptyState.text = "Memuat profil anak..."

        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", currentUser.uid)
            .get(Source.CACHE)
            .addOnSuccessListener { cacheResult ->
                if (!cacheResult.isEmpty) {
                    val daftarAnak = cacheResult.documents.mapNotNull { doc ->
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

                    daftarAnakCache.clear()
                    daftarAnakCache.addAll(daftarAnak)
                    tampilkanDaftarAnak(daftarAnak)
                }

                ambilDataAnakDariServer(currentUser.uid)
            }
            .addOnFailureListener {
                ambilDataAnakDariServer(currentUser.uid)
            }
    }

    private fun ambilDataAnakDariServer(uidPendamping: String) {
        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", uidPendamping)
            .get(Source.SERVER)
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

                daftarAnakCache.clear()
                daftarAnakCache.addAll(daftarAnak)
                perluRefreshData = false

                tampilkanDaftarAnak(daftarAnak)
            }
            .addOnFailureListener { e ->
                if (daftarAnakCache.isEmpty()) {
                    tvEmptyState.visibility = TextView.VISIBLE
                    tvEmptyState.text = "Gagal mengambil data anak"
                }

                Toast.makeText(
                    this,
                    "Gagal mengambil data anak: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun tampilkanDaftarAnak(daftarAnak: List<Anak>) {
        containerProfilAnak.removeAllViews()

        if (daftarAnak.isEmpty()) {
            tvEmptyState.visibility = TextView.VISIBLE
            tvEmptyState.text = "Belum ada profil anak"
        } else {
            tvEmptyState.visibility = TextView.GONE

            daftarAnak.forEach { anak ->
                tambahCardAnak(anak)
            }
        }
    }

    private fun tambahCardAnak(anak: Anak) {
        val itemView = layoutInflater.inflate(
            R.layout.item_anak,
            containerProfilAnak,
            false
        )

        val imgAvatarAnak = itemView.findViewById<ImageView>(R.id.imgAvatarAnak)
        val tvItemNamaAnak = itemView.findViewById<TextView>(R.id.tvItemNamaAnak)
        val tvItemUmurAnak = itemView.findViewById<TextView>(R.id.tvItemUmurAnak)

        val karakterAnak = when (anak.avatar) {
            "char_blue" -> R.drawable.char_blue
            "char_purple" -> R.drawable.char_purple
            "char_star" -> R.drawable.char_star
            else -> R.drawable.char_red
        }

        imgAvatarAnak.setImageResource(karakterAnak)
        tvItemNamaAnak.text = anak.namaAnak
        tvItemUmurAnak.text = "${anak.usia} tahun"

        itemView.setOnClickListener {
            val intent = Intent(this, DashboardAnakActivity::class.java)

            intent.putExtra("ID_ANAK", anak.idDokumen)
            intent.putExtra("NAMA_ANAK", anak.namaAnak)
            intent.putExtra("USIA_ANAK", anak.usia)

            intent.putExtra("id_anak", anak.idDokumen)
            intent.putExtra("nama_anak", anak.namaAnak)
            intent.putExtra("usia_anak", anak.usia)

            startActivity(intent)
        }

        itemView.setOnLongClickListener {
            tampilkanDialogHapusAnak(anak)
            true
        }

        containerProfilAnak.addView(itemView)
    }

    private fun tampilkanDialogHapusAnak(anak: Anak) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(18))
            background = roundedDrawable("#FFFFFF", 26, "#E7D9C8")
        }

        val icon = TextView(this).apply {
            text = "!"
            gravity = Gravity.CENTER
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = circleDrawable("#E95A6A")
            includeFontPadding = false

            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        val title = TextView(this).apply {
            text = "Hapus Profil Anak?"
            gravity = Gravity.CENTER
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#2F2F2F"))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(14), 0, 0)
            }
        }

        val message = TextView(this).apply {
            text = "Profil ${anak.namaAnak} akan dihapus dari daftar anak. Tekan Hapus jika sudah yakin."
            gravity = Gravity.CENTER
            textSize = 13.5f
            setTextColor(Color.parseColor("#7A6B5D"))
            setLineSpacing(4f, 1f)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(8), 0, 0)
            }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(20), 0, 0)
            }
        }

        val btnBatal = TextView(this).apply {
            text = "Batal"
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#7A6B5D"))
            background = roundedDrawable("#FFF8F1", 18, "#E7D9C8")

            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                setMargins(0, 0, dp(8), 0)
            }
        }

        val btnHapus = TextView(this).apply {
            text = "Hapus"
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = roundedDrawable("#E95A6A", 18, "#E95A6A")

            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                setMargins(dp(8), 0, 0, 0)
            }
        }

        buttonRow.addView(btnBatal)
        buttonRow.addView(btnHapus)

        container.addView(icon)
        container.addView(title)
        container.addView(message)
        container.addView(buttonRow)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(container)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnHapus.setOnClickListener {
            btnHapus.text = "Menghapus..."
            btnHapus.isEnabled = false
            hapusProfilAnakTanpaReload(anak, dialog)
        }

        dialog.show()
    }

    private fun hapusProfilAnakTanpaReload(
        anak: Anak,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        db.collection("tb_anak")
            .document(anak.idDokumen)
            .delete()
            .addOnSuccessListener {
                dialog.dismiss()

                daftarAnakCache.removeAll { it.idDokumen == anak.idDokumen }
                perluRefreshData = false

                tampilkanDaftarAnak(daftarAnakCache)
                tampilkanToastCustom("Profil ${anak.namaAnak} berhasil dihapus")
            }
            .addOnFailureListener { e ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Gagal menghapus profil: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun tampilkanToastCustom(pesan: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = roundedDrawable("#FFF8F1", 18, "#E7D9C8")
            elevation = dp(4).toFloat()
        }

        val icon = TextView(this).apply {
            text = "✓"
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = circleDrawable("#8DB52A")
            includeFontPadding = false

            layoutParams = LinearLayout.LayoutParams(dp(28), dp(28))
        }

        val text = TextView(this).apply {
            this.text = pesan
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#2F2F2F"))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(10), 0, 0, 0)
            }
        }

        layout.addView(icon)
        layout.addView(text)

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(90))
            show()
        }
    }

    private fun roundedDrawable(color: String, radius: Int, strokeColor: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius).toFloat()
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