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


#----------------- Hilt / Dagger -----------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-keepattributes *Annotation*
-dontwarn dagger.hilt.internal.**.

#----------------- Jetpack Compose -----------------
# Jetpack Compose Basic Rules
-keepclasseswithmembers class androidx.compose.** { *; }
-keepclassmembers class * { @androidx.compose.runtime.Composable *; }

#----------------- App Specific Rules -----------------
# Keep these classes and their members because they are used by Hilt and reflection.
-keep class com.odorik.odorikbuddy.data.local.SecurePreferences { *; }
-keep class com.odorik.odorikbuddy.data.repository.UserRepository { *; }
-keep class com.odorik.odorikbuddy.domain.usecase.** { *; }

# Optional: Ignore warnings for dependencies
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*