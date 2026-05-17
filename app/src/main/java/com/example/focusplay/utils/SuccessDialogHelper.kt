package com.example.focusplay.utils

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.example.focusplay.R

object SuccessDialogHelper {

    fun showSuccessDialog(
        activity: Activity,
        title: String,
        message: String,
        buttonText: String = "Lanjut",
        onButtonClick: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_success, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnAction = dialogView.findViewById<Button>(R.id.btnDialogAction)

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
            onButtonClick()
        }

        dialog.show()
    }
}