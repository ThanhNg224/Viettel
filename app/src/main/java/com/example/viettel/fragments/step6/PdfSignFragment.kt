package com.example.viettel.fragments.step6

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.databinding.FragmentPdfSignBinding
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.updateNavigationControls
import java.io.ByteArrayOutputStream
import java.io.IOException

class PdfSignFragment : Fragment() {

    private var _binding: FragmentPdfSignBinding? = null
    private val binding get() = _binding!!

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }

    private var totalPages = 0
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfSignBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProgressUtils.animateProgressToStep(view, 6)
        loadPdfFromAssets()
        setupListeners()

        updateNavigationControls(isBackVisible = true, isContinueVisible = false, isContinueEnabled = false)
    }

    private fun loadPdfFromAssets() {
        try {
            val inputStream = requireContext().assets.open("Testing.pdf")
            binding.pdfView.fromStream(inputStream)
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
        } catch (_: IOException) {
            Toast.makeText(requireContext(), "Khong the mo PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnZoomPdf.setOnClickListener {
            val zoomDialog = ZoomPdfDialogFragment(currentPage)
            zoomDialog.show(childFragmentManager, "ZoomPdfDialog")
        }

        binding.btnBackPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                binding.pdfView.jumpTo(currentPage, true)
                updatePageNumber()
            }
        }

        binding.btnNextPage.setOnClickListener {
            if (currentPage < totalPages - 1) {
                currentPage++
                binding.pdfView.jumpTo(currentPage, true)
                updatePageNumber()
            }
        }

        binding.btnClearSignature.setOnClickListener {
            binding.signatureView.clear()
            checkSignatureAndPolicy()
        }

        binding.checkboxAgreePolicy.setOnCheckedChangeListener { _, _ ->
            checkSignatureAndPolicy()
        }

        binding.signatureView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.performClick()
                checkSignatureAndPolicy()
            }
            false
        }

        updateNavigationControls(isBackVisible = true, isContinueVisible = false, isContinueEnabled = false)
    }

    private fun updatePageNumber() {
        @Suppress("SetTextI18n")
        binding.tvPageNumber.text = "${currentPage + 1} / $totalPages"
    }

    fun isSigned(): Boolean {
        return _binding != null && !binding.signatureView.isEmpty()
    }

    fun isPolicyChecked(): Boolean {
        return _binding != null && binding.checkboxAgreePolicy.isChecked
    }

    private fun checkSignatureAndPolicy() {
        val isSigned = isSigned()
        val isChecked = isPolicyChecked()

        if (isSigned && isChecked) {
            val signatureBytes = ByteArrayOutputStream().use { out ->
                binding.signatureView.getSignatureBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }
            identityViewModel.saveSignature(signatureBytes)
        }

        updateNavigationControls(
            isBackVisible = true,
            isContinueVisible = isSigned && isChecked,
            isContinueEnabled = isSigned && isChecked
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    fun isEmpty(): Boolean = !isTouched

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
