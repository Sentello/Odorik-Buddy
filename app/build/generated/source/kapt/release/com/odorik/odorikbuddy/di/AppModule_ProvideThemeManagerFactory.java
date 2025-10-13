package com.odorik.odorikbuddy.di;

import android.app.Application;
import com.odorik.odorikbuddy.data.local.ThemeManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideThemeManagerFactory implements Factory<ThemeManager> {
  private final Provider<Application> applicationProvider;

  public AppModule_ProvideThemeManagerFactory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public ThemeManager get() {
    return provideThemeManager(applicationProvider.get());
  }

  public static AppModule_ProvideThemeManagerFactory create(
      Provider<Application> applicationProvider) {
    return new AppModule_ProvideThemeManagerFactory(applicationProvider);
  }

  public static ThemeManager provideThemeManager(Application application) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideThemeManager(application));
  }
}
