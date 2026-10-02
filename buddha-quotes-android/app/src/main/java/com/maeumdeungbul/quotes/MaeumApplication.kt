package com.maeumdeungbul.quotes

import android.app.Application
import com.maeumdeungbul.quotes.di.AppContainer
import com.maeumdeungbul.quotes.notification.NotificationChannels
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MaeumApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationChannels.createAll(this)
        container.applicationScope.launch {
            container.dailyQuoteScheduler.sync(container.preferencesRepository.userPreferences.first())
        }
    }
}
