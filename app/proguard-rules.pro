# حماية نماذج البيانات الخاصة بـ Gson (DTOs)
-keep class com.example.data.remote.dto.** { *; }
-keepclassmembers class com.example.data.remote.dto.** {
    <fields>;
}

# حماية كيانات Room
-keep class com.example.data.local.entity.** { *; }
-keepclassmembers class com.example.data.local.entity.** {
    <fields>;
}

# الاحتفاظ بمعلومات تتبع الأخطاء
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature

# قواعد Gson العامة
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# الحفاظ على قيم الـ enum المستخدمة في التسلسل
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# حماية Retrofit و OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# حماية Glide
-keep class com.bumptech.glide.** { *; }

# حماية PDF Viewer
-keep class com.github.barteksc.pdfviewer.** { *; }
