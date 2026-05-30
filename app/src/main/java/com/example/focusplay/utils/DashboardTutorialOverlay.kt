package com.example.focusplay.utils

import android.app.Activity
import android.graphics.*
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.example.focusplay.R
import android.view.ViewParent
import androidx.core.widget.NestedScrollView

class DashboardTutorialOverlay(
    private val activity: Activity
) : FrameLayout(activity) {

    private val paintDim = Paint().apply {
        color = Color.parseColor("#B3000000")
        style = Paint.Style.FILL
    }

    private val paintClear = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    private val paintStroke = Paint().apply {
        color = Color.parseColor("#8DB52A")
        style = Paint.Style.STROKE
        strokeWidth = dp(3).toFloat()
        isAntiAlias = true
    }

    private val targetRect = RectF()
    private var currentStep = 0

    private val titleView: TextView
    private val messageView: TextView
    private val stepView: TextView
    private val btnNext: Button
    private val btnSkip: TextView
    private val tutorialBox: View

    private val steps = mutableListOf<TutorialStep>()

    data class TutorialStep(
        val target: View,
        val title: String,
        val message: String
    )

    init {
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val content = LayoutInflater.from(activity)
            .inflate(R.layout.view_dashboard_tutorial_overlay, this, true)

        tutorialBox = content.findViewById(R.id.tutorialBox)
        titleView = content.findViewById(R.id.tvTutorialTitle)
        messageView = content.findViewById(R.id.tvTutorialMessage)
        stepView = content.findViewById(R.id.tvTutorialStep)
        btnNext = content.findViewById(R.id.btnNextTutorial)
        btnSkip = content.findViewById(R.id.btnSkipTutorial)

        btnSkip.setOnClickListener {
            finishTutorial()
        }

        btnNext.setOnClickListener {
            if (currentStep < steps.lastIndex) {
                currentStep++
                showStep()
            } else {
                finishTutorial()
            }
        }
    }

    fun setSteps(data: List<TutorialStep>) {
        steps.clear()
        steps.addAll(data)
    }

    fun start() {
        if (steps.isEmpty()) return
        currentStep = 0

        val root = activity.window.decorView as ViewGroup
        root.addView(
            this,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        post {
            showStep()
        }
    }

    private fun showStep() {
        val step = steps[currentStep]

        titleView.text = step.title
        messageView.text = step.message
        stepView.text = "${currentStep + 1} dari ${steps.size}"

        btnNext.text = if (currentStep == steps.lastIndex) {
            "Selesai"
        } else {
            "Lanjut"
        }

        scrollKeTarget(step.target) {
            hitungTargetRect(step.target)
            aturPosisiBox()
            invalidate()
        }
    }

    private fun scrollKeTarget(target: View, setelahScroll: () -> Unit) {
        val scrollView = cariNestedScrollView(target)

        if (scrollView != null) {
            val targetTop = hitungTopRelatifKeScrollView(target, scrollView)
            val posisiScroll = maxOf(0, targetTop - dp(24))

            scrollView.smoothScrollTo(0, posisiScroll)

            postDelayed({
                setelahScroll()
            }, 450)
        } else {
            target.requestRectangleOnScreen(
                Rect(0, 0, target.width, target.height),
                true
            )

            postDelayed({
                setelahScroll()
            }, 200)
        }
    }

    private fun cariNestedScrollView(view: View): NestedScrollView? {
        var parent: ViewParent? = view.parent

        while (parent != null) {
            if (parent is NestedScrollView) {
                return parent
            }

            parent = parent.parent
        }

        return null
    }

    private fun hitungTopRelatifKeScrollView(target: View, scrollView: NestedScrollView): Int {
        var top = target.top
        var parent: ViewParent? = target.parent

        while (parent is View && parent != scrollView) {
            top += parent.top
            parent = parent.parent
        }

        return top
    }

    private fun hitungTargetRect(target: View) {
        val location = IntArray(2)
        val rootLocation = IntArray(2)

        target.getLocationOnScreen(location)
        this.getLocationOnScreen(rootLocation)

        val left = location[0] - rootLocation[0] - dp(8)
        val top = location[1] - rootLocation[1] - dp(8)
        val right = left + target.width + dp(16)
        val bottom = top + target.height + dp(16)

        targetRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    private fun aturPosisiBox() {
        tutorialBox.measure(
            MeasureSpec.makeMeasureSpec(width - dp(40), MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        )

        val boxWidth = width - dp(40)
        val boxHeight = tutorialBox.measuredHeight

        val params = tutorialBox.layoutParams as LayoutParams
        params.width = boxWidth

        val ruangBawah = height - targetRect.bottom
        val posisiY = if (ruangBawah > boxHeight + dp(40)) {
            targetRect.bottom.toInt() + dp(18)
        } else {
            maxOf(dp(24), targetRect.top.toInt() - boxHeight - dp(18))
        }

        params.leftMargin = dp(20)
        params.topMargin = posisiY
        tutorialBox.layoutParams = params
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintDim)

        val radius = dp(18).toFloat()
        canvas.drawRoundRect(targetRect, radius, radius, paintClear)
        canvas.drawRoundRect(targetRect, radius, radius, paintStroke)
    }

    private fun finishTutorial() {
        activity.getSharedPreferences("tutorial_dashboard", Activity.MODE_PRIVATE)
            .edit()
            .putBoolean("sudah_tampil_spotlight", true)
            .apply()

        val parent = parent as? ViewGroup
        parent?.removeView(this)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}