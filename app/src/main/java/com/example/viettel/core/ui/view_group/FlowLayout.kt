package com.example.viettel.core.ui.view_group

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import kotlin.math.max

open class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val allViews = mutableListOf<List<View>>()
    private val lineHeights = mutableListOf<Int>()
    private val lineWidths = mutableListOf<Int>()

    // Gravity
    private var horizontalGravity: Int = Gravity.START            // START | CENTER_HORIZONTAL | END
    private var itemVerticalGravity: Int = Gravity.TOP            // TOP | CENTER_VERTICAL | BOTTOM
    private var verticalGravity: Int = Gravity.TOP                // TOP | CENTER_VERTICAL | BOTTOM

    fun setHorizontalGravity(gravity: Int) {
        horizontalGravity = gravity
        requestLayout()
    }
    fun setItemVerticalGravity(gravity: Int) {
        itemVerticalGravity = gravity
        requestLayout()
    }
    fun setVerticalGravity(gravity: Int) {
        verticalGravity = gravity
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        val maxLineWidth = if (modeWidth == MeasureSpec.UNSPECIFIED)
            Int.MAX_VALUE else sizeWidth - paddingLeft - paddingRight

        var widthUsed = 0
        var heightUsed = 0
        var lineWidth = 0
        var lineHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue
            val lp = child.layoutParams as MarginLayoutParams
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

            val cw = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val ch = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (lineWidth + cw > maxLineWidth) {
                widthUsed = max(widthUsed, lineWidth)
                heightUsed += lineHeight
                lineWidth = cw
                lineHeight = ch
            } else {
                lineWidth += cw
                lineHeight = max(lineHeight, ch)
            }
        }

        widthUsed = max(widthUsed, lineWidth)
        heightUsed += lineHeight

        val measuredW = if (modeWidth == MeasureSpec.EXACTLY) sizeWidth
        else widthUsed + paddingLeft + paddingRight

        val measuredH = if (modeHeight == MeasureSpec.EXACTLY) sizeHeight
        else heightUsed + paddingTop + paddingBottom

        setMeasuredDimension(
            resolveSize(measuredW, widthMeasureSpec),
            resolveSize(measuredH, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        allViews.clear()
        lineHeights.clear()
        lineWidths.clear()

        val contentWidth = (r - l) - paddingLeft - paddingRight

        var lineWidth = 0
        var lineHeight = 0
        var lineViews = mutableListOf<View>()

        // Gom dòng
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue
            val lp = child.layoutParams as MarginLayoutParams
            val cw = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val ch = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (lineWidth + cw > contentWidth) {
                allViews.add(lineViews)
                lineHeights.add(lineHeight)
                lineWidths.add(lineWidth)
                lineViews = mutableListOf()
                lineWidth = cw
                lineHeight = ch
            } else {
                lineWidth += cw
                lineHeight = max(lineHeight, ch)
            }
            lineViews.add(child)
        }
        if (lineViews.isNotEmpty()) {
            allViews.add(lineViews)
            lineHeights.add(lineHeight)
            lineWidths.add(lineWidth)
        }

        // Tính offset theo verticalGravity cho toàn khối
        val contentHeightAllLines = lineHeights.sum()
        val containerHeight = (b - t) - paddingTop - paddingBottom
        var top = paddingTop + when (verticalGravity) {
            Gravity.CENTER_VERTICAL -> max(0, (containerHeight - contentHeightAllLines) / 2)
            Gravity.BOTTOM -> max(0, containerHeight - contentHeightAllLines)
            else -> 0
        }

        // Đặt vị trí
        for (i in allViews.indices) {
            // offset trái/phải theo horizontalGravity cho từng dòng
            val extra = contentWidth - lineWidths[i]
            var left = paddingLeft + when (horizontalGravity) {
                Gravity.CENTER_HORIZONTAL -> max(0, extra / 2)
                Gravity.END -> max(0, extra)
                else -> 0
            }

            val lh = lineHeights[i]
            for (child in allViews[i]) {
                val lp = child.layoutParams as MarginLayoutParams

                // offset theo itemVerticalGravity trong phạm vi dòng
                val occupied = child.measuredHeight + lp.topMargin + lp.bottomMargin
                val yOff = when (itemVerticalGravity) {
                    Gravity.CENTER_VERTICAL -> max(0, (lh - occupied) / 2)
                    Gravity.BOTTOM -> max(0, lh - occupied)
                    else -> 0
                }

                val lc = left + lp.leftMargin
                val tc = top + lp.topMargin + yOff
                val rc = lc + child.measuredWidth
                val bc = tc + child.measuredHeight
                child.layout(lc, tc, rc, bc)

                left += child.measuredWidth + lp.leftMargin + lp.rightMargin
            }
            top += lh
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams =
        MarginLayoutParams(context, attrs)
    override fun generateLayoutParams(p: LayoutParams): LayoutParams =
        if (p is MarginLayoutParams) MarginLayoutParams(p) else MarginLayoutParams(p)
    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    override fun checkLayoutParams(p: LayoutParams): Boolean = p is MarginLayoutParams
}
