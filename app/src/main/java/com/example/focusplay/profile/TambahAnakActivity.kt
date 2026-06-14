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

/**
 * Halaman untuk membuat profil anak dengan tampilan activity_tambah_anak.xml.
 *
 * Halaman ini dibuka dari kartu Tambah Anak di DashboardActivity. Data yang disimpan ke
 * koleksi "tb_anak" nanti dibaca lagi oleh DashboardActivity dan PilihAnakActivity.
 */
class TambahAnakActivity : AppCompatActivity() {

    // ==================== BAGIAN VARIABEL FORM ====================
    // Input nama panggilan anak.
    private lateinit var etNamaAnak: EditText
    // Input usia anak.
    private lateinit var etUsiaAnak: EditText
    // Tombol untuk menjalankan proses penyimpanan.
    private lateinit var btnSimpanAnak: Button
    // Tempat pilihan avatar yang dibuat melalui kode.
    private lateinit var containerAvatarAnak: LinearLayout

    // Menyediakan data pengguna yang sedang login.
    private lateinit var auth: FirebaseAuth
    // Menyediakan akses ke database Cloud Firestore.
    private lateinit var db: FirebaseFirestore

    // Avatar awal yang dipilih sebelum pengguna memilih avatar lain.
    private var selectedAvatar = "char_red"
    // Menyimpan pasangan ID avatar dan ImageView untuk mengubah status tampilannya.
    private val avatarViews = mutableMapOf<String, ImageView>()

    // ==================== BAGIAN INISIALISASI HALAMAN ====================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Memasang activity_tambah_anak.xml sebagai tampilan.
        setContentView(R.layout.activity_tambah_anak)

        // Mengambil instance autentikasi Firebase.
        auth = FirebaseAuth.getInstance()
        // Mengambil instance database Firestore.
        db = FirebaseFirestore.getInstance()

        // Menghubungkan setiap variabel dengan elemen XML.
        etNamaAnak = findViewById(R.id.etNamaAnak)
        etUsiaAnak = findViewById(R.id.etUsiaAnak)
        btnSimpanAnak = findViewById(R.id.btnSimpanAnak)
        containerAvatarAnak = findViewById(R.id.containerAvatarAnak)

        // Mengambil tombol kembali yang hanya digunakan di dalam onCreate.
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        // Menutup halaman saat tombol kembali ditekan.
        ivBack.setOnClickListener {
            finish()
        }

        // Membuat semua pilihan avatar di dalam container.
        tampilkanPilihanAvatar()
        // Memberi status terpilih pada avatar awal.
        pilihAvatar("char_red")

        // Menjalankan validasi dan penyimpanan ketika tombol ditekan.
        btnSimpanAnak.setOnClickListener {
            // Mengabaikan klik tambahan ketika proses simpan sedang berjalan.
            if (!btnSimpanAnak.isEnabled) return@setOnClickListener

            // Mengambil nama dan menghapus spasi pada awal/akhir teks.
            val nama = etNamaAnak.text.toString().trim()
            // Mengambil usia dalam bentuk teks.
            val usiaText = etUsiaAnak.text.toString().trim()

            // Kedua input wajib diisi.
            if (nama.isEmpty() || usiaText.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mengubah usia menjadi angka dengan aman.
            val usia = usiaText.toIntOrNull()
            // Menghentikan proses jika usia bukan angka.
            if (usia == null) {
                Toast.makeText(this, "Usia harus berupa angka", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mengirim data yang sudah valid ke fungsi penyimpanan Firebase.
            prosesSimpanAnakFirebase(nama, usia)
        }
    }

    // ==================== BAGIAN PILIHAN AVATAR ====================
    private fun tampilkanPilihanAvatar() {
        // Menghapus pilihan lama agar tidak terjadi duplikasi View.
        containerAvatarAnak.removeAllViews()
        // Mengosongkan referensi ImageView avatar yang lama.
        avatarViews.clear()

        // Pilihan avatar dibuat dari daftar AvatarHelper, bukan ditulis satu per satu di XML.
        AvatarHelper.avatarIds.forEach { avatar ->
            // Membuat satu ImageView baru untuk setiap ID avatar.
            val avatarView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(72), dp(72)).apply {
                    setMargins(0, 0, dp(12), 0)
                }
                setBackgroundResource(R.drawable.bg_avatar_normal)
                // Menampilkan drawable berdasarkan ID avatar.
                setImageResource(AvatarHelper.getAvatarResource(avatar))
                contentDescription = "Pilih karakter ${avatar.toLabelAvatar()}"
                isClickable = true
                isFocusable = true
                setPadding(dp(10), dp(10), dp(10), dp(10))
                scaleType = ImageView.ScaleType.FIT_CENTER
                // Mengubah avatar aktif ketika gambar ditekan.
                setOnClickListener {
                    pilihAvatar(avatar)
                }
            }

            // Menyimpan referensi ImageView agar background-nya dapat diperbarui.
            avatarViews[avatar] = avatarView
            // Menambahkan ImageView ke container pada layout.
            containerAvatarAnak.addView(avatarView)
        }
    }

