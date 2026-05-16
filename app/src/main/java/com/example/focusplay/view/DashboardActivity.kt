package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
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

        session = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvWelcomeName.text = "Halo, ${session.getNamaUser()}!"

        // Konfigurasi List RecyclerView
        rvDaftarAnak.layoutManager = LinearLayoutManager(this)
        anakAdapter = AnakAdapter(listAnak)
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

        // Jalankan pemantauan data anak
        ambilDataAnakRealtime()
    }

    private fun ambilDataAnakRealtime() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        // Hanya mengambil anak yang id_pendamping-nya COCOK dengan akun Google yang sedang aktif
        db.collection("tb_anak")
            .whereEqualTo("id_pendamping", currentUser.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this@DashboardActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listAnak.clear() // Bersihkan list lama agar tidak menumpuk duplikat
                    for (doc in snapshots) {
                        // Pasang sistem keamanan (try-catch)
                        try {
                            val anak = doc.toObject(Anak::class.java)
                            listAnak.add(anak)
                        } catch (e: Exception) {
                            // Abaikan dokumen yang formatnya salah/rusak, jangan matikan aplikasi
                        }
                    }
                    anakAdapter.notifyDataSetChanged() // Perbarui daftar di layar HP
                }
            }
    }
}