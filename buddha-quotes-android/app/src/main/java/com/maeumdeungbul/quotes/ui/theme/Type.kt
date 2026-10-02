package com.maeumdeungbul.quotes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val Base = Typography()

// 제목·말씀은 명조(Serif) 계열로 고요한 인상을, 본문은 시스템 고딕으로 가독성을 확보한다.
// 단위는 sp 이므로 시스템 글자 크기 설정을 그대로 따른다.
internal val MaeumTypography = Typography(
    displaySmall = Base.displaySmall.copy(fontFamily = FontFamily.Serif),
    headlineLarge = Base.headlineLarge.copy(fontFamily = FontFamily.Serif),
    headlineMedium = Base.headlineMedium.copy(fontFamily = FontFamily.Serif),
    headlineSmall = Base.headlineSmall.copy(fontFamily = FontFamily.Serif),
    titleLarge = Base.titleLarge.copy(fontFamily = FontFamily.Serif),
    bodyLarge = Base.bodyLarge.copy(lineHeight = 26.sp),
    bodyMedium = Base.bodyMedium.copy(lineHeight = 22.sp),
)

/** 말씀 본문 전용 스타일. 글자 크기 설정(Phase 3)은 이 스타일에 배율을 곱해 적용한다. */
val Typography.quote: TextStyle
    get() = headlineSmall.copy(lineHeight = 40.sp)
