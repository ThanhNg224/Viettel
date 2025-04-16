package com.example.viettel.fragments.step6

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.fragments.step6.SignatureView
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.viewmodel.DocumentViewModel
import com.github.barteksc.pdfviewer.PDFView
import java.io.IOException

class PdfSignFragment : Fragment() {

    private lateinit var pdfView: PDFView
    private lateinit var signatureView: SignatureView
    private lateinit var tvPageNumber: TextView
    private lateinit var btnPrevPage: ImageButton
    private lateinit var btnNextPage: ImageButton
    private lateinit var btnZoomPdf: ImageButton
    private lateinit var checkboxAgree: CheckBox
    private lateinit var btnClearSignature: View
    private val docViewModel: DocumentViewModel by activityViewModels()


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

        (activity as? MainActivity)?.setContinueVisible(false)

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
                .enableSwipe(true)
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
            Log.e("TAG", "IOException: ${e.message}", e)
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
            checkSignatureAndPolicy() // reset lại điều kiện nếu đã ký rồi
        }

        // Lắng nghe tick checkbox
        checkboxAgree.setOnCheckedChangeListener { _, _ ->
            checkSignatureAndPolicy()
        }

        signatureView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.performClick() // ✅ Accessibility support
                checkSignatureAndPolicy()
            }
            false // allow drawing
        }

        // Ẩn nút tiếp tục khi mới vào
        (activity as? MainActivity)?.setContinueVisible(false)
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
    private fun checkSignatureAndPolicy() {
        val isSigned = isSigned()
        val isChecked = isPolicyChecked()

        if (isSigned && isChecked) {
            docViewModel.signatureBitmap = getSignatureBitmap()
        }

        (activity as? MainActivity)?.setContinueVisible(isSigned && isChecked)
    }


}

class SignatureView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val path = Path()
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var isTouched = false

    fun clear() {
        path.reset()
        isTouched = false
        invalidate()
    }

    fun isEmpty(): Boolean {
        return !isTouched
    }

    fun getSignatureBitmap(): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                isTouched = true
                performClick()
            }
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

}