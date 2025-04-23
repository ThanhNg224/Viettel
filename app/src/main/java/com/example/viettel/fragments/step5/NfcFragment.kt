@file:Suppress("DEPRECATION")

package com.example.viettel.fragments.step5

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.viewmodel.DocumentViewModel
import io.reactivex.disposables.CompositeDisposable
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.ISO7816
import org.jmrtd.AccessDeniedException
import org.jmrtd.BACDeniedException
import org.jmrtd.PACEException
import org.jmrtd.lds.icao.MRZInfo
import org.spongycastle.jce.provider.BouncyCastleProvider
import vn.leeon.eidsdk.data.Eid
import vn.leeon.eidsdk.facade.EidCallback
import vn.leeon.eidsdk.facade.EidFacade
import java.security.Security

@Suppress("DEPRECATION")
class NfcFragment : Fragment() {

    private var mrzInfo: MRZInfo? = null

    private val disposable = CompositeDisposable()
    private var progressBar: ProgressBar? = null
    private var txtStatus: TextView? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private val docViewModel: DocumentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            val mrz = arguments?.getSerializable(ARG_MRZ_INFO) as? MRZInfo


            if (mrz != null) mrzInfo = mrz
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_nfc, container, false).apply {
        progressBar = findViewById(R.id.progressBar)
        txtStatus = findViewById(R.id.txtStatus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 5)
        (activity as? MainActivity)?.apply {
            setBackVisible(true)
            setContinueVisible(true)
            setContinueEnabled(false)  // 🔒 Disable Continue on start
        }
        if (mrzInfo == null) {
            Toast.makeText(requireContext(), "Không có dữ liệu MRZ", Toast.LENGTH_SHORT).show()
            return
        }
        handleNfcTag()
    }

    private fun handleNfcTag() {
        val mrz = mrzInfo ?: return
        val disposableNfc = EidFacade.handleDocumentNfcTag(
            requireContext(),
            mrz,
            object : EidCallback {
                override fun onEidReadStart() {
                    uiHandler.post {
                        progressBar?.visibility = View.VISIBLE
                        txtStatus?.text = "🔄 Đang đọc chip..."

                        (activity as? MainActivity)?.apply {
                            setBackVisible(true)
                            setContinueVisible(true)
                            setContinueEnabled(false)  // 🔒 Disable Continue during NFC read
                        }
                    }
                }


                override fun onEidReadFinish() {
                    uiHandler.post {
                        progressBar?.visibility = View.GONE
                        txtStatus?.append("\nĐọc xong.")
                    }
                }

                override fun onEidRead(eid: Eid?) {
                    uiHandler.post {
                        if (eid == null) {
                            txtStatus?.text = "❌ Không đọc được dữ liệu từ chip"
                            Toast.makeText(context, "Không có dữ liệu", Toast.LENGTH_SHORT).show()
                        } else {
                            txtStatus?.text = "✅ Đọc dữ liệu chip thành công"

                            // Save into ViewModel
                            docViewModel.eid = eid
                            eid.face?.let { bitmap ->
                                docViewModel.chipPortrait = bitmap
                                Log.d("NFC", "✅ chipPortrait set from eid.face")
                            } ?: Log.w("NFC", "❌ eid.face was null")

                            // Navigate to details after a short delay
                            uiHandler.postDelayed({
                                showEidDetails()
                            }, 150)
                        }
                        (activity as? MainActivity)?.setContinueEnabled(true)  // ✅ Re-enable Continue

                    }
                }

                override fun onAccessDeniedException(ex: AccessDeniedException) =
                    handleCardException(ex)

                override fun onBACDeniedException(ex: BACDeniedException) =
                    handleCardException(ex)

                override fun onPACEException(ex: PACEException) =
                    handleCardException(ex)

                override fun onCardException(ex: CardServiceException) {
                    val message = when (ex.sw.toShort()) {
                        ISO7816.SW_CLA_NOT_SUPPORTED -> getString(R.string.warning_cla_not_supported)
                        else -> ex.localizedMessage ?: "Unknown card exception"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    handleCardException(ex)
                }

                override fun onGeneralException(exception: Exception?) =
                    handleCardException(exception)
            }
        )
        disposable.add(disposableNfc)
    }

    @SuppressLint("SetTextI18n")
    private fun handleCardException(exception: Exception?) {
        Log.e(TAG, "CardException: ${exception?.message}", exception)
        uiHandler.post {
            progressBar?.visibility = View.GONE
            txtStatus?.text = "❌ Có lỗi khi đọc chip:\n${exception?.message}"
        }
    }

    private fun showEidDetails() {
        EidDetailsFragment()
        (activity as? MainActivity)?.replaceFragment(EidDetailsFragment())

    }

    override fun onDestroyView() {
        progressBar = null
        txtStatus = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (!disposable.isDisposed) disposable.dispose()
        super.onDestroy()
    }

    companion object {
        private val TAG = NfcFragment::class.java.simpleName
        private const val ARG_MRZ_INFO = "mrz_info"

        init {
            Security.insertProviderAt(BouncyCastleProvider(), 1)
        }

        fun newInstance(mrzInfo: MRZInfo) = NfcFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_MRZ_INFO, mrzInfo)
            }
        }
    }
}