package com.maeumdeungbul.quotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.maeumdeungbul.quotes.ui.MaeumAppRoot
import com.maeumdeungbul.quotes.ui.theme.MaeumTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // super.onCreate 이전에 호출해야 한다. 최초 사용자 여부(온보딩) 판별은 Phase 3에서
        // setKeepOnScreenCondition 으로 DataStore 값을 읽을 때까지 스플래시를 유지하도록 연결한다.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaeumTheme {
                MaeumAppRoot()
            }
        }
    }
}
