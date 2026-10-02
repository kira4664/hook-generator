package com.maeumdeungbul.quotes.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.maeumdeungbul.quotes.BuildConfig
import com.maeumdeungbul.quotes.R

private val generalItems = listOf(
    R.string.settings_notification,
    R.string.settings_font_size,
    R.string.settings_dark_mode,
    R.string.settings_quote_background,
    R.string.settings_meditation_sound,
    R.string.settings_reset_data,
)

// 개인정보처리방침·이용약관·문의 주소는 실제 값이 주입되기 전까지 "준비 중"으로 둔다(임의 URL 금지).
private val infoItems = listOf(
    R.string.settings_privacy_policy,
    R.string.settings_terms,
    R.string.settings_licenses,
    R.string.settings_contact,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item(key = "header_general") { SectionHeader(R.string.settings_section_general) }
            items(generalItems, key = { it }) { SettingsRow(it) }
            item(key = "divider") { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            item(key = "header_info") { SectionHeader(R.string.settings_section_info) }
            items(infoItems, key = { it }) { SettingsRow(it) }
            item(key = "version") {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_app_version)) },
                    trailingContent = { Text(BuildConfig.VERSION_NAME) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

/** Phase 8에서 각 항목에 실제 동작(화면 이동, 다이얼로그)을 연결한다. */
@Composable
private fun SettingsRow(@StringRes titleRes: Int) {
    ListItem(
        headlineContent = { Text(stringResource(titleRes)) },
        supportingContent = { Text(stringResource(R.string.settings_coming_soon)) },
    )
}
