package com.example.focusplay.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.model.TambahAnakResponse // <-- BARIS INI WAJIB ADA
import com.example.focusplay.network.ApiClient
import com.example.focusplay.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahAnakActivity : AppCompatActivity() {

    private lateinit var etNamaAnak: EditText
    private lateinit var etUsiaAnak: EditText
    private lateinit var btnSimpanAnak: Button
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_anak)

        session = SessionManager(this)
        etNamaAnak = findViewById(R.id.etNamaAnak)
        etUsiaAnak = findViewById(R.id.etUsiaAnak)
        btnSimpanAnak = findViewById(R.id.btnSimpanAnak)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        btnSimpanAnak.setOnClickListener {
            val nama = etNamaAnak.text.toString().trim()
            val usia = etUsiaAnak.text.toString().trim()

            if (nama.isEmpty() || usia.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prosesSimpanAnak(nama, usia.toInt())
        }
    }

    private fun prosesSimpanAnak(nama: String, usia: Int) {
        val requestData = HashMap<String, Any>()
        // Mengambil ID orang tua dari session
        requestData["id_pendamping"] = session.getUserId()
        requestData["nama_anak"] = nama
        requestData["usia"] = usia

        ApiClient.instance.tambahAnak(requestData).enqueue(object : Callback<TambahAnakResponse> {
            override fun onResponse(call: Call<TambahAnakResponse>, response: Response<TambahAnakResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@TambahAnakActivity, "Profil $nama berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TambahAnakActivity, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TambahAnakResponse>, t: Throwable) {
                Toast.makeText(this@TambahAnakActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}