# Navigation Compose 타입 안전 Route(@Serializable)는 직렬화 이름이 유지되어야 한다.
-keep @kotlinx.serialization.Serializable class com.maeumdeungbul.quotes.navigation.** { *; }

# assets JSON DTO (kotlinx.serialization). 라이브러리 기본 consumer rule 외에 필드명을 보존한다.
-keep @kotlinx.serialization.Serializable class com.maeumdeungbul.quotes.data.model.** { *; }
