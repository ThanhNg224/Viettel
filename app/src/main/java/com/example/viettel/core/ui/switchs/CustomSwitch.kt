package com.example.viettel.core.ui.switchs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.example.viettel.R
import com.example.viettel.core.extensions.dpToPx
import com.example.viettel.databinding.SwitchCustomBinding

class CustomSwitch : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }


    private var isChecked = false
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    private val binding: SwitchCustomBinding =
        SwitchCustomBinding.inflate(LayoutInflater.from(context), this, true)

    private var trackWidth = 0
    private var trackHeight = 0
    private var padding = 0
    private var thumbSize = 0

    private fun initView(attrs: AttributeSet?) {
        binding.root.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSwitch)
        try {
            isChecked = typedArray.getBoolean(R.styleable.CustomSwitch_checked, false)

            val sizeStyle = typedArray.getInt(R.styleable.CustomSwitch_switch_size, 0)
            trackWidth = (if (sizeStyle == 0) 40 else 32).dpToPx()
            trackHeight = (if (sizeStyle == 0) 20 else 16).dpToPx()
            padding = 2.dpToPx()
            thumbSize = trackHeight - padding * 2

            // update layout params
            binding.viewTrack.layoutParams = LayoutParams(trackWidth, trackHeight)
            binding.viewTrack.setPadding(padding, padding, padding, padding)
            binding.thumb.layoutParams = RelativeLayout.LayoutParams(thumbSize, thumbSize)



            // Selected default color
            val thumbSelectedDefaultColor = ContextCompat.getColor(context, R.color.white)
            val trackSelectedDefaultColor = ContextCompat.getColor(context, R.color.green)

            // Unselected default color
            val thumbUnselectedDefaultColor = ContextCompat.getColor(context, R.color.grey)
            val trackUnselectedDefaultColor = ContextCompat.getColor(context, R.color.grey)

            // Selected disable color
            val thumbSelectedDisableColor = ContextCompat.getColor(context, R.color.white).let { color ->
                (color and 0x00FFFFFF) or (0x80 shl 24) // 0x80 = 50% opacity
            }
            val trackSelectedDisableColor = ContextCompat.getColor(context, R.color.green).let { color ->
                (color and 0x00FFFFFF) or (0x80 shl 24) // 0x80 = 50% opacity
            }

            // Unselected disable color
            val thumbUnselectedDisableColor = ContextCompat.getColor(context, R.color.grey).let { color ->
                (color and 0x00FFFFFF) or (0x80 shl 24) // 0x80 = 50% opacity
            }
            val trackUnselectedDisableColor = ContextCompat.getColor(context, R.color.grey).let { color ->
                (color and 0x00FFFFFF) or (0x80 shl 24) // 0x80 = 50% opacity
            }


            // Selected hover color
            val thumbSelectedHoverColor = ContextCompat.getColor(context, R.color.white)
            val trackSelectedHoverColor = ContextCompat.getColor(context, R.color.green_light)

            // Unselected hover color
            val thumbUnselectedHoverColor = ContextCompat.getColor(context, R.color.grey)
            val trackUnselectedHoverColor = ContextCompat.getColor(context, R.color.grey)

            // track color setTintList
            (binding.viewTrack.background as? StateListDrawable)?.setTintList(
                ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_selected), // Disabled + Selected
                        intArrayOf(-android.R.attr.state_enabled), // Disabled + Unselected
                        intArrayOf(android.R.attr.state_pressed, android.R.attr.state_selected), // hover + Selected
                        intArrayOf(android.R.attr.state_pressed), // hover + Unselected
                        intArrayOf(android.R.attr.state_selected), // Selected
                        intArrayOf() // Unselected
                    ),
                    intArrayOf(
                        trackSelectedDisableColor,
                        trackUnselectedDisableColor,
                        trackSelectedHoverColor,
                        trackUnselectedHoverColor,
                        trackSelectedDefaultColor,
                        trackUnselectedDefaultColor
                    )
                )
            )

            // thumb color setTintList
            (binding.thumb.background as? StateListDrawable)?.setTintList(
                ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_selected), // Disabled + Selected
                        intArrayOf(-android.R.attr.state_enabled), // Disabled + Unselected
                        intArrayOf(android.R.attr.state_pressed, android.R.attr.state_selected), // hover + Selected
                        intArrayOf(android.R.attr.state_pressed), // hover + Unselected
                        intArrayOf(android.R.attr.state_selected), // Selected
                        intArrayOf() // Unselected
                    ),
                    intArrayOf(
                        thumbSelectedDisableColor,
                        thumbUnselectedDisableColor,
                        thumbSelectedHoverColor,
                        thumbUnselectedHoverColor,
                        thumbSelectedDefaultColor,
                        thumbUnselectedDefaultColor
                    )
                )
            )


        } finally {
            // Đảm bảo giải phóng tài nguyên
            typedArray.recycle()
        }

        updateSwitchState()
        binding.switchTrack.onClickListener { toggle() }
    }

    private fun toggle() {
        isChecked = !isChecked
        updateSwitchState()
        onCheckedChangeListener?.onCheckedChanged(this, isChecked)
    }

    private fun updateSwitchState() {
        if (isChecked) {
            binding.thumb.animate()
                .translationX((trackWidth - thumbSize - padding * 2).toFloat())
                .setDuration(200)
            isSelected = true
        } else {
            binding.thumb.animate().translationX(0f).setDuration(200)
            isSelected = false
        }
    }

    fun isChecked(): Boolean = isChecked

    fun setChecked(checked: Boolean) {
        isChecked = checked
        updateSwitchState()
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(customSwitch: CustomSwitch, isChecked: Boolean)
    }

}
