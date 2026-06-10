package com.example.focusplay.games

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView

object GameMenuStyle {
    fun createPanelBackground(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 24).toFloat()
            setColor(Color.parseColor("#FFF8E4"))
            setStroke(dp(context, 4), Color.parseColor("#FFB347"))
        }
    }

    fun createMenuButton(
        context: Context,
        text: String,
        topColor: String,
        bottomColor: String
    ): TextView {
        return TextView(context).apply {
            this.text = text
            gravity = android.view.Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            includeFontPadding = true
            background = createButtonBackground(context, topColor, bottomColor)
            isClickable = true
            isFocusable = true
            minHeight = 0
            setPadding(dp(context, 16), 0, dp(context, 16), 0)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 52)
            ).apply {
                setMargins(0, dp(context, 8), 0, dp(context, 8))
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> view.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(45)
                        .start()

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(70)
                        .start()
                }
                false
            }
        }
    }

    private fun createButtonBackground(
        context: Context,
        topColor: String,
        bottomColor: String
    ): StateListDrawable {
        return StateListDrawable().apply {
            addState(
                intArrayOf(android.R.attr.state_pressed),
                createButtonLayer(context, darken(topColor), darken(bottomColor))
            )
            addState(
                intArrayOf(),
                createButtonLayer(context, Color.parseColor(topColor), Color.parseColor(bottomColor))
            )
        }
    }

    private fun createButtonLayer(
        context: Context,
        topColor: Int,
        bottomColor: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 14).toFloat()
            setColor(topColor)
            setStroke(dp(context, 1), darken(bottomColor))
        }
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    private fun darken(color: String): Int {
        return darken(Color.parseColor(color))
    }

    private fun darken(color: Int): Int {
        return Color.rgb(
            (Color.red(color) * 0.86f).toInt(),
            (Color.green(color) * 0.86f).toInt(),
            (Color.blue(color) * 0.86f).toInt()
        )
    }
}
