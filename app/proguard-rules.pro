# ProGuard / R8 Rules para StudyFlow
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Firebase
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }

# Kotlin Serialization & Data Models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.studyflow.app.data.local.** { *; }
-keep class com.studyflow.app.domain.** { *; }
