package com.example.focusplay.utils

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object AuthBottomSheetHelper {

    fun setup(dialog: BottomSheetDialog, maxHeightRatio: Float = 0.9f) {
        dialog.setOnShowListener {
            val sheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            ) ?: return@setOnShowListener

            val maxHeight = (sheet.resources.displayMetrics.heightPixels * maxHeightRatio).toInt()
            val behavior = BottomSheetBehavior.from(sheet)

            sheet.setBackgroundColor(Color.TRANSPARENT)
            sheet.layoutParams = sheet.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            sheet.requestLayout()

            behavior.apply {
                isDraggable = true
                isFitToContents = true
                isHideable = true
                skipCollapsed = true
                this.maxHeight = maxHeight
                peekHeight = maxHeight
            }

            sheet.post {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }
}