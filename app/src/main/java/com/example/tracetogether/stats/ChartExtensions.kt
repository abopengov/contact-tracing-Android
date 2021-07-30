package com.example.tracetogether.stats

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.splitBy
import com.example.tracetogether.util.Extensions.withCommas
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter

fun LineChart.setup() {
    description?.isEnabled = false
    axisRight?.isEnabled = false
    xAxis?.isEnabled = false
    axisLeft.axisMinimum = 0f
    legend?.isEnabled = false
    isDragEnabled = false
    setPinchZoom(false)
    setTouchEnabled(false)
    setScaleEnabled(false)
    setMaxVisibleValueCount(200)
    extraTopOffset = 15f
    extraRightOffset = 25f
    setNoDataTextColor(R.color.black)
}

fun LineChart.displayData(context: Context, labelFrequency: Int, data: List<Long?>) {
    val lineDataSets: List<LineDataSet> = data
            .mapIndexed { index, activeCaseCount ->
                Pair(index, activeCaseCount)
            }
            .splitBy { pair -> pair.second == null }
            .map { subList ->
                subList.map { pair ->
                    Entry(pair.first.toFloat(), pair.second?.toFloat() ?: 0f)
                }
            }
            .map { subList ->
                val lineDataSet = LineDataSet(subList, "")
                lineDataSet.color = ContextCompat.getColor(context, R.color.final_blue)
                lineDataSet.lineWidth = 3f
                lineDataSet.fillColor = ContextCompat.getColor(context, R.color.border_blue)
                lineDataSet.setDrawFilled(true)
                lineDataSet.setDrawCircles(false)
                lineDataSet.setDrawValues(true)
                lineDataSet.circleRadius = 20f
                lineDataSet.valueTextSize = 11f
                lineDataSet.valueFormatter = CustomLineDataFormatter(labelFrequency)
                lineDataSet.valueTextColor = ContextCompat.getColor(context, R.color.final_blue)
                lineDataSet.valueTypeface =
                        ResourcesCompat.getFont(context, R.font.muli_semibold)
                lineDataSet
            }

    val lineData = LineData(lineDataSets)
    this.data = lineData
    invalidate()
}

fun BarChart.setup() {
    description?.isEnabled = false
    axisRight?.isEnabled = false
    xAxis?.isEnabled = false
    axisLeft.axisMinimum = 0f
    legend?.isEnabled = false
    isDragEnabled = false
    setPinchZoom(false)
    setTouchEnabled(false)
    setScaleEnabled(false)
    setNoDataTextColor(R.color.black)
}

fun BarChart.displayData(context: Context, data: List<Long?>) {
    val barDataSet = BarDataSet(data.mapIndexed { index, activeCaseCount ->
        BarEntry(
            index.toFloat(),
            activeCaseCount?.toFloat() ?: 0f
        )
    }, "")
    barDataSet.setDrawValues(false)
    val startColor = ContextCompat.getColor(context, R.color.chart_blue_start)
    val endColor = ContextCompat.getColor(context, R.color.chart_blue_end)
    barDataSet.setGradientColor(startColor, endColor)

    val barData = BarData(listOf(barDataSet))
    this.data = barData
    invalidate()
}

private class CustomLineDataFormatter(val frequency: Int) : ValueFormatter() {
    var index = 0

    override fun getFormattedValue(value: Float): String {
        index += 1
        return if (index % frequency == 0)
            value.withCommas()
        else ""
    }
}