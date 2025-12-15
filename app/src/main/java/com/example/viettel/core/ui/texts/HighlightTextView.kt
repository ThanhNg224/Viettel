package com.example.viettel.core.ui.texts

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.example.viettel.R
import com.example.viettel.listener.StringCallback

class HighlightTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    // Config
    private var highlightColor: Int = currentTextColor
    private var highlightKeywords: List<String> = emptyList()
    private var highlightUnderline: Boolean = false
    private var highlightClickable: Boolean = false
    private var keywordClickListener: StringCallback? = null

    // Base lấy từ TextAppearance (chỉ dùng cho span)
    private var taTypeface: Typeface? = null
    private var taTextSizePx: Float? = null
    private var taTextStyle: Int? = null
    private var taTextColor: Int? = null

    // Override trực tiếp (ghi đè TA)
    private var highlightTypeface: Typeface? = null
    private var highlightTextSizePx: Float? = null

    private var isApplying = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.HighlightTextView, defStyle, 0)

        // 1) Nếu có TextAppearance cho highlight -> parse android attrs (không dùng styleable của bạn)
        val taResId = a.getResourceId(R.styleable.HighlightTextView_highlightTextAppearance, 0)
        if (taResId != 0) parseTextAppearance(taResId)

        // 2) Attr trực tiếp trên view -> ghi đè
        try {
            applyAttrsFrom(a)
        } finally {
            a.recycle()
        }

        // 3) Áp dụng ban đầu
        post { applyHighlight() }
    }

    /** Parse đúng các android attrs trong một TextAppearance làm "base" cho highlight */
    @SuppressLint("ResourceType")
    private fun parseTextAppearance(@StyleRes resId: Int) {
        val attrs = intArrayOf(
            android.R.attr.textColor,     // 0
            android.R.attr.textSize,      // 1
            android.R.attr.fontFamily,    // 2
            android.R.attr.textStyle      // 3
        )
        val ta = context.obtainStyledAttributes(resId, attrs)
        try {
            if (ta.hasValue(0)) taTextColor = ta.getColor(0, currentTextColor)
            if (ta.hasValue(1)) taTextSizePx = ta.getDimension(1, textSize)

            if (ta.hasValue(2)) {
                val v = ta.peekValue(2)
                taTypeface = when (v.type) {
                    TypedValue.TYPE_STRING -> Typeface.create(ta.getString(2), Typeface.NORMAL)
                    TypedValue.TYPE_REFERENCE -> {
                        val fontResId = ta.getResourceId(2, 0)
                        if (fontResId != 0) ResourcesCompat.getFont(context, fontResId) else null
                    }
                    else -> null
                }
            }
            if (ta.hasValue(3)) taTextStyle = ta.getInt(3, Typeface.NORMAL)

            // Base color từ TextAppearance (có thể bị attr trực tiếp ghi đè)
            taTextColor?.let { highlightColor = it }

            // Áp textStyle lên typeface base nếu có
            if (taTextStyle != null) {
                taTypeface = taTypeface?.let { Typeface.create(it, taTextStyle!!) }
                    ?: Typeface.create(typeface, taTextStyle!!)
            }
        } finally {
            ta.recycle()
        }
    }

    /** Chỉ đọc các attr custom đặt trực tiếp trên view */
    private fun applyAttrsFrom(ta: TypedArray) {
        // color
        if (ta.hasValue(R.styleable.HighlightTextView_highlightColor)) {
            highlightColor = ta.getColor(R.styleable.HighlightTextView_highlightColor, highlightColor)
        }

        // keywords
        ta.getString(R.styleable.HighlightTextView_highlightKeywords)?.let { s ->
            highlightKeywords = s.split(",").mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() } }
        }

        // font (custom attr chỉ ghi đè cho highlight span)
        val fontResId = ta.getResourceId(R.styleable.HighlightTextView_highlightFontFamily, -1)
        if (fontResId != -1) {
            highlightTypeface = ResourcesCompat.getFont(context, fontResId)
        }

        // size (dimension -> px) ghi đè cho highlight span
        if (ta.hasValue(R.styleable.HighlightTextView_highlightTextSize)) {
            highlightTextSizePx = ta.getDimension(R.styleable.HighlightTextView_highlightTextSize, textSize)
        }

        // underline
        if (ta.hasValue(R.styleable.HighlightTextView_highlightUnderline)) {
            highlightUnderline = ta.getBoolean(R.styleable.HighlightTextView_highlightUnderline, false)
        }

        // clickable
        if (ta.hasValue(R.styleable.HighlightTextView_highlightClickable)) {
            highlightClickable = ta.getBoolean(R.styleable.HighlightTextView_highlightClickable, false)
        }
    }

    private fun effectiveTypeface(): Typeface? {
        // Ưu tiên: override trên view > base từ TextAppearance > null
        return highlightTypeface ?: taTypeface
    }

    private fun effectiveTextSizePx(): Float? {
        // Ưu tiên: override trên view > base từ TextAppearance > null
        return highlightTextSizePx ?: taTextSizePx
    }

    private fun applyHighlight() {
        if (isApplying) return
        val fullText = text ?: return
        val full = fullText.toString()
        if (full.isEmpty() || highlightKeywords.isEmpty()) {
            movementMethod = null
            linksClickable = false
            return
        }

        isApplying = true
        try {
            val spannable = SpannableString(full)
            val typefaceForSpan = effectiveTypeface()
            val textSizeForSpan = effectiveTextSizePx()

            // Tối ưu: gộp keyword thành 1 regex
            val pattern = Regex(
                highlightKeywords.filter { it.isNotEmpty() }
                    .joinToString("|") { Regex.escape(it) },
                RegexOption.IGNORE_CASE
            )

            pattern.findAll(full).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1

                // color
                spannable.setSpan(ForegroundColorSpan(highlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // font
                typefaceForSpan?.let {
                    spannable.setSpan(CustomTypefaceSpan(it), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                // size
                textSizeForSpan?.let {
                    spannable.setSpan(AbsoluteSizeSpan(it.toInt()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                // underline: nếu không clickable thì underline bằng span riêng
                if (highlightUnderline && !highlightClickable) {
                    spannable.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                // clickable: chỉ add khi được bật
                if (highlightClickable) {
                    spannable.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            // pass đúng từ khớp (match.value), không phải keyword input
                            keywordClickListener?.execute(match.value)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = highlightColor
                            ds.isUnderlineText = highlightUnderline
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            setText(spannable, BufferType.SPANNABLE)

            if (highlightClickable) {
                movementMethod = LinkMovementMethod.getInstance()
                linksClickable = true
            } else {
                movementMethod = null
                linksClickable = false
            }
        } finally {
            isApplying = false
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        if (!isApplying) post { applyHighlight() }
    }

    fun setOnKeywordClickListener(listener: StringCallback?) {
        keywordClickListener = listener
    }

    fun setHighlightClickable(enabled: Boolean) {
        highlightClickable = enabled
        applyHighlight()
    }

    fun setHighlightContent(
        text: String,
        keywords: List<String>,
        color: Int? = null,
        font: Typeface? = null,
        textSizeSp: Float? = null,
        underline: Boolean? = null
    ) {
        this.text = text
        this.highlightKeywords = keywords
        color?.let { this.highlightColor = it }
        font?.let { this.highlightTypeface = it }
        textSizeSp?.let { this.highlightTextSizePx = it * resources.displayMetrics.scaledDensity }
        underline?.let { this.highlightUnderline = it }
        applyHighlight()
    }

    class CustomTypefaceSpan(private val tf: Typeface) : MetricAffectingSpan() {
        override fun updateMeasureState(paint: TextPaint) = apply(paint)
        override fun updateDrawState(tp: TextPaint) = apply(tp)
        private fun apply(p: TextPaint) { p.typeface = tf }
    }
}
