package com.example.focusplay.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.focusplay.R
import com.example.focusplay.model.Anak
import com.example.focusplay.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvWaktuBermain: TextView
    private lateinit var tvRataAkurasi: TextView

    // Komponen untuk Daftar Anak
    private lateinit var rvDaftarAnak: RecyclerView
    private lateinit var anakAdapter: AnakAdapter
    private var listAnak = ArrayList<Anak>()

    private lateinit var btnTambahAnak: LinearLayout
    private lateinit var btnRiwayatPermainan: LinearLayout
    private lateinit var btnPengaturanPermainan: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var chartWeekly: WeeklyLineChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        hubungkanView()
        setupRecyclerView()
        tampilkanNamaUser()
        tampilkanRingkasanSementara()
        tampilkanGrafikSesi()
        aturAksiTombol()

        ambilDataAnakRealtime()
    }

    private fun hubungkanView() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        tvWaktuBermain = findViewById(R.id.tvWaktuBermain)
        tvRataAkurasi = findViewById(R.id.tvRataAkurasi)

        rvDaftarAnak = findViewById(R.id.rvDaftarAnak)

        btnTambahAnak = findViewById(R.id.btnTambahAnak)
        btnRiwayatPermainan = findViewById(R.id.btnRiwayatPermainan)
        btnPengaturanPermainan = findViewById(R.id.btnPengaturanPermainan)
        btnLogout = findViewById(R.id.btnLogout)

        chartWeekly = findViewById(R.id.chartWeekly)
    }

    private fun setupRecyclerView() {
        rvDaftarAnak.layoutManager = LinearLayoutManager(this)
        anakAdapter = AnakAdapter(
            listAnak,
            onClickAnak = { anakYangDipilih ->
                tampilkanPilihanMenu(anakYangDipilih)
            },
            onLongClickAnak = { anakYangDipilih ->
                tampilkanDialogHapus(anakYangDipilih)
            }
        )
        rvDaftarAnak.adapter = anakAdapter
    }

    private fun tampilkanNamaUser() {
        val namaUser = session.getNamaUser()
        tvWelcomeName.text = if (namaUser.isNotEmpty()) {
            "Halo, $namaUser!"
        } else {
            "Halo, Orang Tua!"
        }
    }

    private fun tampilkanRingkasanSementara() {
        tvWaktuBermain.text = "0 menit"
        tvRataAkurasi.text = "0%"
    }

    private fun tampilkanGrafikSesi() {
        // Saya kirimkan list kosong agar teks "Belum ada data..." muncul
        // dan garis zigzag dari data palsumu hilang.
        chartWeekly.setData(emptyList())
    }

    private fun aturAksiTombol() {
        btnTambahAnak.setOnClickListener {
            startActivity(Intent(this, TambahAnakActivity::class.java))
        }

        btnRiwayatPermainan.setOnClickListener {
            startActivity(Intent(this, RiwayatPermainanActivity::class.java))
        }

        btnPengaturanPermainan.setOnClickListener {
            // Nanti dihubungkan ke PengaturanPermainanActivity
        }

        btnLogout.setOnClickListener {
            session.logout()
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // --- FUNGSI FIREBASE ---

    private fun ambilDataAnakRealtime() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", currentUser.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listAnak.clear()
                    for (doc in snapshots) {
                        try {
                            val anak = doc.toObject(Anak::class.java)
                            anak.id_dokumen = doc.id
                            listAnak.add(anak)
                        } catch (e: Exception) { }
                    }
                    anakAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun tampilkanPilihanMenu(anak: Anak) {
        val opsi = arrayOf("Masuk Area Anak 🎮", "Lihat Evaluasi AI 🤖")
        AlertDialog.Builder(this)
            .setTitle("Menu Profil: ${anak.nama_anak}")
            .setItems(opsi) { _, index ->
                when (index) {
                    0 -> {
                        val intent = Intent(this, DashboardAnakActivity::class.java)
                        intent.putExtra("ID_ANAK", anak.id_dokumen)
                        intent.putExtra("NAMA_ANAK", anak.nama_anak)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this, EvaluasiActivity::class.java)
                        intent.putExtra("ID_ANAK", anak.id_dokumen)
                        intent.putExtra("NAMA_ANAK", anak.nama_anak)
                        startActivity(intent)
                    }
                }
            }
            .show()
    }

    private fun tampilkanDialogHapus(anak: Anak) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Profil")
            .setMessage("Apakah kamu yakin ingin menghapus profil ${anak.nama_anak}?")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("tb_anak").document(anak.id_dokumen)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data dihapus!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}