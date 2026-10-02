package com.maeumdeungbul.quotes.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteType
import com.maeumdeungbul.quotes.ui.theme.LocalQuoteAppearance
import com.maeumdeungbul.quotes.ui.theme.quote
import com.maeumdeungbul.quotes.ui.theme.scaled
import com.maeumdeungbul.quotes.ui.theme.style

@StringRes
fun QuoteType.labelRes(): Int = when (this) {
    QuoteType.BUDDHA -> R.string.quote_type_buddha
    QuoteType.SCRIPTURE -> R.string.quote_type_scripture
    QuoteType.MASTER -> R.string.quote_type_master
    QuoteType.WISDOM -> R.string.quote_type_wisdom
    QuoteType.UNKNOWN -> R.string.quote_type_unknown
}

/** 말씀 유형 배지. 검수 전 말씀에는 "출처 확인 중"을 함께 붙인다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuoteTypeBadges(
    quote: Quote,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Badge(stringResource(quote.quoteType.labelRes()), containerColor, contentColor)
        if (!quote.verified && quote.quoteType != QuoteType.UNKNOWN) {
            Badge(stringResource(R.string.quote_type_unknown), containerColor, contentColor)
        }
    }
}

@Composable
private fun Badge(text: String, containerColor: Color, contentColor: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = containerColor, contentColor = contentColor) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun FavoriteIconButton(
    favorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    IconToggleButton(checked = favorite, onCheckedChange = { onToggle() }, modifier = modifier) {
        Icon(
            imageVector = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(if (favorite) R.string.action_unfavorite else R.string.action_favorite),
            tint = tint,
        )
    }
}

@Composable
fun ShareIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share), tint = tint)
    }
}

/** 배경 그림 위에 크게 보여 주는 말씀 카드(홈의 오늘의 말씀 등). */
@Composable
fun QuoteHeroCard(
    quote: Quote,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    onClick: (() -> Unit)? = null,
    minHeight: Dp = 300.dp,
) {
    val appearance = LocalQuoteAppearance.current
    val style = appearance.background.style()
    val clickLabel = stringResource(R.string.action_open_detail)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(
                if (onClick != null) Modifier.clickable(onClickLabel = clickLabel, onClick = onClick) else Modifier,
            ),
        shape = MaterialTheme.shapes.large,
    ) {
        QuoteBackgroundBox(
            background = appearance.background,
            style = style,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = style.contentSecondary,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.quote.scaled(appearance.fontScale.multiplier),
                    color = style.content,
                )
                if (quote.attribution.isNotBlank()) {
                    Text(
                        text = "— ${quote.attribution}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = style.contentSecondary,
                    )
                }
                QuoteTypeBadges(
                    quote = quote,
                    containerColor = style.content.copy(alpha = 0.12f),
                    contentColor = style.content,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FavoriteIconButton(quote.favorite, onToggleFavorite, tint = style.content)
                    ShareIconButton(onShare, tint = style.content)
                }
            }
        }
    }
}

/** 목록용 말씀 카드. */
@Composable
fun QuoteListItem(
    quote: Quote,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickLabel = stringResource(R.string.action_open_detail)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClickLabel = clickLabel, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 4.dp)) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 12.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (quote.attribution.isNotBlank()) {
                        Text(
                            text = quote.attribution,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    QuoteTypeBadges(quote)
                }
                FavoriteIconButton(quote.favorite, onToggleFavorite)
                ShareIconButton(onShare)
            }
        }
    }
}

/** 가로 목록용 작은 카드(추천·최근 본 말씀). */
@Composable
fun QuoteCompactCard(
    quote: Quote,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appearance = LocalQuoteAppearance.current
    val style = appearance.background.style()
    val clickLabel = stringResource(R.string.action_open_detail)
    Card(
        modifier = modifier
            .width(240.dp)
            .height(168.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClickLabel = clickLabel, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        QuoteBackgroundBox(appearance.background, style, Modifier.fillMaxSize()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = style.content,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = quote.category.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = style.contentSecondary,
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
