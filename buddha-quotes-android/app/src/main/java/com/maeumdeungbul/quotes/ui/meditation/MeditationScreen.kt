package com.maeumdeungbul.quotes.ui.meditation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.ui.components.StatusMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.meditation_title)) }) },
    ) { padding ->
        StatusMessage(
            title = stringResource(R.string.meditation_preparing),
            modifier = Modifier.padding(padding),
        )
    }
}
