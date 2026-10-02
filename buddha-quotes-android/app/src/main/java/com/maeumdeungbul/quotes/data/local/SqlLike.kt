package com.maeumdeungbul.quotes.data.local

/** SQLite LIKE 검색어 만들기. 사용자가 입력한 %, _, \ 는 와일드카드가 아닌 글자로 취급한다(ESCAPE '\'). */
object SqlLike {
    fun containsPattern(query: String): String {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return "%"
        val escaped = buildString {
            trimmed.forEach { ch ->
                if (ch == '\\' || ch == '%' || ch == '_') append('\\')
                append(ch)
            }
        }
        return "%$escaped%"
    }
}
