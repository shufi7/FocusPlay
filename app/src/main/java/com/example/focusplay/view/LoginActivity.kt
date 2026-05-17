package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.ErrorDialogHelper
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

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnLoginGoogle: TextView

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Toast.makeText(this, "Token Google tidak ditemukan", Toast.LENGTH_SHORT).show()
                }

            } catch (e: ApiException) {
                ErrorDialogHelper.showErrorDialog(
                    activity = this,
                    title = "Google Sign-In Gagal",
                    message = "Terjadi masalah saat memilih akun Google. Coba ulangi lagi ya."
                )
            }
        } else {
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

        session = SessionManager(this)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null || session.isLogin()) {
            startActivity(Intent(this, PilihPeranActivity::class.java))
            finish()
            return
        }

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            Toast.makeText(
                this,
                "Login email/password sedang disiapkan. Gunakan Google dulu.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

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
                        message = "Yeay! Kamu berhasil masuk ke FocusPlay."
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