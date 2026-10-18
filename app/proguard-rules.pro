# ShrinkIt ProGuard / R8 Rules

# Preserve Hilt & Dagger
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-dontwarn dagger.hilt.**

# Preserve Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Preserve ExifInterface reflection
-keepclassmembers class androidx.exifinterface.media.ExifInterface { *; }

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