    private fun pilihAvatar(avatar: String) {
        // ID avatar yang dipilih disimpan ke field "avatar" di Firestore.
        // Menyimpan ID avatar yang sedang aktif.
        selectedAvatar = avatar

        // Memeriksa seluruh avatar untuk menentukan background selected/normal.
        avatarViews.forEach { (avatarId, avatarView) ->
            val background = if (avatarId == avatar) {
                R.drawable.bg_avatar_selected
            } else {
                R.drawable.bg_avatar_normal
            }
            // Menerapkan background sesuai status terpilih.
            avatarView.setBackgroundResource(background)
        }

        // Menjalankan animasi kecil hanya pada avatar yang dipilih.
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

    // ==================== BAGIAN SIMPAN DATA FIRESTORE ====================
    private fun prosesSimpanAnakFirebase(nama: String, usia: Int) {
        // UID Firebase Auth menghubungkan profil anak dengan akun pendamping.
        // Mengambil akun Firebase yang sedang aktif.
        val currentUser = auth.currentUser

        // Penyimpanan tidak dapat dilakukan tanpa sesi login.
        if (currentUser == null) {
            Toast.makeText(
                this,
                "Sesi login tidak ditemukan. Silakan login ulang.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Menyusun data sesuai nama field pada koleksi tb_anak.
        val anakData = hashMapOf(
            "id_pendamping" to currentUser.uid,
            "nama_anak" to nama,
            "usia" to usia,
            "avatar" to selectedAvatar
        )

        // Mencegah tombol ditekan berulang selama request berjalan.
        btnSimpanAnak.isEnabled = false
        // Memberi informasi bahwa proses penyimpanan sedang berlangsung.
        btnSimpanAnak.text = "Menyimpan..."

        // Menambahkan dokumen baru ke koleksi tb_anak.
        db.collection("tb_anak")
            .add(anakData)
            // Callback ketika dokumen berhasil dibuat.
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Profil $nama berhasil disimpan!",
                    Toast.LENGTH_SHORT
                ).show()
                // Kembali ke Dashboard agar profil baru dimuat melalui onResume.
                finish()
            }
            // Callback ketika proses Firestore gagal.
            .addOnFailureListener { e ->
                // Mengaktifkan kembali tombol agar penyimpanan dapat dicoba lagi.
                btnSimpanAnak.isEnabled = true
                btnSimpanAnak.text = "Simpan Profil Anak"
                Toast.makeText(
                    this,
                    "Gagal menyimpan: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // ==================== BAGIAN FUNGSI BANTUAN ====================
    private fun String.toLabelAvatar(): String {
        return removePrefix("char_").replace("_", " ")
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
