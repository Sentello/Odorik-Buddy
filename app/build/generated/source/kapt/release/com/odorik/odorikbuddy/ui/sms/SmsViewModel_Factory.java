package com.odorik.odorikbuddy.ui.sms;

import com.odorik.odorikbuddy.data.local.SecurePreferences;
import com.odorik.odorikbuddy.data.remote.OdorikApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SmsViewModel_Factory implements Factory<SmsViewModel> {
  private final Provider<OdorikApi> apiProvider;

  private final Provider<SecurePreferences> securePreferencesProvider;

  public SmsViewModel_Factory(Provider<OdorikApi> apiProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    this.apiProvider = apiProvider;
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public SmsViewModel get() {
    return newInstance(apiProvider.get(), securePreferencesProvider.get());
  }

  public static SmsViewModel_Factory create(Provider<OdorikApi> apiProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    return new SmsViewModel_Factory(apiProvider, securePreferencesProvider);
  }

  public static SmsViewModel newInstance(OdorikApi api, SecurePreferences securePreferences) {
    return new SmsViewModel(api, securePreferences);
  }
}
