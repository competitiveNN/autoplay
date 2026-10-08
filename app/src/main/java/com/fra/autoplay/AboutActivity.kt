package com.fra.autoplay

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(true)
            it.title = getString(R.string.about_title)
        }

        val aboutText = findViewById<TextView>(R.id.about_text)
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            aboutText.text = getString(R.string.about_body, pInfo.versionName ?: "1.1.0")
        } catch (_: Exception) {
            aboutText.text = getString(R.string.about_body, "1.1.0")
        }

        val versionButton = findViewById<android.widget.Button>(R.id.about_version_button)
        versionButton.setOnClickListener {
            try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName ?: "1.1.0"
                val clipboard = getSystemService<ClipboardManager>()
                clipboard?.setPrimaryClip(ClipData.newPlainText("version", version))
            } catch (_: Exception) {}
            Toast.makeText(this, R.string.about_version_toast, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}