package com.maeumdeungbul.quotes.data.model

import kotlinx.serialization.Serializable

// assets/data/*.json 의 형식. 형식 설명은 docs/DESIGN.md 6장 참고.

@Serializable
data class ContentManifest(
    /** 콘텐츠를 수정할 때마다 1씩 올린다. 앱이 저장된 버전보다 크면 다시 시드한다. */
    val contentVersion: Int,
)

@Serializable
data class QuotesFile(
    val quotes: List<QuoteJson>,
)

@Serializable
data class QuoteJson(
    val id: Long,
    val text: String,
    val author: String? = null,
    val source: String? = null,
    val sourceDetail: String? = null,
    val category: String,
    val quoteType: String = "UNKNOWN",
    val verified: Boolean = false,
    val translator: String? = null,
    val licenseNote: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class CategoriesFile(
    val categories: List<CategoryJson>,
)

@Serializable
data class CategoryJson(
    val id: String,
    val name: String,
    val sortOrder: Int,
)

@Serializable
data class LicensesFile(
    val libraries: List<LicenseJson>,
)

@Serializable
data class LicenseJson(
    val name: String,
    val owner: String,
    val license: String,
    val url: String,
)
