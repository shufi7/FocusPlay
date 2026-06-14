package com.example.focusplay.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardActivity
import com.example.focusplay.dashboard.DashboardAnakActivity
import com.example.focusplay.utils.AvatarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/**
 * Halaman untuk memilih profil anak dengan tampilan activity_pilih_anak.xml.
 *
 * Profil diambil dari koleksi "tb_anak" berdasarkan UID pendamping. Saat kartu dipilih,
 * data anak dikirim melalui Intent ke DashboardAnakActivity untuk digunakan saat bermain.
 */
class PilihAnakActivity : AppCompatActivity() {

    // ==================== BAGIAN VARIABEL DAN DATA ANAK ====================
    // Menyediakan informasi pengguna Firebase yang sedang login.
    private lateinit var auth: FirebaseAuth
    // Menyediakan akses untuk membaca dan menghapus data Firestore.
    private lateinit var db: FirebaseFirestore

    // Grid tempat kartu profil anak ditambahkan.
    private lateinit var containerProfilAnak: GridLayout
    // Menampilkan status loading, kosong, atau gagal.
    private lateinit var tvEmptyState: TextView
    // Tombol menuju Dashboard Orang Tua ketika belum ada profil.
    private lateinit var btnBukaHalamanOrangTua: TextView
    // Tombol kembali pada header.
    private lateinit var ivBack: ImageView

    // Menyimpan hasil terakhir agar daftar dapat ditampilkan tanpa request ulang.
    private val daftarAnakCache = mutableListOf<Anak>()
    // Menentukan apakah data harus dimuat ulang dari Firestore.
    private var perluRefreshData = true

    // Bentuk data anak yang digunakan khusus di halaman ini.
    data class Anak(
        val idDokumen: String,
        val namaAnak: String,
        val usia: Int,
        val avatar: String
    )

    // ==================== BAGIAN INISIALISASI HALAMAN ====================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Memasang activity_pilih_anak.xml sebagai tampilan halaman.
        setContentView(R.layout.activity_pilih_anak)

        // Menyiapkan Firebase Auth dan Firestore.
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Menghubungkan variabel dengan elemen pada XML.
        containerProfilAnak = findViewById(R.id.containerProfilAnak)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBukaHalamanOrangTua = findViewById(R.id.btnBukaHalamanOrangTua)
        ivBack = findViewById(R.id.ivBackPilihAnak)

        // Menutup halaman ketika tombol kembali ditekan.
        ivBack.setOnClickListener {
            finish()
        }

        // Membuka Dashboard Orang Tua ketika tombol ditekan.
        btnBukaHalamanOrangTua.setOnClickListener {
            // Membuat Intent menuju DashboardActivity.
            startActivity(Intent(this, DashboardActivity::class.java))
            // Menutup halaman pemilihan profil.
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        // Menggunakan cache jika data sudah ada dan tidak perlu diperbarui.
        if (daftarAnakCache.isNotEmpty() && !perluRefreshData) {
            tampilkanDaftarAnak(daftarAnakCache)
        } else {
            // Mengambil ulang data jika cache belum tersedia.
            ambilDataAnak()
        }
    }

