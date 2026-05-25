package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private val splashDelay = 2500L
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        session = SessionManager(this)

        handler.postDelayed({
            val tujuan = if (session.isLogin()) {
                Intent(this, PilihPeranActivity::class.java)
            } else {
                Intent(this, AuthChoiceActivity::class.java)
            }

            startActivity(tujuan)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, splashDelay)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}