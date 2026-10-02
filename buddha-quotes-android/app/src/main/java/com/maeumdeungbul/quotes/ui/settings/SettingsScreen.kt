package com.maeumdeungbul.quotes.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.BuildConfig
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.ThemeMode
import com.maeumdeungbul.quotes.playback.MeditationSound
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.ui.components.BackgroundPicker
import com.maeumdeungbul.quotes.ui.components.LoadingState
import com.maeumdeungbul.quotes.ui.components.NotificationDeniedDialog
import com.maeumdeungbul.quotes.ui.components.labelRes
import com.maeumdeungbul.quotes.ui.components.rememberNotificationPermissionRequest
import com.maeumdeungbul.quotes.util.Formatters
import java.time.LocalTime

private enum class SettingsDialog { TIME, FONT, THEME, BACKGROUND, SOUND, RESET }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = appViewModel {
        SettingsViewModel(it.preferencesRepository, it.quoteRepository, it.meditationRepository, it.dailyQuoteScheduler)
    },
) {
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var showPermissionDenied by remember { mutableStateOf(false) }
    val requestNotificationPermission = rememberNotificationPermissionRequest { granted ->
        if (granted) viewModel.setDailyNotificationEnabled(true) else showPermissionDenied = true
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { padding ->
        val current = prefs
        if (current == null) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item(key = "h_notification") { SectionTitle(R.string.settings_notification) }
            item(key = "notification_switch") {
                SwitchRow(
                    title = stringResource(R.string.settings_daily_notification),
                    checked = current.dailyNotificationEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) requestNotificationPermission() else viewModel.setDailyNotificationEnabled(false)
                    },
                )
            }
            item(key = "notification_time") {
                SettingsRow(
                    title = stringResource(R.string.settings_notification_time),
                    value = Formatters.time(current.dailyNotificationTime),
                    enabled = current.dailyNotificationEnabled,
                    onClick = { dialog = SettingsDialog.TIME },
                )
            }

            item(key = "divider1") { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item(key = "h_display") { SectionTitle(R.string.settings_section_display) }
            item(key = "font") {
                SettingsRow(
                    title = stringResource(R.string.settings_font_size),
                    value = stringResource(current.fontScale.labelRes()),
                    onClick = { dialog = SettingsDialog.FONT },
                )
            }
            item(key = "theme") {
                SettingsRow(
                    title = stringResource(R.string.settings_dark_mode),
                    value = stringResource(current.themeMode.labelRes()),
                    onClick = { dialog = SettingsDialog.THEME },
                )
            }
            item(key = "background") {
                SettingsRow(
                    title = stringResource(R.string.settings_quote_background),
                    value = stringResource(current.quoteBackground.labelRes()),
                    onClick = { dialog = SettingsDialog.BACKGROUND },
                )
            }

            item(key = "divider2") { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item(key = "h_meditation") { SectionTitle(R.string.settings_section_meditation) }
            item(key = "sound") {
                SettingsRow(
                    title = stringResource(R.string.settings_meditation_sound),
                    value = MeditationSound.fromId(current.ambientSoundId)?.let { stringResource(it.labelRes) }
                        ?: stringResource(R.string.sound_silent),
                    onClick = { dialog = SettingsDialog.SOUND },
                )
            }
            item(key = "bell") {
                SwitchRow(
                    title = stringResource(R.string.meditation_bell),
                    checked = current.bellEnabled,
                    onCheckedChange = viewModel::setBellEnabled,
                )
            }

            item(key = "divider3") { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item(key = "h_data") { SectionTitle(R.string.settings_section_data) }
            item(key = "reset") {
                SettingsRow(
                    title = stringResource(R.string.settings_reset_data),
                    onClick = { dialog = SettingsDialog.RESET },
                )
            }

            item(key = "divider4") { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item(key = "h_info") { SectionTitle(R.string.settings_section_info) }
            item(key = "privacy") {
                LinkRow(R.string.settings_privacy_policy, BuildConfig.PRIVACY_POLICY_URL) { openUrl(context, it) }
            }
            item(key = "terms") {
                LinkRow(R.string.settings_terms, BuildConfig.TERMS_URL) { openUrl(context, it) }
            }
            item(key = "licenses") {
                SettingsRow(title = stringResource(R.string.settings_licenses), onClick = onOpenLicenses)
            }
            item(key = "contact") {
                LinkRow(R.string.settings_contact, BuildConfig.CONTACT_EMAIL) { sendContactEmail(context, it) }
            }
            item(key = "version") {
                SettingsRow(title = stringResource(R.string.settings_app_version), value = BuildConfig.VERSION_NAME)
            }
        }
    }

    val current = prefs ?: return
    when (dialog) {
        SettingsDialog.TIME -> NotificationTimeDialog(
            initial = current.dailyNotificationTime,
            onConfirm = {
                viewModel.setDailyNotificationTime(it)
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        SettingsDialog.FONT -> ChoiceDialog(
            title = stringResource(R.string.settings_font_size),
            options = FontScale.entries,
            selected = current.fontScale,
            label = { stringResource(it.labelRes()) },
            onSelect = {
                viewModel.setFontScale(it)
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        SettingsDialog.THEME -> ChoiceDialog(
            title = stringResource(R.string.settings_dark_mode),
            options = ThemeMode.entries,
            selected = current.themeMode,
            label = { stringResource(it.labelRes()) },
            onSelect = {
                viewModel.setThemeMode(it)
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        SettingsDialog.BACKGROUND -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.settings_quote_background)) },
            text = {
                BackgroundPicker(selected = current.quoteBackground, onSelect = viewModel::setQuoteBackground)
            },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.action_confirm)) } },
        )
        SettingsDialog.SOUND -> ChoiceDialog(
            title = stringResource(R.string.settings_meditation_sound),
            options = listOf<MeditationSound?>(null) + MeditationSound.available,
            selected = MeditationSound.fromId(current.ambientSoundId),
            label = { sound -> stringResource(sound?.labelRes ?: R.string.sound_silent) },
            onSelect = {
                viewModel.setAmbientSound(it?.id)
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        SettingsDialog.RESET -> ResetDialog(
            onReset = {
                viewModel.reset(it)
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        null -> Unit
    }
    if (showPermissionDenied) NotificationDeniedDialog(onDismiss = { showPermissionDenied = false })
}

@Composable
private fun SectionTitle(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

@Composable
private fun SettingsRow(
    title: String,
    value: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(title, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
        },
        supportingContent = if (value != null) {
            { Text(value) }
        } else {
            null
        },
        modifier = if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier,
    )
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    )
}

/** 외부 링크·연락처가 설정되지 않았다면(빈 값) 임의 주소를 쓰지 않고 "준비 중"으로 표시한다. */
@Composable
private fun LinkRow(@StringRes titleRes: Int, target: String, onOpen: (String) -> Unit) {
    val available = target.isNotBlank()
    SettingsRow(
        title = stringResource(titleRes),
        value = if (available) null else stringResource(R.string.settings_coming_soon),
        enabled = available,
        onClick = { onOpen(target) },
    )
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = option == selected, role = Role.RadioButton, onClick = { onSelect(option) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = null)
                        Text(label(option), modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTimeDialog(initial: LocalTime, onConfirm: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_notification_time)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@StringRes
private fun ResetTarget.labelRes(): Int = when (this) {
    ResetTarget.FAVORITES -> R.string.reset_favorites
    ResetTarget.RECENT -> R.string.reset_recent
    ResetTarget.MEDITATION -> R.string.reset_meditation
    ResetTarget.ALL -> R.string.reset_all
}

@Composable
private fun ResetDialog(onReset: (ResetTarget) -> Unit, onDismiss: () -> Unit) {
    var target by remember { mutableStateOf<ResetTarget?>(null) }
    val pending = target
    if (pending == null) {
        ChoiceDialog(
            title = stringResource(R.string.settings_reset_data),
            options = ResetTarget.entries,
            selected = null,
            label = { stringResource(it.labelRes()) },
            onSelect = { target = it },
            onDismiss = onDismiss,
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(pending.labelRes())) },
            text = { Text(stringResource(R.string.reset_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { onReset(pending) }) { Text(stringResource(R.string.reset_confirm)) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        // 브라우저가 없는 기기
    }
}

private fun sendContactEmail(context: Context, email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.contact_email_subject))
        putExtra(Intent.EXTRA_TEXT, "\n\n—\nApp ${BuildConfig.VERSION_NAME}")
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // 메일 앱이 없는 기기
    }
}
