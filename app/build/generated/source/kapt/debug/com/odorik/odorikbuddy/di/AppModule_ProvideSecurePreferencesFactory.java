package com.odorik.odorikbuddy.di;

import android.app.Application;
import com.odorik.odorikbuddy.data.local.SecurePreferences;
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
public final class AppModule_ProvideSecurePreferencesFactory implements Factory<SecurePreferences> {
  private final Provider<Application> applicationProvider;

  public AppModule_ProvideSecurePreferencesFactory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public SecurePreferences get() {
    return provideSecurePreferences(applicationProvider.get());
  }

  public static AppModule_ProvideSecurePreferencesFactory create(
      Provider<Application> applicationProvider) {
    return new AppModule_ProvideSecurePreferencesFactory(applicationProvider);
  }

  public static SecurePreferences provideSecurePreferences(Application application) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSecurePreferences(application));
  }
}
