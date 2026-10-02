package com.maeumdeungbul.quotes.data.datasource

import android.content.res.AssetManager
import com.maeumdeungbul.quotes.data.model.CategoriesFile
import com.maeumdeungbul.quotes.data.model.ContentManifest
import com.maeumdeungbul.quotes.data.model.LicensesFile
import com.maeumdeungbul.quotes.data.model.QuotesFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** 앱에 포함된 콘텐츠(assets/data)를 읽는다. 오프라인에서도 동작하는 초기 데이터의 원본이다. */
class AssetContentDataSource(
    private val assets: AssetManager,
    private val json: Json,
) {
    suspend fun loadManifest(): ContentManifest = read(MANIFEST)
    suspend fun loadQuotes(): QuotesFile = read(QUOTES)
    suspend fun loadCategories(): CategoriesFile = read(CATEGORIES)
    suspend fun loadLicenses(): LicensesFile = read(LICENSES)

    private suspend inline fun <reified T> read(path: String): T = withContext(Dispatchers.IO) {
        val text = assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        json.decodeFromString<T>(text)
    }

    companion object {
        const val MANIFEST = "data/manifest.json"
        const val QUOTES = "data/quotes_ko.json"
        const val CATEGORIES = "data/categories.json"
        const val LICENSES = "data/licenses.json"
    }
}
