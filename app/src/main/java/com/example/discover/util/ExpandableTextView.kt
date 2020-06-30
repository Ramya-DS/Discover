package com.example.discover.util

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.animation.ScaleAnimation
import android.widget.TextView
import com.example.discover.R


class ExpandableTextView(context: Context, attrs: AttributeSet?) :
    TextView(context, attrs) {

    private var originalText: CharSequence? = null
    private var trimmedText: CharSequence? = null
    private var bufferType: BufferType? = null
    private var trim = true
    private var trimLength: Int
//    private val spannableStringBuilder = SpannableStringBuilder()

//    constructor(context: Context) : this(context, null) {}

    private fun setText() {
        super.setText(displayableText, bufferType)
    }

    private val displayableText: CharSequence?
        get() = if (trim) trimmedText else originalText

    override fun setText(text: CharSequence, type: BufferType) {
        originalText = text
        trimmedText = getTrimmedText(text)
        bufferType = type
        setText()
    }

    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        return if (originalText != null && originalText!!.length > trimLength) {
            SpannableStringBuilder(
                originalText,
                0,
                trimLength + 1
            ).append(
                ELLIPSIS,
                ForegroundColorSpan(Color.BLUE),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            originalText
        }
    }

    fun setTrimLength(trimLength: Int) {
        this.trimLength = trimLength
        trimmedText = getTrimmedText(originalText)
        setText()
    }

    fun getTrimLength(): Int {
        return trimLength
    }

    companion object {
        private const val DEFAULT_TRIM_LENGTH = 200
        private const val ELLIPSIS = "...more"
        private const val COLLAPSED_ELLIPSIS = "...more"
    }

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        trimLength = typedArray.getInt(
            R.styleable.ExpandableTextView_trimLength,
            DEFAULT_TRIM_LENGTH
        )
        typedArray.recycle()
        setOnClickListener {
            trim = !trim
            setText()
            Log.d("set","text")
            if (!trim) ScaleAnimation(1.0f, 1f, 0f, 1f).start()
            Log.d("set","text")
//            val animation = ObjectAnimator.ofInt(
//                this, "maxLines",
//                if (maxLines == 4) lineCount else 4
//            )
//            animation.setDuration((lineCount - 4) * 10L).start()
            requestFocusFromTouch()
        }
    }

