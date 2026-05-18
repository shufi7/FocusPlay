package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
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

    private lateinit var btnLogin: View
    private lateinit var btnRegister: View
    private lateinit var btnLoginGoogle: View

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
                    ErrorDialogHelper.showErrorDialog(
                        activity = this,
                        title = "Login Gagal",
                        message = "Token Google tidak ditemukan. Coba ulangi lagi ya."
                    )
                }

            } catch (e: ApiException) {
                loadingDialog.dismiss()
                ErrorDialogHelper.showErrorDialog(
                    activity = this,
                    title = "Google Sign-In Gagal",
                    message = "Terjadi masalah saat memilih akun Google. Coba ulangi lagi ya."
                )
            }

        } else {
            loadingDialog.dismiss()
            ErrorDialogHelper.showErrorDialog(
                activity = this,
                title = "Login Dibatalkan",
                message = "Kamu belum memilih akun Google untuk masuk."
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        session = SessionManager(this)
        loadingDialog = LoadingDialogHelper(this)

        if (auth.currentUser != null || session.isLogin()) {
            startActivity(Intent(this, PilihPeranActivity::class.java))
            finish()
            return
        }

        hubungkanView()
        setupGoogleLogin()
        aturAksiTombol()
    }

    private fun hubungkanView() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
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
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        }
    }

    private fun prosesLoginEmailPassword() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            ErrorDialogHelper.showErrorDialog(
                activity = this,
                title = "Email Kosong",
                message = "Masukkan email terlebih dahulu."
            )
            return
        }

        if (password.isEmpty()) {
            ErrorDialogHelper.showErrorDialog(
                activity = this,
                title = "Password Kosong",
                message = "Masukkan password terlebih dahulu."
            )
            return
        }

        loadingDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    session.simpanSesiLogin(
                        0,
                        user?.displayName ?: "Orang Tua",
                        user?.email ?: email
                    )

                    SuccessDialogHelper.showSuccessDialog(
                        activity = this,
                        title = "Login Berhasil!",
                        message = "Selamat datang di FocusPlay!"
                    ) {
                        startActivity(Intent(this, PilihPeranActivity::class.java))
                        finish()
                    }

                } else {
                    ErrorDialogHelper.showErrorDialog(
                        activity = this,
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

                    session.simpanSesiLogin(
                        0,
                        user?.displayName ?: "Orang Tua",
                        user?.email ?: ""
                    )

                    SuccessDialogHelper.showSuccessDialog(
                        activity = this,
                        title = "Login Berhasil!",
                        message = "Selamat datang, ${user?.displayName ?: "Orang Tua"}!"
                    ) {
                        startActivity(Intent(this, PilihPeranActivity::class.java))
                        finish()
                    }

                } else {
                    ErrorDialogHelper.showErrorDialog(
                        activity = this,
                        title = "Login Gagal",
                        message = "Akun belum berhasil masuk. Periksa koneksi internet atau coba lagi ya."
                    )
                }
            }
    }
}