package com.example.viettel.core.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.example.viettel.core.ui.buttons.FrameButton
import com.example.viettel.core.extensions.findActivity
import com.example.viettel.core.extensions.isTablet
import kotlin.math.max
import kotlin.math.min

enum class DialogAnimation {
    BottomTop,
    TopTop,
    LeftRight,
    RightLeft,
    Zoom
}

enum class DialogType {
    SideBar,
    Dialog
}

abstract class BaseAnimatedDialog<T : ViewBinding>(
    context: Context,
    private val animation: DialogAnimation = DialogAnimation.BottomTop,
    private val isCancelable: Boolean = true,
    canShowKeyboard: Boolean = false,
    private val bottomInset: Int = 0,
    dialogType: DialogType = DialogType.Dialog,
) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {

    protected val binding: T by lazy { createBinding(LayoutInflater.from(context)) }

    private var isAnimating = false

    abstract fun createBinding(inflater: LayoutInflater): T

    // Tạo container full màn hình để có nền mờ và chỗ chạy anim
    private var container: FrameLayout = FrameButton(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor("#80000000".toColorInt()) // nền mờ

        // click nền -> ẩn dialog
        onClickListener {
            if (!isAnimating && isCancelable) dismiss()
        }
    }

    init {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val contentParams: FrameLayout.LayoutParams

        when (dialogType) {
            DialogType.SideBar -> {
                // SideBar chiếm 70%
                val sidebarWidth = (screenWidth * 0.7f).toInt()
                contentParams = FrameLayout.LayoutParams(
                    sidebarWidth,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.END // bạn có thể đổi thành Gravity.START
                )
            }

            DialogType.Dialog -> {
                val widthOnPad = if (context.isTablet()) min(screenWidth, screenHeight / 2) else screenWidth
                val dialogWidth = (widthOnPad * 0.9f).toInt()
                contentParams = FrameLayout.LayoutParams(
                    dialogWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER // bạn có thể đổi thành Gravity.BOTTOM, TOP...
                )
            }
        }

        // ép content chỉ wrap_content
        binding.root.isClickable = true
        container.addView(binding.root, contentParams)
        setContentView(container)

        window?.apply {
            // Transparent background
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )

            // Fullscreen
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )

            // Status bar & navigation bar transparent
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }

        if (canShowKeyboard) {
            observeKeyboardVisibility()
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        window?.attributes?.apply {
            flags = if (on) {
                flags or bits
            } else {
                flags and bits.inv()
            }
        }
    }

    override fun show() {
        val activity = context.findActivity() ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        try {
            super.show()
            runEnterAnimation(binding.root)
        } catch (e: WindowManager.BadTokenException) {
            // phòng trường hợp context vẫn invalid
            e.printStackTrace()
        }
    }

    private var onDialogDismiss: (() -> Unit)? = null

    fun setOnDialogDismiss(listener: () -> Unit) {
        onDialogDismiss = listener
    }

    override fun dismiss() {
        if (!isShowing) return
        runExitAnimation(binding.root) {
            if (isShowing) {
                try {
                    super.dismiss()
                    onDialogDismiss?.invoke()
                } catch (_: Exception) {
                    // Window đã detach → ignore
                }
            }
        }
    }

    private fun getScreenHeight() = context.resources.displayMetrics.heightPixels
    private fun getScreenWidth() = context.resources.displayMetrics.widthPixels

    private fun runEnterAnimation(view: View) {
        val h = getScreenHeight().toFloat()
        val w = getScreenWidth().toFloat()
        isAnimating = true

        when (animation) {
            DialogAnimation.BottomTop -> {
                view.translationY = h
                view.animate().translationY(0f).setDuration(300)
                    .withEndAction { isAnimating = false }
                    .start()
            }

            DialogAnimation.TopTop -> {
                view.translationY = -h
                view.animate().translationY(0f).setDuration(300)
                    .withEndAction { isAnimating = false }
                    .start()
            }

            DialogAnimation.LeftRight -> {
                view.translationX = -w
                view.animate().translationX(0f).setDuration(300)
                    .withEndAction { isAnimating = false }
                    .start()
            }

            DialogAnimation.RightLeft -> {
                view.translationX = w
                view.animate().translationX(0f).setDuration(300)
                    .withEndAction { isAnimating = false }
                    .start()
            }

            DialogAnimation.Zoom -> {
                view.scaleX = 0.7f
                view.scaleY = 0.7f
                view.alpha = 0f
                view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(300)
                    .withEndAction { isAnimating = false }
                    .start()
            }
        }
    }

    private fun runExitAnimation(view: View, end: () -> Unit) {
        val h = getScreenHeight().toFloat()
        val w = getScreenWidth().toFloat()
        isAnimating = true

        when (animation) {
            DialogAnimation.BottomTop -> {
                view.animate().translationY(-h).setDuration(300)
                    .withEndAction {
                        isAnimating = false
                        end()
                    }.start()
            }

            DialogAnimation.TopTop -> {
                view.animate().translationY(-h).setDuration(300)
                    .withEndAction {
                        isAnimating = false
                        end()
                    }.start()
            }

            DialogAnimation.LeftRight -> {
                view.animate().translationX(-w).setDuration(300)
                    .withEndAction {
                        isAnimating = false
                        end()
                    }.start()
            }

            DialogAnimation.RightLeft -> {
                view.animate().translationX(w).setDuration(300)
                    .withEndAction {
                        isAnimating = false
                        end()
                    }.start()
            }

            DialogAnimation.Zoom -> {
                view.animate().scaleX(0.7f).scaleY(0.7f).alpha(0f).setDuration(300)
                    .withEndAction {
                        isAnimating = false
                        end()
                    }.start()
            }
        }
    }

    private var lastKeyboardHeight = -1
    private fun observeKeyboardVisibility() {
        // Cập nhật view khi bàn phím xuất hiện
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val keyboardHeight = max(imeHeight - bottomInset, 0)

                if (keyboardHeight != lastKeyboardHeight) {
                    lastKeyboardHeight = keyboardHeight

                    updateKeyboardMargin(view, keyboardHeight)
                }

                insets
            }
        } else {
            // Android cũ xử lý bàn phím
            container.viewTreeObserver.addOnGlobalLayoutListener {
                val rect = Rect()
                container.getWindowVisibleDisplayFrame(rect)
                val screenHeight = container.rootView.height
                val keyboardHeight = max(screenHeight - rect.bottom - bottomInset, 0)

                if (keyboardHeight != lastKeyboardHeight) {
                    lastKeyboardHeight = keyboardHeight
                    updateKeyboardMargin(container, if (keyboardHeight > 100) keyboardHeight else 0)
                }
            }
        }
    }

    private fun updateKeyboardMargin(view: View, bottomMargin: Int) {
        view.setPadding(0, 0, 0, bottomMargin)
    }

    override fun onStop() {
        super.onStop()
        binding.root.animate().cancel()
        isAnimating = false
        // nếu đang hiển thị mà Activity đóng → đảm bảo dismiss ngay
        if (isShowing) {
            try {
                super.dismiss()
            } catch (_: Exception) {
            }
        }
    }

}