//    private var expandInterpolator: TimeInterpolator? = null
//    private var collapseInterpolator: TimeInterpolator? = null
//
//    private var animationDuration: Long = 0
//    private var animating = false
//    private var expanded = false
//    private var collapsedHeight = 0
//
//    init {
//        val attributes =
//            context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
//        animationDuration =
//            attributes.getInt(R.styleable.ExpandableTextView_animation_duration, 450).toLong()
//
//        trimLength =
//            attributes.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH)
//        attributes.recycle()
//
//        expandInterpolator = AccelerateDecelerateInterpolator()
//        collapseInterpolator = AccelerateDecelerateInterpolator()
//
//        setOnClickListener {
//            //            trim = !trim
//            Log.d("expanded", "Start $expanded")
//            expanded = toggle()
//            Log.d("expanded", "Finish $expanded")
//            requestFocusFromTouch()
//        }
//    }
//
//    override fun onMeasure(
//        widthMeasureSpec: Int,
//        heightMeasureSpec: Int
//    ) {
//        var mHeightMeasureSpec = heightMeasureSpec
//        if (maxLines == 0 && !expanded && !animating) {
//            mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
//        }
//
////        val availableScreenWidth =
////            measuredWidth - compoundPaddingLeft.toFloat() - compoundPaddingRight.toFloat()
////        var availableTextWidth = availableScreenWidth * maxLines
////        var ellipsizedText = TextUtils.ellipsize(text, paint, availableTextWidth, ellipsize)
////
////        if (ellipsizedText.toString() != text.toString()) {
////
////            availableTextWidth =
////                (availableScreenWidth - paint.measureText(if (expanded) EXPANDED_ELLIPSIS else COLLAPSED_ELLIPSIS)) * maxLines
////            ellipsizedText = TextUtils.ellipsize(text, paint, availableTextWidth, ellipsize)
////            val defaultEllipsisStart = ellipsizedText.indexOf(getDefaultEllipsis())
////            val defaultEllipsisEnd = defaultEllipsisStart + 1
////
////            if (defaultEllipsisStart != -1) {
////                spannableStringBuilder.clear()
////                text = spannableStringBuilder.append(ellipsizedText)
////                    .replace(
////                        defaultEllipsisStart,
////                        defaultEllipsisEnd,
////                        SpannableString(if (expanded) EXPANDED_ELLIPSIS else COLLAPSED_ELLIPSIS)
////                    )
////            }
////        }
//
//        super.onMeasure(widthMeasureSpec, mHeightMeasureSpec)
//    }
//
//    private fun setLabelAfterEllipsis() {
//        if (layout.getEllipsisCount(maxLines - 1) == 0) {
//            return  // Nothing to do
//        }
//        val start = layout.getLineStart(0)
//        val end = layout.getLineEnd(lineCount - 1)
//        val displayed = text.toString().substring(start, end)
//        val displayedWidth = getTextWidth(displayed, textSize)
//        val strLabel = "more"
//        val ellipsis = "..."
//        val suffix = ellipsis + strLabel
//        var textWidth: Int
//        var newText = displayed
//        textWidth = getTextWidth(newText + suffix, textSize)
//        while (textWidth > displayedWidth) {
//            newText = newText.substring(0, newText.length - 1).trim { it <= ' ' }
//            textWidth = getTextWidth(newText + suffix, textSize)
//        }
//        text = newText + suffix
//    }
//
//    private fun getTextWidth(text: String, textSize: Float): Int {
//        val bounds = Rect()
//        val paint = Paint()
//        paint.textSize = textSize
//        paint.getTextBounds(text, 0, text.length, bounds)
//        return ceil(bounds.width().toDouble()).toInt()
//    }
//
//    private fun toggle(): Boolean {
//        return if (expanded) collapse() else expand()
//    }
//
//    private fun expand(): Boolean {
//        if (!expanded && !animating && maxLines >= 0) {
//            measure(
//                MeasureSpec.makeMeasureSpec(this.measuredWidth, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
//            )
//            collapsedHeight = this.measuredHeight
//            animating = true
//            maxLines = Int.MAX_VALUE
//            measure(
//                MeasureSpec.makeMeasureSpec(this.measuredWidth, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
//            )
//            val expandedHeight = this.measuredHeight
//            val valueAnimator =
//                ValueAnimator.ofInt(collapsedHeight, expandedHeight)
//            valueAnimator.addUpdateListener { animation ->
//                height = animation.animatedValue as Int
//            }
//            valueAnimator.addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    maxHeight = Int.MAX_VALUE
//                    minHeight = 0
//                    val layoutParams = layoutParams
//                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
//                    setLayoutParams(layoutParams)
//
//                    expanded = true
//                    animating = false
//                }
//            })
//            valueAnimator.interpolator = expandInterpolator
//            valueAnimator
//                .setDuration(animationDuration)
//                .start()
//            return true
//        }
//        return false
//    }
//
//    private fun collapse(): Boolean {
//        if (expanded && !animating && maxLines >= 0) {
//            val expandedHeight = this.measuredHeight
//            animating = true
//            val valueAnimator = ValueAnimator.ofInt(expandedHeight, collapsedHeight)
//            valueAnimator.addUpdateListener { animation ->
//                height = animation.animatedValue as Int
//            }
//            valueAnimator.addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    expanded = false
//                    animating = false
//                    val layoutParams = layoutParams
//                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
//                    setLayoutParams(layoutParams)
//                }
//            })
//            valueAnimator.interpolator = collapseInterpolator
//            valueAnimator
//                .setDuration(animationDuration)
//                .start()
//            return true
//        }
//        return false
//    }
//
//    fun setAnimationDuration(animationDuration: Long) {
//        this.animationDuration = animationDuration
//    }
//
//    fun setInterpolator(interpolator: TimeInterpolator?) {
//        expandInterpolator = interpolator
//        collapseInterpolator = interpolator
//    }
//
//    fun setExpandInterpolator(expandInterpolator: TimeInterpolator?) {
//        this.expandInterpolator = expandInterpolator
//    }
//
//    fun getExpandInterpolator(): TimeInterpolator? {
//        return expandInterpolator
//    }
//
//    fun setCollapseInterpolator(collapseInterpolator: TimeInterpolator?) {
//        this.collapseInterpolator = collapseInterpolator
//    }
//
//    fun getCollapseInterpolator(): TimeInterpolator? {
//        return collapseInterpolator
//    }
//
//    fun isExpanded(): Boolean {
//        return expanded
//    }
}