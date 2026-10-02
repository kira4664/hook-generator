package com.maeumdeungbul.quotes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.maeumdeungbul.quotes.ads.LocalAdsProvider
import com.maeumdeungbul.quotes.domain.model.ThemeMode
import com.maeumdeungbul.quotes.notification.DailyQuoteNotifier
import com.maeumdeungbul.quotes.ui.MaeumAppRoot
import com.maeumdeungbul.quotes.ui.MainUiState
import com.maeumdeungbul.quotes.ui.MainViewModel
import com.maeumdeungbul.quotes.ui.onboarding.OnboardingScreen
import com.maeumdeungbul.quotes.ui.theme.LocalQuoteAppearance
import com.maeumdeungbul.quotes.ui.theme.MaeumTheme
import com.maeumdeungbul.quotes.ui.theme.QuoteAppearance
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val container by lazy { (application as MaeumApplication).container }

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer { MainViewModel(container.preferencesRepository, container.dailyQuoteScheduler) }
        }
    }

    /** 알림에서 전달된 말씀 id. 화면이 처리하면 null 로 되돌린다. */
    private val pendingQuoteId = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // 설정(최초 실행 여부·테마)을 읽을 때까지 스플래시를 유지한다. → 온보딩 또는 홈
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
        enableEdgeToEdge()
        if (savedInstanceState == null) handleIntent(intent)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val prefs = (state as? MainUiState.Ready)?.preferences ?: return@setContent
            val darkTheme = when (prefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // 앱 테마를 시스템과 다르게 설정한 경우에도 상태바 아이콘 색이 맞도록 한다.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
                )
                onDispose {}
            }
            val pendingQuote by pendingQuoteId.collectAsStateWithLifecycle()

            MaeumTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(
                    LocalQuoteAppearance provides QuoteAppearance(prefs.fontScale, prefs.quoteBackground),
                    LocalAdsProvider provides container.adsProvider,
                ) {
                    if (prefs.onboardingDone) {
                        MaeumAppRoot(
                            pendingQuoteId = pendingQuote,
                            onPendingQuoteHandled = { pendingQuoteId.value = null },
                        )
                    } else {
                        OnboardingScreen(onFinish = viewModel::completeOnboarding)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val quoteId = intent?.getLongExtra(DailyQuoteNotifier.EXTRA_QUOTE_ID, -1L) ?: -1L
        if (quoteId > 0) {
            pendingQuoteId.value = quoteId
            intent?.removeExtra(DailyQuoteNotifier.EXTRA_QUOTE_ID)
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
