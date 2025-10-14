package com.odorik.odorikbuddy.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u000b\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0018\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\nH\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/odorik/odorikbuddy/di/AppModule;", "", "()V", "provideLocaleManager", "Lcom/odorik/odorikbuddy/data/local/LocaleManager;", "application", "Landroid/app/Application;", "provideOdorikApi", "Lcom/odorik/odorikbuddy/data/remote/OdorikApi;", "securePreferences", "Lcom/odorik/odorikbuddy/data/local/SecurePreferences;", "provideSecurePreferences", "provideSecureStorage", "Lcom/odorik/odorikbuddy/data/local/SecureStorage;", "provideThemeManager", "Lcom/odorik/odorikbuddy/data/local/ThemeManager;", "provideUserRepository", "Lcom/odorik/odorikbuddy/data/repository/UserRepository;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.odorik.odorikbuddy.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.remote.OdorikApi provideOdorikApi(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.repository.UserRepository provideUserRepository(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.local.SecureStorage provideSecureStorage(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.local.ThemeManager provideThemeManager(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.local.LocaleManager provideLocaleManager(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.odorik.odorikbuddy.data.local.SecurePreferences provideSecurePreferences(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        return null;
    }
}