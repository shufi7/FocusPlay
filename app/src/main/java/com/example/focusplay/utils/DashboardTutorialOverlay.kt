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

/**
 * Menampilkan tutorial di atas DashboardActivity.
 *
 * Bentuk kotak diambil dari view_dashboard_tutorial_overlay.xml. Target, judul, dan pesan
 * setiap langkahnya dikirim dari DashboardActivity lewat setSteps().
 */
class DashboardTutorialOverlay(
    private val activity: Activity
) : FrameLayout(activity) {

    // ==================== BAGIAN WARNA DAN AREA SOROTAN ====================
    // Warna gelap untuk menutupi area di luar target.
    private val paintDim = Paint().apply {
        color = Color.parseColor("#B3000000")
        style = Paint.Style.FILL
    }

    // Paint CLEAR digunakan untuk membuat area target transparan.
    private val paintClear = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    // Garis hijau di sekeliling target yang sedang dijelaskan.
    private val paintStroke = Paint().apply {
        color = Color.parseColor("#8DB52A")
        style = Paint.Style.STROKE
        strokeWidth = dp(3).toFloat()
        isAntiAlias = true
    }

    // Menyimpan posisi target dalam koordinat overlay.
    private val targetRect = RectF()
    // Menyimpan indeks langkah tutorial yang sedang aktif.
    private var currentStep = 0

    // ==================== BAGIAN ELEMEN TUTORIAL ====================
    private val titleView: TextView
    private val messageView: TextView
    private val stepView: TextView
    private val btnNext: Button
    private val btnSkip: TextView
    private val tutorialBox: View

    // Menampung daftar langkah yang dikirim DashboardActivity.
    private val steps = mutableListOf<TutorialStep>()

    data class TutorialStep(
        val target: View,
        val title: String,
        val message: String
    )

    // ==================== BAGIAN INISIALISASI OVERLAY ====================
    init {
        // Mengizinkan FrameLayout menjalankan onDraw().
        setWillNotDraw(false)
        // Layer software diperlukan untuk efek PorterDuff CLEAR.
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // Kotak dan tombol berasal dari XML, sedangkan area sorotan digambar di onDraw().
        val content = LayoutInflater.from(activity)
            .inflate(R.layout.view_dashboard_tutorial_overlay, this, true)

        // Menghubungkan elemen hasil inflate dengan variabel Kotlin.
        tutorialBox = content.findViewById(R.id.tutorialBox)
        titleView = content.findViewById(R.id.tvTutorialTitle)
        messageView = content.findViewById(R.id.tvTutorialMessage)
        stepView = content.findViewById(R.id.tvTutorialStep)
        btnNext = content.findViewById(R.id.btnNextTutorial)
        btnSkip = content.findViewById(R.id.btnSkipTutorial)

        // Tombol Lewati langsung menyelesaikan tutorial.
        btnSkip.setOnClickListener {
            finishTutorial()
        }

        // Tombol lanjut berpindah langkah atau menyelesaikan tutorial terakhir.
        btnNext.setOnClickListener {
            if (currentStep < steps.lastIndex) {
                currentStep++
                showStep()
            } else {
                finishTutorial()
            }
        }
    }

    // ==================== BAGIAN MULAI DAN GANTI LANGKAH ====================
    fun setSteps(data: List<TutorialStep>) {
        // Daftar View yang mau dijelaskan dan disorot masuk dari DashboardActivity.
        // Mengganti daftar langkah lama dengan data baru.
        steps.clear()
        steps.addAll(data)
    }

    fun start() {
        // Tutorial tidak dijalankan jika belum memiliki langkah.
        if (steps.isEmpty()) return
        currentStep = 0

        // Memasang overlay ke decorView agar menutupi seluruh Dashboard.
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
        // Mengganti isi teks sesuai langkah lalu menggeser halaman ke target.
        // Mengambil langkah berdasarkan indeks aktif.
        val step = steps[currentStep]

        // Memasukkan teks langkah ke elemen XML.
        titleView.text = step.title
        messageView.text = step.message
        stepView.text = "${currentStep + 1} dari ${steps.size}"

        // Mengubah tulisan tombol pada langkah terakhir.
        btnNext.text = if (currentStep == steps.lastIndex) {
            "Selesai"
        } else {
            "Lanjut"
        }

        // Menggeser layar ke target sebelum menghitung dan menggambar sorotan.
        scrollKeTarget(step.target) {
            hitungTargetRect(step.target)
            aturPosisiBox()
            invalidate()
        }
    }

    // ==================== BAGIAN SCROLL KE TARGET ====================
    private fun scrollKeTarget(target: View, setelahScroll: () -> Unit) {
        // Mencari NestedScrollView karena target dapat berada di bawah layar.
        // Menelusuri parent View untuk menemukan NestedScrollView.
        val scrollView = cariNestedScrollView(target)

        if (scrollView != null) {
            // Menghitung posisi scroll agar target memiliki jarak dari atas.
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

    // ==================== BAGIAN POSISI SOROTAN DAN KOTAK ====================
    private fun hitungTargetRect(target: View) {
        val location = IntArray(2)
        val rootLocation = IntArray(2)

        // Mengambil koordinat target dan overlay pada layar.
        target.getLocationOnScreen(location)
        this.getLocationOnScreen(rootLocation)

        // Memberi ruang tambahan delapan dp di sekitar target.
        val left = location[0] - rootLocation[0] - dp(8)
        val top = location[1] - rootLocation[1] - dp(8)
        val right = left + target.width + dp(16)
        val bottom = top + target.height + dp(16)

        targetRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    private fun aturPosisiBox() {
        // Mengukur tinggi kotak tutorial sebelum menentukan posisinya.
        tutorialBox.measure(
            MeasureSpec.makeMeasureSpec(width - dp(40), MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        )

        val boxWidth = width - dp(40)
        val boxHeight = tutorialBox.measuredHeight

        val params = tutorialBox.layoutParams as LayoutParams
        params.width = boxWidth

        // Menentukan apakah kotak cukup diletakkan di bawah target.
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

    // ==================== BAGIAN GAMBAR OVERLAY ====================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Menggelapkan layar, lalu membuat area target transparan dan diberi garis.
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintDim)

        val radius = dp(18).toFloat()
        canvas.drawRoundRect(targetRect, radius, radius, paintClear)
        canvas.drawRoundRect(targetRect, radius, radius, paintStroke)
    }

    // ==================== BAGIAN SELESAI TUTORIAL ====================
    private fun finishTutorial() {
        // Menyimpan status selesai agar tutorial tidak muncul kembali.
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
