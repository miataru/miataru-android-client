# Consumer rules to keep Moshi/Kotlin reflection metadata for model classes
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn okio.**
