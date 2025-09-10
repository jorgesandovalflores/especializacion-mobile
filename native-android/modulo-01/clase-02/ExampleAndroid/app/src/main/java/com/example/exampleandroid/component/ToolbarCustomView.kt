package com.example.exampleandroid.component

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.example.exampleandroid.R
import com.google.android.material.card.MaterialCardView
import kotlin.math.max

class ToolbarCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvTitle: TextView
    private val bar: LinearLayout

    private var colorStart: Int = 0xFFF5B700.toInt()
    private var colorMiddle: Int = 0xFF18AEF5.toInt()
    private var colorEnd: Int = 0xFF4B007D.toInt()
    private var ratioStart: Float = 0.12f
    private var ratioMiddle: Float = 0.10f
    private var ratioEnd: Float = 0.78f

    init {
        LayoutInflater.from(context).inflate(R.layout.view_toolbar_custom, this, true)
        tvTitle = findViewById(R.id.tvTitle)
        bar = findViewById(R.id.bar)

        radius = 0f
        cardElevation = 6f
        useCompatPadding = false
        preventCornerOverlap = false

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.ToolbarCustomView) {
                tvTitle.text = getString(R.styleable.ToolbarCustomView_tc_title) ?: tvTitle.text

                val bh = getDimension(R.styleable.ToolbarCustomView_tc_barHeight, -1f)
                if (bh > 0) {
                    bar.layoutParams = bar.layoutParams.apply { height = bh.toInt() }
                }

                colorStart = getColor(R.styleable.ToolbarCustomView_tc_colorStart, colorStart)
                colorMiddle = getColor(R.styleable.ToolbarCustomView_tc_colorMiddle, colorMiddle)
                colorEnd = getColor(R.styleable.ToolbarCustomView_tc_colorEnd, colorEnd)
                ratioStart = getFloat(R.styleable.ToolbarCustomView_tc_ratioStart, ratioStart)
                ratioMiddle = getFloat(R.styleable.ToolbarCustomView_tc_ratioMiddle, ratioMiddle)
                ratioEnd = getFloat(R.styleable.ToolbarCustomView_tc_ratioEnd, ratioEnd)
            }
        }

        buildSegments()
    }

    private fun buildSegments() {
        bar.removeAllViews()
        val total = max(ratioStart + ratioMiddle + ratioEnd, 0.0001f)

        fun addSeg(color: Int, ratio: Float) {
            val v = View(context).apply { setBackgroundColor(color) }
            val params = LinearLayout.LayoutParams(
                /* width  = */ 0,
                /* height = */ LinearLayout.LayoutParams.MATCH_PARENT,
                /* weight = */ ratio / total
            )
            bar.addView(v, params)
        }

        addSeg(colorStart, ratioStart)
        addSeg(colorMiddle, ratioMiddle)
        addSeg(colorEnd, ratioEnd)
    }

    fun setTitle(text: String) { tvTitle.text = text }

}