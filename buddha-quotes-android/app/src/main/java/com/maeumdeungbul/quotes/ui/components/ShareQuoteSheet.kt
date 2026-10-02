package com.maeumdeungbul.quotes.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteBackground
import com.maeumdeungbul.quotes.share.QuoteSharer
import com.maeumdeungbul.quotes.ui.theme.LocalQuoteAppearance
import com.maeumdeungbul.quotes.ui.theme.style
import kotlinx.coroutines.launch

private enum class ShareRatio(val aspect: Float, val widthFraction: Float, @StringRes val labelRes: Int) {
    SQUARE(1f, 1f, R.string.share_ratio_square),
    STORY(9f / 16f, 0.62f, R.string.share_ratio_story),
}

@StringRes
fun QuoteBackground.labelRes(): Int = when (this) {
    QuoteBackground.LOTUS -> R.string.background_lotus
    QuoteBackground.MOUNTAIN -> R.string.background_mountain
    QuoteBackground.FOREST -> R.string.background_forest
    QuoteBackground.TEMPLE -> R.string.background_temple
    QuoteBackground.SKY -> R.string.background_sky
    QuoteBackground.SUNSET -> R.string.background_sunset
    QuoteBackground.WATER -> R.string.background_water
    QuoteBackground.MINIMAL -> R.string.background_minimal
}

/** 텍스트 공유 / 이미지 카드 공유 시트 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareQuoteSheet(quote: Quote, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val initialBackground = LocalQuoteAppearance.current.background
    var background by remember { mutableStateOf(initialBackground) }
    var ratio by remember { mutableStateOf(ShareRatio.SQUARE) }
    var sharing by remember { mutableStateOf(false) }
    val graphicsLayer = rememberGraphicsLayer()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.share_title), style = MaterialTheme.typography.titleLarge)

            ShareCard(
                quote = quote,
                background = background,
                modifier = Modifier
                    .fillMaxWidth(ratio.widthFraction)
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(ratio.aspect)
                    .drawWithContent {
                        // 화면에 보이는 미리보기를 그대로 이미지로 저장하기 위해 레이어에 기록한다.
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShareRatio.entries.forEach { option ->
                    FilterChip(
                        selected = ratio == option,
                        onClick = { ratio = option },
                        label = { Text(stringResource(option.labelRes)) },
                    )
                }
            }

            BackgroundPicker(selected = background, onSelect = { background = it })

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { QuoteSharer.shareText(context, quote) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.share_as_text))
                }
                Button(
                    onClick = {
                        if (sharing) return@Button
                        sharing = true
                        scope.launch {
                            try {
                                QuoteSharer.shareImage(context, graphicsLayer.toImageBitmap(), quote.id)
                            } finally {
                                sharing = false
                            }
                        }
                    },
                    enabled = !sharing,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.share_as_image))
                }
            }
        }
    }
}

/** 공유용 이미지 카드: 머리말 · 말씀 · 출처 · 앱 이름 */
@Composable
private fun ShareCard(quote: Quote, background: QuoteBackground, modifier: Modifier = Modifier) {
    val style = background.style()
    val textStyle = when {
        quote.text.length <= 40 -> MaterialTheme.typography.headlineSmall
        quote.text.length <= 90 -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.titleMedium
    }.copy(fontFamily = FontFamily.Serif)

    QuoteBackgroundBox(
        background = background,
        style = style,
        modifier = modifier.clip(MaterialTheme.shapes.medium),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.share_card_header),
                style = MaterialTheme.typography.labelLarge,
                color = style.contentSecondary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = quote.text,
                style = textStyle,
                color = style.content,
                textAlign = TextAlign.Center,
            )
            if (quote.attribution.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = quote.attribution,
                    style = MaterialTheme.typography.bodySmall,
                    color = style.contentSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.labelMedium,
                color = style.contentSecondary,
            )
        }
    }
}

/** 배경 테마 선택(공유 시트·설정에서 사용) */
@Composable
fun BackgroundPicker(
    selected: QuoteBackground,
    onSelect: (QuoteBackground) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(QuoteBackground.entries, key = { it.name }) { option ->
            val name = stringResource(option.labelRes())
            val isSelected = option == selected
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                QuoteBackgroundBox(
                    background = option,
                    style = option.style(),
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(
                            BorderStroke(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            CircleShape,
                        )
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) })
                        .semantics { contentDescription = name },
                ) {}
                Text(name, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
