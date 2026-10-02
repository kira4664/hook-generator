package com.maeumdeungbul.quotes.data

import com.maeumdeungbul.quotes.data.datasource.ContentRules
import com.maeumdeungbul.quotes.data.model.CategoriesFile
import com.maeumdeungbul.quotes.data.model.ContentManifest
import com.maeumdeungbul.quotes.data.model.LicensesFile
import com.maeumdeungbul.quotes.data.model.QuotesFile
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 번들된 콘텐츠 JSON 이 앱에서 파싱되고, 콘텐츠 규칙을 모두 지키는지 검사한다. */
class ContentAssetsTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val dataDir = File("src/main/assets/data")

    private inline fun <reified T> read(name: String): T =
        json.decodeFromString(File(dataDir, name).readText(Charsets.UTF_8))

    @Test
    fun manifestHasPositiveVersion() {
        assertTrue(read<ContentManifest>("manifest.json").contentVersion >= 1)
    }

    @Test
    fun allQuotesFollowContentRules() {
        val categoryIds = read<CategoriesFile>("categories.json").categories.map { it.id }.toSet()
        val quotes = read<QuotesFile>("quotes_ko.json").quotes
        assertTrue(quotes.isNotEmpty())
        assertEquals("duplicate ids", quotes.size, quotes.map { it.id }.toSet().size)
        val problems = quotes.flatMap { quote ->
            ContentRules.violations(quote, categoryIds).map { "#${quote.id}: $it" }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun categoriesAreUnique() {
        val categories = read<CategoriesFile>("categories.json").categories
        assertEquals(categories.size, categories.map { it.id }.toSet().size)
    }

    @Test
    fun licensesParse() {
        assertTrue(read<LicensesFile>("licenses.json").libraries.isNotEmpty())
    }
}
