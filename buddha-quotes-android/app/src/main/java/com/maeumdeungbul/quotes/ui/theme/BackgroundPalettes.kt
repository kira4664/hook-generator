package com.maeumdeungbul.quotes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.maeumdeungbul.quotes.domain.model.QuoteBackground

/**
 * 말씀 카드 배경 그림의 색. 그림(일러스트) 전용 팔레트이며 UI 색은 MaterialTheme 에서 관리한다.
 * [content] 는 배경 위 글자색으로, 각 배경에서 명암 대비 4.5:1 이상이 되도록 골랐다.
 */
@Immutable
data class BackgroundStyle(
    val gradient: List<Color>,
    val accent: Color,
    val content: Color,
    val contentSecondary: Color,
)

@Composable
fun QuoteBackground.style(): BackgroundStyle = when (this) {
    QuoteBackground.LOTUS -> BackgroundStyle(
        gradient = listOf(Color(0xFFFBF1EC), Color(0xFFF2D9D2)),
        accent = Color(0xFFE2A99F),
        content = Color(0xFF3A2622),
        contentSecondary = Color(0xFF6B4A43),
    )
    QuoteBackground.MOUNTAIN -> BackgroundStyle(
        gradient = listOf(Color(0xFFEDF1F2), Color(0xFFCDD9DE)),
        accent = Color(0xFF8FA4AE),
        content = Color(0xFF1F2A30),
        contentSecondary = Color(0xFF42535C),
    )
    QuoteBackground.FOREST -> BackgroundStyle(
        gradient = listOf(Color(0xFF2F4A3A), Color(0xFF1B2E24)),
        accent = Color(0xFF4D7259),
        content = Color(0xFFF1F5EE),
        contentSecondary = Color(0xFFC9D8CC),
    )
    QuoteBackground.TEMPLE -> BackgroundStyle(
        gradient = listOf(Color(0xFFF6EAD6), Color(0xFFE8D0AC)),
        accent = Color(0xFF8A5A3C),
        content = Color(0xFF3B2A1A),
        contentSecondary = Color(0xFF654A33),
    )
    QuoteBackground.SKY -> BackgroundStyle(
        gradient = listOf(Color(0xFFBFDCEE), Color(0xFFEAF4FA)),
        accent = Color(0xFFFFFFFF),
        content = Color(0xFF1B3140),
        contentSecondary = Color(0xFF3E5868),
    )
    QuoteBackground.SUNSET -> BackgroundStyle(
        gradient = listOf(Color(0xFF6E4A7E), Color(0xFFB9636F), Color(0xFFE59A6E)),
        accent = Color(0xFFFCE3A6),
        content = Color(0xFFFFF8F0),
        contentSecondary = Color(0xFFF6E1D3),
    )
    QuoteBackground.WATER -> BackgroundStyle(
        gradient = listOf(Color(0xFF1F4E5F), Color(0xFF2B6E80)),
        accent = Color(0xFF7FC0CF),
        content = Color(0xFFEFF8FA),
        contentSecondary = Color(0xFFC5E1E8),
    )
    QuoteBackground.MINIMAL -> BackgroundStyle(
        gradient = listOf(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.surfaceContainer),
        accent = MaterialTheme.colorScheme.outlineVariant,
        content = MaterialTheme.colorScheme.onSurface,
        contentSecondary = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
