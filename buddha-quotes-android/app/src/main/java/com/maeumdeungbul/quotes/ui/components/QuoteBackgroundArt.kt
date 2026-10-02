package com.maeumdeungbul.quotes.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.maeumdeungbul.quotes.domain.model.QuoteBackground
import com.maeumdeungbul.quotes.ui.theme.BackgroundStyle

/**
 * 배경을 사진·일러스트 파일로 바꾸고 싶다면 여기에 drawable 을 연결한다(라이선스가 명확한 자체 이미지만).
 * null 이면 코드로 그린 기본 배경을 사용한다.
 */
@DrawableRes
private fun QuoteBackground.imageRes(): Int? = when (this) {
    QuoteBackground.LOTUS,
    QuoteBackground.MOUNTAIN,
    QuoteBackground.FOREST,
    QuoteBackground.TEMPLE,
    QuoteBackground.SKY,
    QuoteBackground.SUNSET,
    QuoteBackground.WATER,
    QuoteBackground.MINIMAL,
    -> null
}

/** 말씀 배경. 이미지 공유 카드에서도 그대로 그려진다. */
@Composable
fun QuoteBackgroundBox(
    background: QuoteBackground,
    style: BackgroundStyle,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        val image = background.imageRes()
        if (image != null) {
            Image(
                painter = painterResource(image),
                contentDescription = null, // 장식용 배경
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
        } else {
            Box(
                Modifier
                    .matchParentSize()
                    .drawBehind {
                        drawRect(Brush.verticalGradient(style.gradient))
                        drawMotif(background, style.accent)
                    },
            )
        }
        content()
    }
}

