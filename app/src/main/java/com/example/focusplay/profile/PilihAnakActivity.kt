package com.example.focusplay.profile

import android.content.Intent
import android.os.Bundle
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
    private lateinit var btnTambahProfilAnak: LinearLayout
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
        btnTambahProfilAnak = findViewById(R.id.btnTambahProfilAnak)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        ivBack = findViewById(R.id.ivBackPilihAnak)

        ivBack.setOnClickListener {
            finish()
        }

        btnTambahProfilAnak.setOnClickListener {
            perluRefreshData = true
            startActivity(Intent(this, TambahAnakActivity::class.java))
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

        containerProfilAnak.addView(itemView)
    }
}