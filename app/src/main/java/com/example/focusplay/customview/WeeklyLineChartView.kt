package com.example.focusplay.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
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
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#111827")
        textSize = 31f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 23f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 25f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    fun setData(data: List<DataGrafikHarian>) {
        dataGrafik = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = 70f
        val paddingRight = 32f
        val paddingTop = 95f
        val paddingBottom = 62f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        canvas.drawText("Visualisasi Data Sesi", paddingLeft, 38f, titlePaint)
        canvas.drawText("Rata-rata akurasi per hari", paddingLeft, 72f, subTitlePaint)

        gambarGrid(canvas, paddingLeft, paddingRight, paddingTop, chartHeight)

        if (dataGrafik.isEmpty()) {
            gambarDataKosong(canvas, paddingTop, chartHeight)
            return
        }

        gambarGarisGrafik(canvas, paddingLeft, chartWidth, paddingTop, chartHeight)
    }

    private fun gambarGrid(
        canvas: Canvas,
        paddingLeft: Float,
        paddingRight: Float,
        paddingTop: Float,
        chartHeight: Float
    ) {
        val minValue = 0f
        val maxValue = 100f
        val totalGrid = 5
        val stepY = chartHeight / totalGrid

        for (i in 0..totalGrid) {
            val y = paddingTop + (i * stepY)

            canvas.drawLine(
                paddingLeft,
                y,
                width - paddingRight,
                y,
                gridPaint
            )

            val value = (maxValue - (i * ((maxValue - minValue) / totalGrid))).toInt()
            canvas.drawText(value.toString(), 18f, y + 8f, textPaint)
        }
    }

    private fun gambarDataKosong(
        canvas: Canvas,
        paddingTop: Float,
        chartHeight: Float
    ) {
        canvas.drawText(
            "Belum ada data sesi bermain",
            width / 2f,
            paddingTop + (chartHeight / 2f),
            emptyPaint
        )

        canvas.drawText(
            "Grafik akan muncul setelah anak bermain",
            width / 2f,
            paddingTop + (chartHeight / 2f) + 36f,
            emptyPaint
        )
    }

    private fun gambarGarisGrafik(
        canvas: Canvas,
        paddingLeft: Float,
        chartWidth: Float,
        paddingTop: Float,
        chartHeight: Float
    ) {
        val minValue = 0f
        val maxValue = 100f

        val path = Path()

        dataGrafik.forEachIndexed { index, item ->
            val x = if (dataGrafik.size == 1) {
                paddingLeft + chartWidth / 2f
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
            canvas.drawText(item.labelHari, x - 18f, height - 20f, textPaint)
        }

        canvas.drawPath(path, garisPaint)
    }
}