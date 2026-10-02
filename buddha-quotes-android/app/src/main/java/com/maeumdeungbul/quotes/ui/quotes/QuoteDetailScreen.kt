package com.maeumdeungbul.quotes.ui.quotes

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.BuildConfig
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.ads.AdPlacement
import com.maeumdeungbul.quotes.ads.AdSlot
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.ReportReason
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.ui.components.LoadingState
import com.maeumdeungbul.quotes.ui.components.QuoteCompactCard
import com.maeumdeungbul.quotes.ui.components.QuoteHeroCard
import com.maeumdeungbul.quotes.ui.components.SectionHeader
import com.maeumdeungbul.quotes.ui.components.ShareQuoteSheet
import com.maeumdeungbul.quotes.ui.components.StatusMessage
import com.maeumdeungbul.quotes.ui.components.labelRes
import com.maeumdeungbul.quotes.ui.theme.LocalQuoteAppearance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quoteId: Long,
    onBack: () -> Unit,
    onQuoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuoteDetailViewModel = appViewModel {
        QuoteDetailViewModel(quoteId, it.quoteRepository, it.preferencesRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showShare by remember { mutableStateOf(false) }
    var showReport by rememberSaveable { mutableStateOf(false) }
    val reportThanks = stringResource(R.string.report_thanks)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val quote = state.quote
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.isError -> StatusMessage(
                title = stringResource(R.string.error_load_quotes),
                actionLabel = stringResource(R.string.action_retry),
                onAction = viewModel::retry,
                modifier = Modifier.padding(padding),
            )
            quote == null -> StatusMessage(
                title = stringResource(R.string.detail_not_found),
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                QuoteHeroCard(
                    quote = quote,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onShare = { showShare = true },
                    minHeight = 360.dp,
                )

                ActionButtons(
                    favorite = quote.favorite,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onShare = { showShare = true },
                )

                FontScaleSelector(
                    selected = LocalQuoteAppearance.current.fontScale,
                    onSelect = viewModel::setFontScale,
                )

                SourceInfo(quote)

                TextButton(onClick = { showReport = true }) {
                    Text(stringResource(R.string.report_button))
                }

                if (state.related.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(stringResource(R.string.detail_related))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.related, key = { it.id }) { related ->
                                QuoteCompactCard(quote = related, onClick = { onQuoteClick(related.id) })
                            }
                        }
                    }
                }

                // 광고 후보 위치: 상세 하단
                AdSlot(AdPlacement.QUOTE_DETAIL_BOTTOM)
            }
        }
    }

    val quote = state.quote
    if (showShare && quote != null) {
        ShareQuoteSheet(quote = quote, onDismiss = { showShare = false })
    }
    if (showReport && quote != null) {
        ReportSheet(
            onDismiss = { showReport = false },
            onSubmit = { reason, memo ->
                showReport = false
                viewModel.report(reason, memo)
                sendReportEmail(context, quote, reason, memo)
                scope.launch { snackbarHostState.showSnackbar(reportThanks) }
            },
        )
    }
}

@Composable
private fun ActionButtons(favorite: Boolean, onToggleFavorite: () -> Unit, onShare: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(if (favorite) R.string.detail_saved else R.string.action_favorite))
        }
        OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.detail_share))
        }
    }
}

@Composable
fun FontScaleSelector(selected: FontScale, onSelect: (FontScale) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.settings_font_size), style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FontScale.entries, key = { it.name }) { scale ->
                FilterChip(
                    selected = scale == selected,
                    onClick = { onSelect(scale) },
                    label = { Text(stringResource(scale.labelRes())) },
                )
            }
        }
    }
}

@Composable
private fun SourceInfo(quote: Quote) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        InfoRow(stringResource(R.string.detail_type), stringResource(quote.quoteType.labelRes()))
        quote.author?.let { InfoRow(stringResource(R.string.detail_author), it) }
        quote.source?.let { source ->
            InfoRow(stringResource(R.string.detail_source), listOfNotNull(source, quote.sourceDetail).joinToString(" "))
        }
        InfoRow(stringResource(R.string.detail_category), quote.category.name)
        InfoRow(
            stringResource(R.string.detail_verification),
            stringResource(if (quote.verified) R.string.detail_verified else R.string.detail_unverified),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.3f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.7f))
    }
}

@StringRes
private fun ReportReason.labelRes(): Int = when (this) {
    ReportReason.WRONG_SOURCE -> R.string.report_reason_source
    ReportReason.WRONG_AUTHOR -> R.string.report_reason_author
    ReportReason.TYPO -> R.string.report_reason_typo
    ReportReason.OTHER -> R.string.report_reason_other
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSheet(onDismiss: () -> Unit, onSubmit: (ReportReason, String) -> Unit) {
    var reason by rememberSaveable { mutableStateOf(ReportReason.WRONG_SOURCE) }
    var memo by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.report_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.report_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(Modifier.selectableGroup()) {
                ReportReason.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = reason == option, role = Role.RadioButton, onClick = { reason = option })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = reason == option, onClick = null)
                        Text(stringResource(option.labelRes()), modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
            OutlinedTextField(
                value = memo,
                onValueChange = { if (it.length <= MEMO_MAX_LENGTH) memo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.report_memo_label)) },
                placeholder = { Text(stringResource(R.string.report_memo_hint)) },
                minLines = 3,
            )
            Button(onClick = { onSubmit(reason, memo.trim()) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.report_submit))
            }
        }
    }
}

private const val MEMO_MAX_LENGTH = 500

/** 문의 이메일이 설정된 경우에만 메일 앱을 연다. 설정되지 않았다면 기기에 기록만 남긴다. */
private fun sendReportEmail(context: Context, quote: Quote, reason: ReportReason, memo: String) {
    val email = BuildConfig.CONTACT_EMAIL
    if (email.isBlank()) return
    val body = buildString {
        appendLine(context.getString(R.string.report_email_body_intro))
        appendLine()
        appendLine("ID: ${quote.id}")
        appendLine(quote.text)
        appendLine("— ${quote.attribution}")
        appendLine()
        appendLine("${context.getString(R.string.report_email_reason)}: ${context.getString(reason.labelRes())}")
        if (memo.isNotBlank()) appendLine("${context.getString(R.string.report_memo_label)}: $memo")
        appendLine()
        appendLine("App ${BuildConfig.VERSION_NAME}")
    }
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.report_email_subject, quote.id))
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // 메일 앱이 없으면 로컬 기록만 남는다.
    }
}
