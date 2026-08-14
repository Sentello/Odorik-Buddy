# Project-specific ProGuard/R8 rules.
#
# Most libraries ship their own consumer rules inside the artifact and R8
# applies them automatically — verified for this project's versions:
#   Retrofit 2.11 (META-INF/proguard/retrofit2.pro), Hilt/Dagger, Room,
#   WorkManager (keeps worker names + constructors), OkHttp, Compose.
# Only rules R8 cannot derive on its own belong in this file.

#----------------- Release stack traces -----------------
# Keep line numbers so release crashes can be retraced with mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

#----------------- Strip logging -----------------
# Remove all android.util.Log calls (including Log.e) in release builds.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

#----------------- Gson -----------------
# Gson 2.10.1 ships no consumer rules, so its needs are declared here.
# Generic signatures resolve parameterized fields (e.g. List<ConnectedDevice>).
-keepattributes Signature, *Annotation*
# Serialization is driven by @SerializedName; classes may still be renamed.
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep no-arg constructors so Gson instantiates models normally (with field
# defaults) instead of falling back to Unsafe allocation.
-keepclassmembers class com.odorik.odorikbuddy.model.** {
    <init>();
}
-keepclassmembers class com.odorik.odorikbuddy.data.model.** {
    <init>();
}

#----------------- MPAndroidChart -----------------
# animateX/animateY run through ObjectAnimator, which looks up these phase
# accessors reflectively by name — invisible to R8, so keep them explicitly.
-keepclassmembers class com.github.mikephil.charting.animation.ChartAnimator {
    public void setPhaseX(float);
    public float getPhaseX();
    public void setPhaseY(float);
    public float getPhaseY();
}