private fun DrawScope.drawMotif(background: QuoteBackground, accent: Color) {
    val w = size.width
    val h = size.height
    when (background) {
        QuoteBackground.LOTUS -> drawLotus(accent.copy(alpha = 0.45f))
        QuoteBackground.MOUNTAIN -> {
            drawPath(
                Path().apply {
                    moveTo(0f, h * 0.72f)
                    lineTo(w * 0.22f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.66f)
                    lineTo(w * 0.68f, h * 0.46f)
                    lineTo(w, h * 0.62f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                },
                accent.copy(alpha = 0.35f),
            )
            drawPath(
                Path().apply {
                    moveTo(0f, h * 0.84f)
                    lineTo(w * 0.3f, h * 0.7f)
                    lineTo(w * 0.55f, h * 0.82f)
                    lineTo(w * 0.8f, h * 0.72f)
                    lineTo(w, h * 0.8f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                },
                accent.copy(alpha = 0.55f),
            )
        }
        QuoteBackground.FOREST -> {
            val trees = listOf(0.06f to 0.22f, 0.2f to 0.3f, 0.34f to 0.2f, 0.62f to 0.26f, 0.78f to 0.34f, 0.93f to 0.24f)
            trees.forEach { (x, height) ->
                val baseY = h
                val treeW = w * 0.14f
                drawPath(
                    Path().apply {
                        moveTo(w * x, baseY - h * height)
                        lineTo(w * x + treeW / 2, baseY)
                        lineTo(w * x - treeW / 2, baseY)
                        close()
                    },
                    accent.copy(alpha = 0.6f),
                )
            }
        }
        QuoteBackground.TEMPLE -> {
            // 처마 끝이 살짝 들린 기와지붕 실루엣
            val roofTop = h * 0.74f
            drawPath(
                Path().apply {
                    moveTo(w * 0.02f, roofTop - h * 0.03f)
                    cubicTo(w * 0.2f, roofTop + h * 0.02f, w * 0.8f, roofTop + h * 0.02f, w * 0.98f, roofTop - h * 0.03f)
                    lineTo(w * 0.9f, roofTop + h * 0.05f)
                    cubicTo(w * 0.7f, roofTop + h * 0.03f, w * 0.3f, roofTop + h * 0.03f, w * 0.1f, roofTop + h * 0.05f)
                    close()
                },
                accent.copy(alpha = 0.55f),
            )
            val pillarW = w * 0.035f
            listOf(0.22f, 0.5f, 0.78f).forEach { x ->
                drawRect(
                    color = accent.copy(alpha = 0.35f),
                    topLeft = Offset(w * x - pillarW / 2, roofTop + h * 0.05f),
                    size = Size(pillarW, h - roofTop),
                )
            }
        }
        QuoteBackground.SKY -> {
            val cloud = accent.copy(alpha = 0.7f)
            listOf(Offset(w * 0.2f, h * 0.82f), Offset(w * 0.75f, h * 0.2f)).forEach { center ->
                val r = w * 0.07f
                drawCircle(cloud, r, center)
                drawCircle(cloud, r * 1.3f, center + Offset(r * 1.2f, -r * 0.3f))
                drawCircle(cloud, r, center + Offset(r * 2.4f, 0f))
            }
        }
        QuoteBackground.SUNSET -> {
            drawCircle(accent.copy(alpha = 0.55f), radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.86f))
            drawRect(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(0f, h * 0.86f),
                size = Size(w, h * 0.14f),
            )
        }
        QuoteBackground.WATER -> {
            val stroke = Stroke(width = w * 0.006f)
            listOf(0.72f, 0.82f, 0.92f).forEachIndexed { index, y ->
                val path = Path().apply {
                    moveTo(0f, h * y)
                    var x = 0f
                    val wave = w / 4f
                    while (x < w) {
                        cubicTo(x + wave * 0.25f, h * y - h * 0.02f, x + wave * 0.75f, h * y + h * 0.02f, x + wave, h * y)
                        x += wave
                    }
                }
                drawPath(path, accent.copy(alpha = 0.6f - index * 0.15f), style = stroke)
            }
        }
        QuoteBackground.MINIMAL -> {
            // 엷은 원상(圓相)
            val d = minOf(w, h) * 0.32f
            drawArc(
                color = accent.copy(alpha = 0.6f),
                startAngle = -70f,
                sweepAngle = 320f,
                useCenter = false,
                topLeft = Offset(w - d * 1.15f, h * 0.06f),
                size = Size(d, d),
                style = Stroke(width = d * 0.05f),
            )
        }
    }
}

/** 앱 아이콘과 같은 연꽃 모양을 카드 아래쪽에 크게 그린다. */
private fun DrawScope.drawLotus(color: Color) {
    val scale = size.width * 0.55f / 52f
    val originX = size.width / 2f - 54f * scale
    val originY = size.height * 1.02f - 66f * scale
    fun x(v: Float) = originX + v * scale
    fun y(v: Float) = originY + v * scale
    val petals = listOf(
        floatArrayOf(54f, 30f, 60f, 38f, 62f, 48f, 54f, 62f, 46f, 48f, 48f, 38f),
        floatArrayOf(54f, 62f, 44f, 58f, 36f, 50f, 34f, 40f, 42f, 42f, 50f, 48f),
        floatArrayOf(54f, 62f, 64f, 58f, 72f, 50f, 74f, 40f, 66f, 42f, 58f, 48f),
        floatArrayOf(54f, 64f, 44f, 66f, 34f, 62f, 28f, 54f, 38f, 52f, 48f, 56f),
        floatArrayOf(54f, 64f, 64f, 66f, 74f, 62f, 80f, 54f, 70f, 52f, 60f, 56f),
    )
    petals.forEach { p ->
        drawPath(
            Path().apply {
                moveTo(x(p[0]), y(p[1]))
                cubicTo(x(p[2]), y(p[3]), x(p[4]), y(p[5]), x(p[6]), y(p[7]))
                cubicTo(x(p[8]), y(p[9]), x(p[10]), y(p[11]), x(p[0]), y(p[1]))
                close()
            },
            color,
        )
    }
}
