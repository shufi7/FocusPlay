package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.profile.PilihPeranActivity
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

        val ivLogoSplash = findViewById<ImageView>(R.id.ivLogoSplash)
        mulaiAnimasiLogo(ivLogoSplash)

        handler.postDelayed({
            val tujuan = if (session.isLogin()) {
                Intent(this, PilihPeranActivity::class.java)
            } else {
                Intent(this, AuthChoiceActivity::class.java)
            }

            startActivity(tujuan)
            finish()
        }, splashDelay)
    }

    private fun mulaiAnimasiLogo(view: ImageView) {
        // Animasi Muncul (Fade In)
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        // Animasi Naik Turun (Bobbing)
        val movingDistance = 30f // Jarak gerak 30px
        
        view.animate()
            .translationY(movingDistance)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction(object : Runnable {
                override fun run() {
                    animasiKebalikan(view, movingDistance)
                }
            })
            .start()
    }

    private fun animasiKebalikan(view: ImageView, distance: Float) {
        val target = if (view.translationY > 0) -distance else distance
        
        view.animate()
            .translationY(target)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                animasiKebalikan(view, distance)
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
