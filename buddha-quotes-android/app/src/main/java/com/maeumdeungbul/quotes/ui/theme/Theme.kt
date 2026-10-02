package com.maeumdeungbul.quotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Bark40,
    onPrimary = Ivory,
    primaryContainer = Bark90,
    onPrimaryContainer = Bark10,
    secondary = Sand40,
    onSecondary = Ivory,
    secondaryContainer = Sand90,
    onSecondaryContainer = Sand10,
    tertiary = Celadon40,
    onTertiary = Ivory,
    tertiaryContainer = Celadon90,
    onTertiaryContainer = Celadon10,
    background = Ivory,
    onBackground = Ink,
    surface = Ivory,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = InkSoft,
    outline = Stone,
    outlineVariant = StoneLight,
    surfaceContainerLowest = SurfaceLowestLight,
    surfaceContainerLow = SurfaceLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceHighLight,
    surfaceContainerHighest = SurfaceHighestLight,
)

private val DarkColors = darkColorScheme(
    primary = Bark80,
    onPrimary = Bark20,
    primaryContainer = Bark30,
    onPrimaryContainer = Bark90,
    secondary = Sand80,
    onSecondary = Sand20,
    secondaryContainer = Sand30,
    onSecondaryContainer = Sand90,
    tertiary = Celadon80,
    onTertiary = Celadon20,
    tertiaryContainer = Celadon30,
    onTertiaryContainer = Celadon90,
    background = InkNight,
    onBackground = IvoryNight,
    surface = InkNight,
    onSurface = IvoryNight,
    surfaceVariant = InkSoft,
    onSurfaceVariant = StoneLight,
    outline = StoneDark,
    outlineVariant = InkSoft,
    surfaceContainerLowest = SurfaceLowestDark,
    surfaceContainerLow = SurfaceLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceHighDark,
    surfaceContainerHighest = SurfaceHighestDark,
)

/**
 * 앱 전체 테마. 브랜드 색 유지를 위해 Dynamic Color(Material You)는 사용하지 않는다.
 * Phase 8에서 사용자 설정(시스템/밝게/어둡게)이 [darkTheme] 으로 전달된다.
 */
@Composable
fun MaeumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaeumTypography,
        shapes = MaeumShapes,
        content = content,
    )
}
