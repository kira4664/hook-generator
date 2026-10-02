package com.maeumdeungbul.quotes.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.maeumdeungbul.quotes.MaeumApplication
import com.maeumdeungbul.quotes.di.AppContainer

/** 현재 화면(NavBackStackEntry)에 범위가 지정된 ViewModel 을 AppContainer 의 의존성으로 만든다. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (AppContainer) -> VM): VM {
    val container = (LocalContext.current.applicationContext as MaeumApplication).container
    return viewModel(factory = viewModelFactory { initializer { create(container) } })
}
