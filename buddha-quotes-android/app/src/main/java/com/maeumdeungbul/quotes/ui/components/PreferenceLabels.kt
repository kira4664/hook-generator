package com.maeumdeungbul.quotes.ui.components

import androidx.annotation.StringRes
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.ThemeMode

@StringRes
fun FontScale.labelRes(): Int = when (this) {
    FontScale.SMALL -> R.string.font_small
    FontScale.DEFAULT -> R.string.font_default
    FontScale.LARGE -> R.string.font_large
    FontScale.EXTRA_LARGE -> R.string.font_extra_large
}

@StringRes
fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
