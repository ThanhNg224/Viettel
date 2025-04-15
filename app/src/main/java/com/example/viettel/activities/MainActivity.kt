package com.example.viettel.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.fragments.CaptureBackPhotoFragment
import com.example.viettel.fragments.CaptureFrontPhotoFragment
import com.example.viettel.fragments.DocumentSelectionFragment
import com.example.viettel.fragments.EidDetailsFragment
import com.example.viettel.fragments.NfcFragment
import com.example.viettel.fragments.PdfSignFragment
import com.example.viettel.fragments.PlaceDocumentFragment
import com.example.viettel.fragments.PortraitComparisonFragment
import com.example.viettel.fragments.PortraitLivenessFragment
import com.google.android.material.snackbar.Snackbar
import org.jmrtd.lds.icao.MRZInfo

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FULLSCREEN MODE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

        // Language
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

        // Document Selection
        if (savedInstanceState == null) {
            replaceFragment(DocumentSelectionFragment())
        }

        // Back Button: Directly trigger the system Back action.
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Continue Button
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            when (currentFragment) {
                is DocumentSelectionFragment -> replaceFragment(PdfSignFragment())
                is PlaceDocumentFragment -> replaceFragment(CaptureFrontPhotoFragment())
                is CaptureFrontPhotoFragment -> replaceFragment(CaptureBackPhotoFragment())
                is NfcFragment -> replaceFragment(EidDetailsFragment())
                is EidDetailsFragment -> replaceFragment(PortraitLivenessFragment())
                is PortraitLivenessFragment -> replaceFragment(PortraitComparisonFragment())
                is PortraitComparisonFragment -> replaceFragment(PdfSignFragment())
                is PdfSignFragment -> Toast.makeText(this, "Đã đến bước cuối", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "Không xác định bước hiện tại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Replace the fragment in the fragment container.
     */
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Displays a custom Snackbar when a language is chosen.
     */
    private fun showLanguageSnackbar(message: String) {
        val rootLayout = findViewById<ConstraintLayout>(R.id.main_layout)
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.black))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .setAction("OK") { }
            .show()
    }

    /**
     * Call this function to animate custom progress bar to a given step.
     *
     *
     */
    fun animateToStep(step: Int) {
        val progressLine = findViewById<View>(R.id.progressLine)
        val container = findViewById<View>(R.id.progressBarContainer)
        container?.post {
            val totalSteps = 8
            val stepWidth = (container.width.toFloat() / totalSteps) * step
            val lp = progressLine.layoutParams
            lp.width = stepWidth.toInt()
            progressLine.layoutParams = lp
        }
    }

    /**
     * When OCR is successful launch the NFC step.
     */
    fun launchNFCStep(mrzInfo: MRZInfo) {
        // Do not log the entire mrzInfo.toString(); log just some fields if needed
        Log.d("MainActivity", "MRZ read: Document Number = ${mrzInfo.documentNumber}")
        replaceFragment(NfcFragment.newInstance(mrzInfo))
        animateToStep(5)
    }
    fun setContinueVisible(visible: Boolean) {
        findViewById<Button>(R.id.btnContinue)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }


}
