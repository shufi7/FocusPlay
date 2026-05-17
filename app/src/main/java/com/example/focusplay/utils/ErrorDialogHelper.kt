package com.example.focusplay.utils

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
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
        val btnAction = dialogView.findViewById<Button>(R.id.btnDialogErrorAction)

        tvTitle.text = title
        tvMessage.text = message
        btnAction.text = buttonText

        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnAction.setOnClickListener {
            dialog.dismiss()
            onButtonClick?.invoke()
        }

        dialog.show()
    }
}