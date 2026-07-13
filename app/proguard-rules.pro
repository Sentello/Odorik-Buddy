# Add project specific ProGuard rules here.
# You can find more details about customizing this file at
# https://developer.android.com/studio/build/shrink-code


# Remove all logging calls (Log.d, Log.e, etc.) in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

#----------------- Retrofit and Gson -----------------
# Preserve generics and reflection for Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit core classes
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# For Kotlin coroutines in Retrofit (if using suspend functions)
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep your API models for Gson deserialization
-keepclassmembers class com.odorik.odorikbuddy.data.model.** {
    <fields>;
    <init>();
}
-keepclassmembers class com.odorik.odorikbuddy.model.** {
    <fields>;
    <init>();
}
-keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }


#----------------- Hilt / Dagger / ViewModels -----------------
# Keep Hilt/Dagger annotations and core classes
-keepattributes *Annotation*
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-dontwarn dagger.hilt.internal.**.

# Keep all ViewModels to prevent R8 stripping during dependency injection
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Hilt's ViewModel factory and generated components
-keep class dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keep class dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$* { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }

#----------------- Jetpack Compose -----------------
-keepclassmembers class * { @androidx.compose.runtime.Composable *; }

#----------------- App Specific Rules -----------------
# Keep these classes and their members because they are used by reflection/Hilt
-keep class com.odorik.odorikbuddy.data.local.SecurePreferences { *; }
-keep class com.odorik.odorikbuddy.data.repository.UserRepository { *; }
-keep class com.odorik.odorikbuddy.domain.usecase.** { *; }

# WorkManager
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class androidx.work.** { *; }
-keep class androidx.startup.** { *; }

# Models - Keep them fully to avoid serialization issues
-keep class com.odorik.odorikbuddy.data.model.** { *; }
-keep class com.odorik.odorikbuddy.model.** { *; }

# Keep UpdateWorkManager and its worker
-keep class com.odorik.odorikbuddy.worker.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase
-keep class com.odorik.odorikbuddy.data.local.OdorikDatabase { *; }
-keep class com.odorik.odorikbuddy.data.local.**Dao { *; }
-dontwarn androidx.room.paging.**

# Retrofit APIs - Keep interfaces explicitly
-keep interface com.odorik.odorikbuddy.data.api.** { *; }
-keep interface com.odorik.odorikbuddy.data.remote.** { *; }

# Optional: Ignore warnings for dependencies
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*