package com.example.focusplay.view

import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.ErrorDialogHelper
import com.example.focusplay.utils.SuccessDialogHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etKonfirmasiPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleKonfirmasiPassword: ImageButton
    private lateinit var btnDaftar: Button
    private lateinit var tvMasukSini: TextView

    private var passwordTerlihat = false
    private var konfirmasiPasswordTerlihat = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etNama = findViewById(R.id.etNamaRegister)
        etEmail = findViewById(R.id.etEmailRegister)
        etPassword = findViewById(R.id.etPasswordRegister)
        etKonfirmasiPassword = findViewById(R.id.etKonfirmasiPasswordRegister)
        btnTogglePassword = findViewById(R.id.btnTogglePasswordRegister)
        btnToggleKonfirmasiPassword = findViewById(R.id.btnToggleKonfirmasiPasswordRegister)
        btnDaftar = findViewById(R.id.btnProsesRegister)
        tvMasukSini = findViewById(R.id.tvMasukSini)

        tvMasukSini.setOnClickListener {
            finish()
        }

        btnTogglePassword.setOnClickListener {
            passwordTerlihat = !passwordTerlihat
            aturTampilanPassword(etPassword, btnTogglePassword, passwordTerlihat)
        }

        btnToggleKonfirmasiPassword.setOnClickListener {
            konfirmasiPasswordTerlihat = !konfirmasiPasswordTerlihat
            aturTampilanPassword(
                etKonfirmasiPassword,
                btnToggleKonfirmasiPassword,
                konfirmasiPasswordTerlihat
            )
        }

        btnDaftar.setOnClickListener {
            validasiDanDaftar()
        }
    }

    private fun validasiDanDaftar() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val konfirmasiPassword = etKonfirmasiPassword.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"
            etNama.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password wajib diisi"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            etPassword.requestFocus()
            return
        }

        if (konfirmasiPassword.isEmpty()) {
            etKonfirmasiPassword.error = "Konfirmasi password wajib diisi"
            etKonfirmasiPassword.requestFocus()
            return
        }

        if (password != konfirmasiPassword) {
            etKonfirmasiPassword.error = "Konfirmasi password tidak sama"
            etKonfirmasiPassword.requestFocus()
            return
        }

        prosesRegisterFirebase(nama, email, password)
    }

    private fun prosesRegisterFirebase(nama: String, email: String, password: String) {
        btnDaftar.isEnabled = false
        btnDaftar.text = "Memproses..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnDaftar.isEnabled = true
                btnDaftar.text = "Daftar Sekarang"

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nama)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            SuccessDialogHelper.showSuccessDialog(
                                activity = this,
                                title = "Pendaftaran Berhasil!",
                                message = "Akun berhasil dibuat. Silakan masuk menggunakan email dan password kamu."
                            ) {
                                auth.signOut()
                                finish()
                            }
                        }
                } else {
                    val pesanError = when {
                        task.exception?.message?.contains("email address is already in use", true) == true -> {
                            "Email ini sudah digunakan. Silakan gunakan email lain atau masuk dengan akun tersebut."
                        }

                        task.exception?.message?.contains("badly formatted", true) == true -> {
                            "Format email belum benar."
                        }

                        task.exception?.message?.contains("password", true) == true -> {
                            "Password belum sesuai. Gunakan minimal 6 karakter."
                        }

                        else -> {
                            task.exception?.message ?: "Akun belum berhasil dibuat. Coba lagi."
                        }
                    }

                    ErrorDialogHelper.showErrorDialog(
                        activity = this,
                        title = "Pendaftaran Gagal",
                        message = pesanError
                    )
                }
            }
    }

    private fun aturTampilanPassword(
        editText: EditText,
        button: ImageButton,
        terlihat: Boolean
    ) {
        if (terlihat) {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            button.setImageResource(R.drawable.ic_eye_off)
        } else {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.ic_eye)
        }

        editText.setSelection(editText.text.length)
    }
}