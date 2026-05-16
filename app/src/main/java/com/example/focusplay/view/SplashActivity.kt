package com.example.focusplay.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
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

        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        val ivStar = findViewById<ImageView>(R.id.ivStar)
        val ivBlue = findViewById<ImageView>(R.id.ivBlue)
        val ivRed = findViewById<ImageView>(R.id.ivRed)
        val ivPurple = findViewById<ImageView>(R.id.ivPurple)
        val ivMushroom = findViewById<ImageView>(R.id.ivMushroom)

        // Animasi sederhana
        animateFloating(ivStar, 0)
        animateFloating(ivBlue, 200)
        animateFloating(ivRed, 400)
        animateFloating(ivPurple, 600)
        animateFloating(ivMushroom, 800)
        animateFloating(ivLogo, 100)

        handler.postDelayed({
            if (session.isLogin()) {
                startActivity(Intent(this, PilihPeranActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, splashDelay)
    }

    private fun animateFloating(view: View, startDelay: Long) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, -20f, 0f)
        animator.duration = 1800
        animator.startDelay = startDelay
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}