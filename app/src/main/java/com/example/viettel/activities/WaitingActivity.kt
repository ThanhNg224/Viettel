// app/src/main/java/com/example/viettel/activities/WaitingActivity.kt
package com.example.viettel.activities

import KioskHttpServer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.viettel.R
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import kotlin.isInitialized
import kotlin.jvm.java
import kotlin.let

class WaitingActivity : AppCompatActivity() {

    private lateinit var httpServer: KioskHttpServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        // Fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }

        // Language menu (giống MainActivity)
        val languageSelector = findViewById<LinearLayout>(R.id.ChonNgonNgu)
        languageSelector.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.language_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                val languageText = languageSelector.findViewById<TextView>(R.id.languageText)
                val flagImage = languageSelector.findViewById<ImageView>(R.id.languageFlag)
                when (item.itemId) {
                    R.id.lang_vi -> {
                        languageText.text = "Tiếng Việt"
                        flagImage.setImageResource(R.drawable.ic_vietnam_flag)
                        showLanguageSnackbar("Đã chọn: Tiếng Việt")
                        true
                    }
                    R.id.lang_en -> {
                        languageText.text = "English"
                        flagImage.setImageResource(R.drawable.uk_flag)
                        showLanguageSnackbar("Selected: English")
                        true
                    }
                    R.id.lang_fr -> {
                        languageText.text = "Français"
                        flagImage.setImageResource(R.drawable.france_flag)
                        showLanguageSnackbar("Langue sélectionnée : Français")
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Start HTTP server để chờ trigger từ web trong màn waiting
        httpServer = KioskHttpServer(8088) { action ->
            runOnUiThread {
                // Khi web trigger, qua MainActivity và kết thúc Waiting
                val intent = Intent(this, MainActivity::class.java)
                    .putExtra("ACTION", action) // ví dụ: "START_BUY_SIM"
                startActivity(intent)
                finish()
            }
        }
        httpServer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::httpServer.isInitialized) try { httpServer.stop() } catch (_: Throwable) {}
    }

    private fun showLanguageSnackbar(message: String) {
        val root = findViewById<View>(androidx.appcompat.R.id.content)
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.black))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .setAction("OK") { }
            .show()
    }
}
