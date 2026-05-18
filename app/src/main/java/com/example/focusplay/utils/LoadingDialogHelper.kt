package com.example.focusplay.utils

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.example.focusplay.R

class LoadingDialogHelper(private val activity: Activity) {

    private var dialog: AlertDialog? = null

    fun show() {
        if (dialog?.isShowing == true) return

        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null)

        dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
    }

    fun dismiss() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }
}