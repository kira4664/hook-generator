package com.maeumdeungbul.quotes.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.QuoteBackground

/** 사용자 설정(글자 크기·배경)을 모든 말씀 화면에 전달한다. */
@Immutable
data class QuoteAppearance(
    val fontScale: FontScale = FontScale.DEFAULT,
    val background: QuoteBackground = QuoteBackground.LOTUS,
)

val LocalQuoteAppearance = staticCompositionLocalOf { QuoteAppearance() }

fun TextStyle.scaled(multiplier: Float): TextStyle =
    if (multiplier == 1f) this else copy(fontSize = fontSize * multiplier, lineHeight = lineHeight * multiplier)
