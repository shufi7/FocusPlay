package com.example.focusplay.utils

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.focusplay.R

object ErrorDialogHelper {

    fun showErrorDialog(
        activity: Activity,
        title: String,
        message: String,
        buttonText: String = "Coba Lagi",
        onButtonClick: (() -> Unit)? = null
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_error, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogErrorTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogErrorMessage)
        val btnAction = dialogView.findViewById<ImageButton>(R.id.btnDialogErrorAction)

        tvTitle.text = title
        tvMessage.text = message

        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        aturTombolCepat(btnAction) {
            dialog.dismiss()
            onButtonClick?.invoke()
        }

        dialog.show()
    }

    private fun aturTombolCepat(view: View, aksi: () -> Unit) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.94f)
                        .scaleY(0.94f)
                        .alpha(0.85f)
                        .setDuration(40)
                        .start()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(40)
                        .start()

                    v.performClick()
                    aksi()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(40)
                        .start()
                    true
                }

                else -> true
            }
        }
    }
}