package com.odorik.odorikbuddy.data.local;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0003J\u0006\u0010\t\u001a\u00020\u0006J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0006J\u0016\u0010\r\u001a\u00020\u00032\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u0006R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/odorik/odorikbuddy/data/local/LocaleManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "KEY_LANGUAGE", "", "createLocaleContext", "base", "getPreferredLanguage", "setPreferredLanguage", "", "lang", "updateLocale", "app_debug"})
public final class LocaleManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_LANGUAGE = "language";
    
    @javax.inject.Inject()
    public LocaleManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPreferredLanguage() {
        return null;
    }
    
    public final void setPreferredLanguage(@org.jetbrains.annotations.NotNull()
    java.lang.String lang) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.Context createLocaleContext(@org.jetbrains.annotations.NotNull()
    android.content.Context base) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.Context updateLocale(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String lang) {
        return null;
    }
}