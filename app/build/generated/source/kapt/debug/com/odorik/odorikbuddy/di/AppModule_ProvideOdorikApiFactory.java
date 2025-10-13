package com.odorik.odorikbuddy.di;

import com.odorik.odorikbuddy.data.local.SecurePreferences;
import com.odorik.odorikbuddy.data.remote.OdorikApi;
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
public final class AppModule_ProvideOdorikApiFactory implements Factory<OdorikApi> {
  private final Provider<SecurePreferences> securePreferencesProvider;

  public AppModule_ProvideOdorikApiFactory(Provider<SecurePreferences> securePreferencesProvider) {
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public OdorikApi get() {
    return provideOdorikApi(securePreferencesProvider.get());
  }

  public static AppModule_ProvideOdorikApiFactory create(
      Provider<SecurePreferences> securePreferencesProvider) {
    return new AppModule_ProvideOdorikApiFactory(securePreferencesProvider);
  }

  public static OdorikApi provideOdorikApi(SecurePreferences securePreferences) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideOdorikApi(securePreferences));
  }
}
