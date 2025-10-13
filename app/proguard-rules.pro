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

# Keep your API models for Gson deserialization (adjust package if needed)
-keep class com.odorik.odorikbuddy.data.model.** { *; }

# Optional: Ignore warnings for dependencies
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*