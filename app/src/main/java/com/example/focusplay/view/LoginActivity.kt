package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.ErrorDialogHelper
import com.example.focusplay.utils.LoadingDialogHelper
import com.example.focusplay.utils.SessionManager
import com.example.focusplay.utils.SuccessDialogHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var session: SessionManager
    private lateinit var loadingDialog: LoadingDialogHelper

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageButton

    private lateinit var btnLogin: View
    private lateinit var btnRegister: TextView
    private lateinit var btnLoginGoogle: View

    private var passwordTerlihat = false

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {
            loadingDialog.show()

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    loadingDialog.dismiss()
                    tampilkanError(
                        title = "Login Gagal",
                        message = "Token Google tidak ditemukan. Coba ulangi lagi ya."
                    )
                }

            } catch (e: ApiException) {
                loadingDialog.dismiss()
                tampilkanError(
                    title = "Google Sign-In Gagal",
                    message = "Terjadi masalah saat memilih akun Google. Coba ulangi lagi ya."
                )
            }

        } else {
            loadingDialog.dismiss()
            tampilkanError(
                title = "Login Dibatalkan",
                message = "Kamu belum memilih akun Google untuk masuk."
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        session = SessionManager(this)
        loadingDialog = LoadingDialogHelper(this)

        if (auth.currentUser != null || session.isLogin()) {
            bukaPilihPeran()
            return
        }

        hubungkanView()
        setupGoogleLogin()
        aturAksiTombol()
    }

    private fun hubungkanView() {
        etEmail = findViewById(R.id.etEmailLogin)
        etPassword = findViewById(R.id.etPasswordLogin)
        btnTogglePassword = findViewById(R.id.btnTogglePasswordLogin)

        btnLogin = findViewById(R.id.btnProsesLogin)
        btnRegister = findViewById(R.id.tvDaftarSini)
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle)
    }

    private fun setupGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun aturAksiTombol() {
        btnLogin.setOnClickListener {
            prosesLoginEmailPassword()
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLoginGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        }

        btnTogglePassword.setOnClickListener {
            passwordTerlihat = !passwordTerlihat
            aturTampilanPassword()
        }
    }

    private fun prosesLoginEmailPassword() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password wajib diisi"
            etPassword.requestFocus()
            return
        }

        loadingDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val namaUser = user?.displayName ?: ambilNamaDariEmail(user?.email ?: email)
                    val emailUser = user?.email ?: email

                    session.simpanSesiLogin(
                        0,
                        namaUser,
                        emailUser
                    )

                    SuccessDialogHelper.showSuccessDialog(
                        activity = this,
                        title = "Login Berhasil!",
                        message = "Selamat datang di FocusPlay!"
                    ) {
                        bukaPilihPeran()
                    }

                } else {
                    tampilkanError(
                        title = "Login Gagal",
                        message = "Email atau password belum sesuai. Coba periksa kembali ya."
                    )
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val namaUser = user?.displayName ?: "Pengguna"
                    val emailUser = user?.email ?: ""

                    session.simpanSesiLogin(
                        0,
                        namaUser,
                        emailUser
                    )

                    SuccessDialogHelper.showSuccessDialog(
                        activity = this,
                        title = "Login Berhasil!",
                        message = "Selamat datang, ${ambilNamaPanggilan(namaUser)}!"
                    ) {
                        bukaPilihPeran()
                    }

                } else {
                    tampilkanError(
                        title = "Login Gagal",
                        message = "Akun belum berhasil masuk. Periksa koneksi internet atau coba lagi ya."
                    )
                }
            }
    }

    private fun aturTampilanPassword() {
        if (passwordTerlihat) {
            etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        } else {
            etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_eye)
        }

        etPassword.setSelection(etPassword.text.length)
    }

    private fun ambilNamaPanggilan(namaLengkap: String): String {
        return namaLengkap
            .trim()
            .split(" ")
            .firstOrNull()
            ?: "Pengguna"
    }

    private fun ambilNamaDariEmail(email: String): String {
        return email
            .substringBefore("@")
            .replace(".", " ")
            .replace("_", " ")
            .trim()
            .replaceFirstChar { it.uppercase() }
    }

    private fun bukaPilihPeran() {
        startActivity(Intent(this, PilihPeranActivity::class.java))
        finish()
    }

    private fun tampilkanError(title: String, message: String) {
        ErrorDialogHelper.showErrorDialog(
            activity = this,
            title = title,
            message = message
        )
    }
}