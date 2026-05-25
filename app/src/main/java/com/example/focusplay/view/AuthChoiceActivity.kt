package com.example.focusplay.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.profile.PilihPeranActivity
import com.example.focusplay.auth.LoginBottomSheetFragment
import com.example.focusplay.auth.RegisterBottomSheetFragment
import com.google.firebase.auth.FirebaseAuth
import com.example.focusplay.utils.SessionManager

class AuthChoiceActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var session: SessionManager

    private lateinit var btnLogin: View
    private lateinit var btnRegister: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_choice)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        session = SessionManager(this)

        if (auth.currentUser != null || session.isLogin()) {
            bukaPilihPeran()
            return
        }

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener {
            LoginBottomSheetFragment().show(
                supportFragmentManager,
                "LoginBottomSheet"
            )
        }

        btnRegister.setOnClickListener {
            RegisterBottomSheetFragment().show(
                supportFragmentManager,
                "RegisterBottomSheet"
            )
        }
    }

    private fun bukaPilihPeran() {
        startActivity(android.content.Intent(this, PilihPeranActivity::class.java))
        finish()
    }
}