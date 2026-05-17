package com.example.focusplay.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.focusplay.R
import com.example.focusplay.model.Anak
import com.example.focusplay.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcomeName: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnKeTambahAnak: Button
    private lateinit var rvDaftarAnak: RecyclerView
    private var tvEmptyData: TextView? = null

    private lateinit var session: SessionManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var anakAdapter: AnakAdapter
    private val listAnak = ArrayList<Anak>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        btnLogout = findViewById(R.id.btnLogout)
        btnKeTambahAnak = findViewById(R.id.btnKeTambahAnak)
        rvDaftarAnak = findViewById(R.id.rvDaftarAnak)
        tvEmptyData = findViewById(R.id.tvEmptyData)

        session = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvWelcomeName.text = "Halo, ${session.getNamaUser()}!"

        rvDaftarAnak.layoutManager = LinearLayoutManager(this)
        anakAdapter = AnakAdapter(
            listAnak,
            onClickAnak = { anakYangDipilih ->
                // SEKARANG KITA KIRIM NAMA DAN ID DOKUMEN ANAK NYA KEDASHBOARD ANAK
                val intent = Intent(this, DashboardAnakActivity::class.java)
                intent.putExtra("ID_ANAK", anakYangDipilih.id_dokumen)
                intent.putExtra("NAMA_ANAK", anakYangDipilih.nama_anak)
                startActivity(intent)
            },
            onLongClickAnak = { anakYangDipilih ->
                tampilkanDialogHapus(anakYangDipilih)
            }
        )
        rvDaftarAnak.adapter = anakAdapter

        btnKeTambahAnak.setOnClickListener {
            startActivity(Intent(this, TambahAnakActivity::class.java))
        }

        btnLogout.setOnClickListener {
            session.logout()
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        ambilDataAnakRealtime()
    }

    private fun ambilDataAnakRealtime() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", currentUser.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this@DashboardActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listAnak.clear()
                    for (doc in snapshots) {
                        try {
                            val anak = doc.toObject(Anak::class.java)
                            anak.id_dokumen = doc.id
                            listAnak.add(anak)
                        } catch (e: Exception) {
                            // Abaikan error format
                        }
                    }
                    anakAdapter.notifyDataSetChanged()

                    if (listAnak.isEmpty()) {
                        tvEmptyData?.visibility = View.VISIBLE
                    } else {
                        tvEmptyData?.visibility = View.GONE
                    }
                }
            }
    }

    private fun tampilkanDialogHapus(anak: Anak) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Profil")
            .setMessage("Apakah kamu yakin ingin menghapus profil ${anak.nama_anak}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusDataAnak(anak.id_dokumen)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusDataAnak(idDokumen: String) {
        db.collection("tb_anak").document(idDokumen)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}