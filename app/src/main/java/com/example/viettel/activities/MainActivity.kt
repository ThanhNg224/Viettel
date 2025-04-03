package com.example.viettel.activities

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.fragments.CaptureBackPhotoFragment
import com.example.viettel.fragments.DocumentSelectionFragment
import com.example.viettel.fragments.PlaceDocumentFragment
import com.google.android.material.snackbar.Snackbar
import com.example.viettel.fragments.CaptureFrontPhotoFragment

class MainActivity : AppCompatActivity() {
    // Store front photo from step 3
    private var frontBitmap: Bitmap? = null

    fun setFrontBitmap(bitmap: Bitmap?) {
        frontBitmap = bitmap
    }

    private var backBitmap: Bitmap? = null


    fun setBackBitmap(bitmap: Bitmap?) {
        backBitmap = bitmap
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ FULLSCREEN MODE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.setDecorFitsSystemWindows(false)
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

        // ✅ Language selector logic
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

        // ✅ Load initial fragment
        if (savedInstanceState == null) {
            replaceFragment(DocumentSelectionFragment())
        }

        // ✅ Handle Back button (same as system back)
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ✅ Handle Continue button (switch fragment manually)
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            when (currentFragment) {
                is DocumentSelectionFragment -> {
                    replaceFragment(PlaceDocumentFragment())
                }
                is PlaceDocumentFragment -> {
                    replaceFragment(CaptureFrontPhotoFragment())
                }
                is CaptureFrontPhotoFragment -> {
                    // Step 3 to Step 4
                    replaceFragment(CaptureBackPhotoFragment())
                }

                else -> {
                    // Optional: show toast or do nothing
                }
            }
        }
    }

    /**
     * Replaces fragment inside the container
     */
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Shows a custom snackbar when language is changed
     */
    private fun showLanguageSnackbar(message: String) {
        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main_layout)
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.black))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .setAction("OK") { }
            .show()
    }



}