    // ==================== BAGIAN AMBIL DATA ANAK ====================
    private fun ambilDataAnak() {
        // Mengambil akun yang sedang login sebagai pemilik profil anak.
        val currentUser = auth.currentUser

        // Menghentikan proses jika sesi Firebase tidak ditemukan.
        if (currentUser == null) {
            Toast.makeText(
                this,
                "Sesi login tidak ditemukan. Silakan login ulang.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Mengosongkan kartu lama sebelum menampilkan loading.
        containerProfilAnak.removeAllViews()
        tvEmptyState.visibility = TextView.VISIBLE
        tvEmptyState.text = "Memuat profil anak..."
        btnBukaHalamanOrangTua.visibility = View.GONE

        // Menampilkan cache lebih dulu, lalu memperbarui data dari server.
        // Memfilter profil berdasarkan UID pendamping yang sedang login.
        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", currentUser.uid)
            // Membaca cache lokal Firestore lebih dulu.
            .get(Source.CACHE)
            .addOnSuccessListener { cacheResult ->
                // Cache hanya diproses jika berisi dokumen.
                if (!cacheResult.isEmpty) {
                    // Mengubah setiap dokumen menjadi object Anak.
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

                    // Mengganti isi cache Activity dengan hasil Firestore.
                    daftarAnakCache.clear()
                    daftarAnakCache.addAll(daftarAnak)
                    tampilkanDaftarAnak(daftarAnak)
                }

                // Tetap mengambil server untuk memperoleh data terbaru.
                ambilDataAnakDariServer(currentUser.uid)
            }
            // Jika cache gagal, langsung mencoba mengambil data server.
            .addOnFailureListener {
                ambilDataAnakDariServer(currentUser.uid)
            }
    }

    private fun ambilDataAnakDariServer(uidPendamping: String) {
        // Hasil dari server menggantikan cache agar data tetap terbaru.
        // Membaca koleksi tb_anak langsung dari server.
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
                // Menandai bahwa hasil terbaru sudah berhasil dimuat.
                perluRefreshData = false

                tampilkanDaftarAnak(daftarAnak)
            }
            .addOnFailureListener { e ->
                if (daftarAnakCache.isEmpty()) {
                    tvEmptyState.visibility = TextView.VISIBLE
                    tvEmptyState.text = "Gagal mengambil data anak"
                    btnBukaHalamanOrangTua.visibility = View.GONE
                }

                Toast.makeText(
                    this,
                    "Gagal mengambil data anak: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // ==================== BAGIAN TAMPILKAN PROFIL ANAK ====================
    private fun tampilkanDaftarAnak(daftarAnak: List<Anak>) {
        // Menghapus seluruh kartu sebelum menggambar daftar terbaru.
        containerProfilAnak.removeAllViews()

        // Menampilkan empty state jika tidak ada profil.
        if (daftarAnak.isEmpty()) {
            tvEmptyState.visibility = TextView.VISIBLE
            tvEmptyState.text = "Data anak masih kosong.\nBuat profil anak dulu melalui halaman Orang Tua."
            btnBukaHalamanOrangTua.visibility = View.VISIBLE
        } else {
            tvEmptyState.visibility = TextView.GONE
            btnBukaHalamanOrangTua.visibility = View.GONE

            // Menampilkan satu kartu untuk setiap data anak.
            daftarAnak.forEach { anak ->
                tambahCardAnak(anak)
            }
        }
    }

    private fun tambahCardAnak(anak: Anak) {
        // Tampilan setiap kartu profil diambil dari item_anak.xml.
        // Mengambil struktur kartu dari item_anak.xml.
        val itemView = layoutInflater.inflate(
            R.layout.item_anak,
            containerProfilAnak,
            false
        )

        // Gunakan Spec untuk memaksa item berada di kolom yang benar dengan bobot seimbang (50% lebar)
        // Membuat parameter agar kartu mengisi dua kolom secara seimbang.
        val params = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f), // row spec
            GridLayout.spec(GridLayout.UNDEFINED, 1f)  // column spec
        ).apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(dp(8), dp(8), dp(8), dp(8))
        }
        itemView.layoutParams = params

        // Menghubungkan elemen di dalam item_anak.xml.
        val imgAvatarAnak = itemView.findViewById<ImageView>(R.id.imgAvatarAnak)
        val tvItemNamaAnak = itemView.findViewById<TextView>(R.id.tvItemNamaAnak)
        val tvItemUmurAnak = itemView.findViewById<TextView>(R.id.tvItemUmurAnak)

        // Mengisi avatar, nama, dan umur dari object Anak.
        imgAvatarAnak.setImageResource(AvatarHelper.getAvatarResource(anak.avatar))
        tvItemNamaAnak.text = anak.namaAnak
        tvItemUmurAnak.text = "${anak.usia} tahun"

        // Membuka Dashboard Anak ketika kartu ditekan.
        itemView.setOnClickListener {
            val intent = Intent(this, DashboardAnakActivity::class.java)

            // Data extra ini nanti diambil DashboardAnakActivity untuk tahu anak yang sedang dipilih.
            intent.putExtra("ID_ANAK", anak.idDokumen)
            intent.putExtra("NAMA_ANAK", anak.namaAnak)
            intent.putExtra("USIA_ANAK", anak.usia)
            intent.putExtra("AVATAR_ANAK", anak.avatar)

            intent.putExtra("id_anak", anak.idDokumen)
            intent.putExtra("nama_anak", anak.namaAnak)
            intent.putExtra("usia_anak", anak.usia)
            intent.putExtra("avatar_anak", anak.avatar)

            // Menjalankan perpindahan menuju DashboardAnakActivity.
            startActivity(intent)
        }

        // Membuka dialog hapus ketika kartu ditekan lama.
        itemView.setOnLongClickListener {
            tampilkanDialogHapusAnak(anak)
            true
        }

        // Menambahkan kartu yang selesai dibuat ke GridLayout.
        containerProfilAnak.addView(itemView)
    }

    // ==================== BAGIAN HAPUS PROFIL ANAK ====================
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
        // Menghapus dokumen "tb_anak" menggunakan ID anak yang dipilih.
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

    // ==================== BAGIAN TAMPILAN BANTUAN ====================
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
