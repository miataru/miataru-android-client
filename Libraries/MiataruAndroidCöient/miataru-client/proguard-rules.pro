# ProGuard rules for Miataru client (library build)
# Keep Moshi generated adapters and Json-annotated classes
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn okio.**
