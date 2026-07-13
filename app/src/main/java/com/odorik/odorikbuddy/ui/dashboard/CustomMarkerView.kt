package com.odorik.odorikbuddy.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.odorik.odorikbuddy.R

@SuppressLint("ViewConstructor")
class CustomMarkerView(context: Context, layoutResource: Int, private val dates: List<String>, private val spendingData: List<DashboardViewModel.ChartDay>) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) {
            visibility = View.GONE
            super.refreshContent(e, highlight)
            return
        }

        val index = e.x.toInt()
        if (index < 0 || index >= dates.size || index >= spendingData.size) {
            visibility = View.GONE
            super.refreshContent(e, highlight)
            return
        }

        val date = dates[index]
        val spending = spendingData[index].spending
        if (spending > 0) {
            tvContent.text = context.getString(R.string.spending_for_date, date, spending)

            tvContent.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
            )

            measure(
                android.view.View.MeasureSpec.makeMeasureSpec(tvContent.measuredWidth + 32, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(tvContent.measuredHeight + 32, android.view.View.MeasureSpec.EXACTLY)
            )

            layout(0, 0, measuredWidth, measuredHeight)
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
        super.refreshContent(e, highlight)
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        if (visibility != View.VISIBLE) {
            return
        }
        super.draw(canvas, posX, posY)
    }

    override fun getOffset(): MPPointF {
        val offset = MPPointF(-(width / 2f), -height.toFloat())
        return offset
    }
}