package com.example.viettel.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.viettel.R
import com.github.barteksc.pdfviewer.PDFView
import java.io.IOException

class ZoomPdfDialogFragment(private val startPage: Int) : DialogFragment() {

    private lateinit var pdfView: PDFView
    private lateinit var btnClose: ImageButton
    private lateinit var btnConfirm: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.zoom_pdf_popup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo view
        pdfView = view.findViewById(R.id.popup_pdf_view)
        btnClose = view.findViewById(R.id.btn_close_popup)
        btnConfirm = view.findViewById(R.id.btn_confirm)

        loadPdf()

        // Đóng popup
        btnClose.setOnClickListener { dismiss() }
        btnConfirm.setOnClickListener { dismiss() }
    }

    private fun loadPdf() {
        try {
            val context = requireContext()
            // Sửa tên file PDF cho đồng nhất với PdfSignFragment
            val inputStream = context.assets.open("Testing.pdf")
            pdfView.fromStream(inputStream)
                .defaultPage(startPage)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .enableAntialiasing(true)
                .load()
        } catch (e: IOException) {
            Toast.makeText(context, "Không thể mở PDF trong popup", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi tải PDF", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        val metrics = resources.displayMetrics

        val width = (metrics.widthPixels * 0.6).toInt()
        val height = (metrics.heightPixels * 0.8).toInt()

        window.setLayout(width, height)
        window.setBackgroundDrawableResource(android.R.color.white) // nếu muốn bo góc có thể chỉnh thêm drawable
    }

}
