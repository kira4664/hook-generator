package com.maeumdeungbul.quotes.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.maeumdeungbul.quotes.MaeumApplication
import com.maeumdeungbul.quotes.MainActivity
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.UserPreferences
import com.maeumdeungbul.quotes.domain.usecase.DailySchedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

object NotificationChannels {
    const val DAILY_QUOTE = "daily_quote"

    fun createAll(context: Context) {
        val channel = NotificationChannelCompat.Builder(DAILY_QUOTE, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_daily))
            .setDescription(context.getString(R.string.notification_channel_daily_description))
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}

/**
 * 매일 말씀 알림 예약. 정확한 알람 권한(SCHEDULE_EXACT_ALARM) 없이 WorkManager 로 처리하므로
 * 기기 절전 상태에 따라 수 분 늦게 도착할 수 있다.
 * 다음 알림은 Worker 가 실행될 때마다 다시 예약한다(하루 주기 오차 누적 방지).
 */
class DailyQuoteScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun schedule(time: LocalTime, replaceExisting: Boolean = true) {
        val delay = DailySchedule.delayUntilNext(ZonedDateTime.now(), time)
        val request = OneTimeWorkRequestBuilder<DailyQuoteWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            WORK_NAME,
            if (replaceExisting) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    /** 앱 시작 시 설정과 예약 상태를 맞춘다. */
    fun sync(preferences: UserPreferences) {
        if (preferences.dailyNotificationEnabled) {
            schedule(preferences.dailyNotificationTime, replaceExisting = false)
        } else {
            cancel()
        }
    }

    private companion object {
        const val WORK_NAME = "daily_quote_notification"
    }
}

class DailyQuoteWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as MaeumApplication).container
        val preferences = container.preferencesRepository
        val settings = preferences.userPreferences.first()
        if (!settings.dailyNotificationEnabled) return Result.success()

        val today = LocalDate.now()
        // 같은 날 두 번 알리지 않는다(재예약 과정에서 중복 실행되더라도 안전).
        if (preferences.getLastNotifiedDate() != today) {
            val quote = runCatching { container.quoteRepository.getDailyQuote(today) }.getOrNull()
            if (quote != null && DailyQuoteNotifier.show(applicationContext, quote)) {
                preferences.setLastNotifiedDate(today)
            }
        }
        // 마지막 단계: 다음 날 알림 예약(현재 작업을 대체한다).
        container.dailyQuoteScheduler.schedule(settings.dailyNotificationTime, replaceExisting = true)
        return Result.success()
    }
}

object DailyQuoteNotifier {
    const val EXTRA_QUOTE_ID = "com.maeumdeungbul.quotes.extra.QUOTE_ID"
    private const val NOTIFICATION_ID = 1001

    fun canPostNotifications(context: Context): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun show(context: Context, quote: Quote): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_QUOTE_ID, quote.id)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val body = if (quote.attribution.isBlank()) quote.text else "${quote.text}\n— ${quote.attribution}"
        val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_QUOTE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_daily_title))
            .setContentText(quote.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
