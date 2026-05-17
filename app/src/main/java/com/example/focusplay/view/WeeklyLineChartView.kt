package com.example.focusplay.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

data class DataGrafikHarian(
    val labelHari: String,
    val nilaiAkurasi: Float
)

class WeeklyLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dataGrafik: List<DataGrafikHarian> = emptyList()

    private val garisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E15B94")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val titikPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E15B94")
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5E7EB")
        strokeWidth = 2f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 24f
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#111827")
        textSize = 31f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 23f
    }

    fun setData(data: List<DataGrafikHarian>) {
        dataGrafik = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = 70f
        val paddingRight = 32f
        val paddingTop = 90f
        val paddingBottom = 60f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        canvas.drawText("Visualisasi Data Sesi", paddingLeft, 38f, titlePaint)
        canvas.drawText("Rata-rata akurasi per hari", paddingLeft, 70f, subTitlePaint)

        if (dataGrafik.isEmpty()) {
            canvas.drawText(
                "Belum ada data sesi bermain",
                paddingLeft,
                paddingTop + 80f,
                textPaint
            )
            return
        }

        val minValue = 0f
        val maxValue = 100f
        val totalGrid = 5
        val stepY = chartHeight / totalGrid

        for (i in 0..totalGrid) {
            val y = paddingTop + (i * stepY)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)

            val value = (maxValue - (i * ((maxValue - minValue) / totalGrid))).toInt()
            canvas.drawText(value.toString(), 18f, y + 8f, textPaint)
        }

        val path = Path()

        dataGrafik.forEachIndexed { index, item ->
            val x = if (dataGrafik.size == 1) {
                paddingLeft + chartWidth / 2
            } else {
                paddingLeft + (index * (chartWidth / (dataGrafik.size - 1)))
            }

            val nilai = item.nilaiAkurasi.coerceIn(0f, 100f)
            val normalizedValue = (nilai - minValue) / (maxValue - minValue)
            val y = paddingTop + chartHeight - (normalizedValue * chartHeight)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            canvas.drawCircle(x, y, 8f, titikPaint)
            canvas.drawText(item.labelHari, x - 18f, height - 18f, textPaint)
        }

        canvas.drawPath(path, garisPaint)
    }
}