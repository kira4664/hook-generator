package com.maeumdeungbul.quotes.share

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.Quote
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android 기본 공유 시트로 말씀을 공유한다(카카오톡·문자·Instagram·Facebook·Threads 등).
 * 특정 SNS SDK 에 의존하지 않으며, 이미지 공유도 저장소 권한 없이 FileProvider 로 처리한다.
 */
object QuoteSharer {

    fun shareText(context: Context, quote: Quote) {
        val text = buildString {
            append(quote.text)
            if (quote.attribution.isNotBlank()) append("\n\n— ").append(quote.attribution)
            append("\n\n").append(context.getString(R.string.app_name))
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startChooser(context, send)
    }

    suspend fun shareImage(context: Context, image: ImageBitmap, quoteId: Long) {
        val uri = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() } // 이전에 공유한 이미지 정리
            val file = File(dir, "quote_$quoteId.png")
            val bitmap = image.asAndroidBitmap().let {
                if (it.config == Bitmap.Config.HARDWARE) it.copy(Bitmap.Config.ARGB_8888, false) else it
            }
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startChooser(context, send)
    }

    private fun startChooser(context: Context, intent: Intent) {
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser_title)))
        } catch (e: ActivityNotFoundException) {
            // 공유할 앱이 없는 기기(드묾). 조용히 무시한다.
        }
    }

    private const val SHARE_DIR = "shared"
}
