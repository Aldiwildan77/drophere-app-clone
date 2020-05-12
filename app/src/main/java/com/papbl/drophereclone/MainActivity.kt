package com.papbl.drophereclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var btnPengumpulan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPengumpulan = findViewById(R.id.materialButton)
        btnPengumpulan.setOnClickListener {
            startActivity(Intent(this, PengumpulanActivity::class.java))
        }
    }
}
