package com.example.viettel.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.github.barteksc.pdfviewer.PDFView
import java.io.IOException
import android.widget.CheckBox
import kotlin.text.clear
import com.example.viettel.fragments.SignatureView
import com.example.viettel.utils.ProgressUtils

class PdfSignFragment : Fragment() {

    private lateinit var pdfView: PDFView
    private lateinit var signatureView: SignatureView
    private lateinit var tvPageNumber: TextView
    private lateinit var btnPrevPage: ImageButton
    private lateinit var btnNextPage: ImageButton
    private lateinit var btnZoomPdf: ImageButton
    private lateinit var checkboxAgree: CheckBox
    private lateinit var btnClearSignature: View

    private var totalPages = 0
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_pdf_sign, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 6)
        initViews(view)
        loadPdfFromAssets()
        setupListeners()
    }

    private fun initViews(view: View) {
        pdfView = view.findViewById(R.id.pdfView)
        signatureView = view.findViewById(R.id.signature_view)
        tvPageNumber = view.findViewById(R.id.tv_page_number)
        btnPrevPage = view.findViewById(R.id.btn_back_page)
        btnNextPage = view.findViewById(R.id.btn_next_page)
        btnZoomPdf = view.findViewById(R.id.btn_zoom_pdf)
        checkboxAgree = view.findViewById(R.id.checkbox_agree_policy)
        btnClearSignature = view.findViewById(R.id.btn_clear_signature)
    }

    private fun loadPdfFromAssets() {
        try {
            val inputStream = requireContext().assets.open("Testing.pdf")
            pdfView.fromStream(inputStream)
                .defaultPage(currentPage)
                .enableSwipe(false)
                .swipeHorizontal(false)
                .enableDoubletap(false)
                .enableAnnotationRendering(true)
                .enableAntialiasing(true)
                .onPageChange { page, pageCount ->
                    currentPage = page
                    totalPages = pageCount
                    updatePageNumber()
                }
                .load()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Không thể mở PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        btnZoomPdf.setOnClickListener {
            val zoomDialog = ZoomPdfDialogFragment(currentPage)
            zoomDialog.show(childFragmentManager, "ZoomPdfDialog")
        }

        btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                pdfView.jumpTo(currentPage, true)
                updatePageNumber()
            }
        }

        btnNextPage.setOnClickListener {
            if (currentPage < totalPages - 1) {
                currentPage++
                pdfView.jumpTo(currentPage, true)
                updatePageNumber()
            }
        }

        btnClearSignature.setOnClickListener {
            signatureView.clear()
        }
    }

    private fun updatePageNumber() {
        tvPageNumber.text = "${currentPage + 1} / $totalPages"
    }

    fun isSigned(): Boolean {
        return this::signatureView.isInitialized && !signatureView.isEmpty()
    }

    fun isPolicyChecked(): Boolean {
        return this::checkboxAgree.isInitialized && checkboxAgree.isChecked
    }

    fun getSignatureBitmap() = signatureView.getSignatureBitmap()
}
