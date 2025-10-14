package com.odorik.odorikbuddy;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0018H\u0014J\u000e\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\u001bR\u001b\u0010\u0003\u001a\u00020\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001e\u0010\u000f\u001a\u00020\u00108\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014\u00a8\u0006\u001c"}, d2 = {"Lcom/odorik/odorikbuddy/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "callViewModel", "Lcom/odorik/odorikbuddy/ui/calls/CallViewModel;", "getCallViewModel", "()Lcom/odorik/odorikbuddy/ui/calls/CallViewModel;", "callViewModel$delegate", "Lkotlin/Lazy;", "localeManager", "Lcom/odorik/odorikbuddy/data/local/LocaleManager;", "getLocaleManager", "()Lcom/odorik/odorikbuddy/data/local/LocaleManager;", "setLocaleManager", "(Lcom/odorik/odorikbuddy/data/local/LocaleManager;)V", "themeManager", "Lcom/odorik/odorikbuddy/data/local/ThemeManager;", "getThemeManager", "()Lcom/odorik/odorikbuddy/data/local/ThemeManager;", "setThemeManager", "(Lcom/odorik/odorikbuddy/data/local/ThemeManager;)V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "updateLocale", "lang", "", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @javax.inject.Inject()
    public com.odorik.odorikbuddy.data.local.ThemeManager themeManager;
    @javax.inject.Inject()
    public com.odorik.odorikbuddy.data.local.LocaleManager localeManager;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy callViewModel$delegate = null;
    
    public MainActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.local.ThemeManager getThemeManager() {
        return null;
    }
    
    public final void setThemeManager(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.ThemeManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.local.LocaleManager getLocaleManager() {
        return null;
    }
    
    public final void setLocaleManager(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.LocaleManager p0) {
    }
    
    private final com.odorik.odorikbuddy.ui.calls.CallViewModel getCallViewModel() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    public final void updateLocale(@org.jetbrains.annotations.NotNull()
    java.lang.String lang) {
    }
}