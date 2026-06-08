package com.example.focusplay.utils

import androidx.annotation.DrawableRes
import com.example.focusplay.R

object AvatarHelper {
    @DrawableRes
    fun getAvatarResource(avatar: String?): Int {
        return when (avatar) {
            "char_red" -> R.drawable.char_red
            "char_blue" -> R.drawable.char_blue
            "char_purple" -> R.drawable.char_purple
            "char_star" -> R.drawable.char_star
            "char_mushroom" -> R.drawable.char_mushroom

            "char_moon_purple" -> R.drawable.char_moon_purple
            "char_cucumber" -> R.drawable.char_cucumber
            "char_cloud_blue" -> R.drawable.char_cloud_blue
            "char_heart" -> R.drawable.char_heart
            "char_diamond_orange" -> R.drawable.char_diamond_orange

            else -> R.drawable.char_red
        }
    }
}