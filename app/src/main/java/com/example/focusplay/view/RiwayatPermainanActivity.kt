package com.example.focusplay.view

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class RiwayatPermainanActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_permainan)

        ivBack = findViewById(R.id.ivBackRiwayat)

        ivBack.setOnClickListener {
            finish()
        }
    }
}