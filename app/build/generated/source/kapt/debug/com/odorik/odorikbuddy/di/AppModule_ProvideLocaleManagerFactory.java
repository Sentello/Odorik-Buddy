package com.odorik.odorikbuddy.di;

import android.app.Application;
import com.odorik.odorikbuddy.data.local.LocaleManager;
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
public final class AppModule_ProvideLocaleManagerFactory implements Factory<LocaleManager> {
  private final Provider<Application> applicationProvider;

  public AppModule_ProvideLocaleManagerFactory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public LocaleManager get() {
    return provideLocaleManager(applicationProvider.get());
  }

  public static AppModule_ProvideLocaleManagerFactory create(
      Provider<Application> applicationProvider) {
    return new AppModule_ProvideLocaleManagerFactory(applicationProvider);
  }

  public static LocaleManager provideLocaleManager(Application application) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideLocaleManager(application));
  }
}
