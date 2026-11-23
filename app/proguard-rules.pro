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
# R8 full mode drops Signature unless a -keep rule matches the class. TypeToken
# reads that signature reflectively (`object : TypeToken<List<ActiveCall>>() {}`).
# Without these rules the release build crashes on launch once that class loads:
# "TypeToken must be created with a type argument".
-if class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
# Serialization is driven by @SerializedName; classes may still be renamed.
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep model constructors. Only Gson creates these objects, so without a kept
# constructor R8 full mode decides the class is never instantiated: it deletes its
# fields and constructors and turns every cast to it into a throw. Release-only
# symptoms from 1.6.0: ClassCastException in LiveCallsViewModel as soon as a call
# is in progress, "Vyskytla se neznámá chyba" on Speed dials. Models give every
# field a default (Kotlin then generates the no-arg constructor Gson prefers) and a
# @SerializedName; <init>(...) also covers a model that forgets the defaults.
-keepclassmembers class com.odorik.odorikbuddy.model.** {
    <init>(...);
}
-keepclassmembers class com.odorik.odorikbuddy.data.model.** {
    <init>(...);
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

# Settings backup (data/backup): Gson builds these through their no-arg constructors.
# Field names are kept by the @SerializedName rule above.
-keepclassmembers class com.odorik.odorikbuddy.data.backup.** {
    <init>();
}
