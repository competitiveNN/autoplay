package com.fra.autoplay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(true)
            it.title = getString(R.string.about_title)
        }

        val versionButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.about_version_button)
        versionButton.setOnClickListener {
            Toast.makeText(this, R.string.about_version_toast, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}