package com.example.focusplay.view

import android.os.Bundle
import android.view.MotionEvent
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

    private var bottomSheetSedangTerbuka = false

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

        aturTombolLogin()
        aturTombolRegister()
    }

    private fun aturTombolLogin() {
        btnLogin.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    efekTekan(view)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    efekLepas(view)
                    view.performClick()

                    if (!bottomSheetSedangTerbuka) {
                        bottomSheetSedangTerbuka = true

                        LoginBottomSheetFragment().show(
                            supportFragmentManager,
                            "LoginBottomSheet"
                        )

                        view.postDelayed({
                            bottomSheetSedangTerbuka = false
                        }, 700)
                    }

                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    efekLepas(view)
                    true
                }

                else -> true
            }
        }
    }

    private fun aturTombolRegister() {
        btnRegister.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    efekTekan(view)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    efekLepas(view)
                    view.performClick()

                    if (!bottomSheetSedangTerbuka) {
                        bottomSheetSedangTerbuka = true

                        RegisterBottomSheetFragment().show(
                            supportFragmentManager,
                            "RegisterBottomSheet"
                        )

                        view.postDelayed({
                            bottomSheetSedangTerbuka = false
                        }, 700)
                    }

                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    efekLepas(view)
                    true
                }

                else -> true
            }
        }
    }

    private fun efekTekan(view: View) {
        view.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .alpha(0.85f)
            .setDuration(50)
            .start()
    }

    private fun efekLepas(view: View) {
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(50)
            .start()
    }

    private fun bukaPilihPeran() {
        startActivity(android.content.Intent(this, PilihPeranActivity::class.java))
        finish()
    }
}