package com.example.viettel.core.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import com.example.viettel.databinding.DialogFailBinding

fun Context.showDialogFail(
    title: String? = null,
    content: String? = null
) {
    val dialog = FailDialog(this, title, content)
    dialog.show()
}

private class FailDialog(
    context: Context,
    title: String? = null,
    content: String? = null,
) : BaseAnimatedDialog<DialogFailBinding>(context) {

    override fun createBinding(inflater: LayoutInflater) =
        DialogFailBinding.inflate(inflater)

    init {
        binding.textTitle.text = title
        binding.textContent.text = content
        binding.buttonOk.setOnClickListener { dismiss() }
    }
}
