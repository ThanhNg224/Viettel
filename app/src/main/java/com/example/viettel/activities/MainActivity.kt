package com.example.viettel.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.databinding.ActivityMainBinding
import com.example.viettel.feature.feedback.presentation.ui.EndFragment
import com.example.viettel.feature.feedback.presentation.ui.FeedbackFragment
import com.example.viettel.feature.feedback.presentation.ui.ServiceEvaluationFragment
import com.example.viettel.fragments.step1_2.DocumentSelectionFragment
import com.example.viettel.fragments.step1_2.PlaceDocumentFragment
import com.example.viettel.fragments.step3_4.CaptureBackPhotoFragment
import com.example.viettel.fragments.step3_4.CaptureFrontPhotoFragment
import com.example.viettel.fragments.step5.EidDetailsFragment
import com.example.viettel.fragments.step5.NfcFragment
import com.example.viettel.fragments.step6.PdfSignFragment
import com.example.viettel.fragments.step6.PortraitComparisonFragment
import com.example.viettel.fragments.step6.PortraitLivenessFragment
import com.example.viettel.fragments.step6.VideoCallFragment
import com.example.viettel.fragments.step7.PaymentFragment
import com.example.viettel.fragments.step7.QrCodePaymentFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("KIOSK_ID", "Real serial: ${getHardwareSerial()}")

        fullScreenMore()
        setupLanguageMenu()

        if (savedInstanceState == null) {
            replaceFragment(DocumentSelectionFragment())
        }

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnContinue.setOnClickListener { onContinuePressed() }
    }

    override fun onResume() {
        super.onResume()
        fullScreenMore()
    }

    private fun setupLanguageMenu() {
        binding.ChonNgonNgu.setOnClickListener { anchor ->
            PopupMenu(this, anchor).apply {
                menuInflater.inflate(R.menu.language_menu, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.lang_vi -> {
                            @Suppress("SetTextI18n")
                            binding.languageText.text = "Tieng Viet"
                            binding.languageFlag.setImageResource(R.drawable.ic_vietnam_flag)
                            showLanguageSnackbar("Da chon: Tieng Viet")
                            true
                        }

                        R.id.lang_en -> {
                            @Suppress("SetTextI18n")
                            binding.languageText.text = "English"
                            binding.languageFlag.setImageResource(R.drawable.uk_flag)
                            showLanguageSnackbar("Selected: English")
                            true
                        }

                        R.id.lang_fr -> {
                            @Suppress("SetTextI18n")
                            binding.languageText.text = "Francais"
                            binding.languageFlag.setImageResource(R.drawable.france_flag)
                            showLanguageSnackbar("Langue selectionnee : Francais")
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }
    }

    private fun onContinuePressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        when (currentFragment) {
            is DocumentSelectionFragment -> replaceFragment(PlaceDocumentFragment())
            is PlaceDocumentFragment -> replaceFragment(CaptureFrontPhotoFragment())
            is CaptureFrontPhotoFragment -> replaceFragment(CaptureBackPhotoFragment())
            is CaptureBackPhotoFragment -> replaceFragment(NfcFragment())
            is NfcFragment -> replaceFragment(EidDetailsFragment())
            is EidDetailsFragment -> replaceFragment(PortraitLivenessFragment())
            is PortraitLivenessFragment -> replaceFragment(PortraitComparisonFragment())
            is PortraitComparisonFragment -> replaceFragment(PdfSignFragment())
            is PdfSignFragment -> replaceFragment(VideoCallFragment())
            is VideoCallFragment -> replaceFragment(PaymentFragment())
            is PaymentFragment -> replaceFragment(ServiceEvaluationFragment())
            is QrCodePaymentFragment -> replaceFragment(ServiceEvaluationFragment())
            is ServiceEvaluationFragment -> replaceFragment(EndFragment())
            is FeedbackFragment -> {
                if (currentFragment.isFeedbackValid()) {
                    currentFragment.onContinuePressed()
                } else {
                    Toast.makeText(this, "Vui long chon it nhat mot ly do", Toast.LENGTH_SHORT).show()
                }
            }

            is EndFragment ->
                Toast.makeText(this, "Da den buoc cuoi", Toast.LENGTH_SHORT).show()

            else ->
                Toast.makeText(this, "Khong xac dinh buoc hien tai", Toast.LENGTH_SHORT).show()
        }
    }

    fun fullScreenMore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showLanguageSnackbar(message: String) {
        Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.black))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .setAction("OK") { }
            .show()
    }

    fun animateToStep(step: Int) {
        try {
            val progressLine = binding.root.findViewById<View>(R.id.progressLine)
            val container = binding.root.findViewById<View>(R.id.progressBarContainer)

            if (progressLine != null && container != null) {
                container.post {
                    val totalSteps = 8
                    val stepWidth = (container.width.toFloat() / totalSteps) * step
                    val lp = progressLine.layoutParams
                    lp.width = stepWidth.toInt()
                    progressLine.layoutParams = lp
                }
            } else {
                Log.d("MainActivity", "Progress bar views not found in layout")
            }
        } catch (e: Exception) {
            Log.d("MainActivity", "Progress bar animation error: ${e.message}")
        }
    }

    fun setContinueVisible(visible: Boolean) {
        binding.btnContinue.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setContinueEnabled(enabled: Boolean) {
        binding.btnContinue.isEnabled = enabled
    }

    fun setBackVisible(visible: Boolean) {
        binding.btnBack.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setBackEnabled(enabled: Boolean) {
        binding.btnBack.isEnabled = enabled
    }

    private fun getHardwareSerial(): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop ro.serialno")
            process.inputStream.bufferedReader().readLine().trim()
        } catch (e: Exception) {
            Log.e("DeviceID", "Failed to get hardware serial", e)
            "UNKNOWN"
        }
    }

    @Suppress("unused")
    private fun resolveDeviceId(): String {
        val tm = getSystemService(TELEPHONY_SERVICE) as? TelephonyManager

        try {
            @Suppress("DEPRECATION")
            val imei = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                tm?.imei ?: ""
            } else {
                ""
            }
            if (imei.isNotBlank()) return imei
        } catch (_: SecurityException) {
            Log.w("DeviceID", "No permission for IMEI")
        }

        try {
            @Suppress("DEPRECATION")
            val serial = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Build.getSerial()
            } else {
                ""
            }
            if (serial.isNotBlank() && serial != "unknown") return serial
        } catch (_: Exception) {
        }

        @Suppress("HardwareIds")
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }
}
