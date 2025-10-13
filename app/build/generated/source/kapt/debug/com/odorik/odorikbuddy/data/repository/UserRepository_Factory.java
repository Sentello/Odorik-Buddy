package com.odorik.odorikbuddy.data.repository;

import android.app.Application;
import com.odorik.odorikbuddy.data.local.SecurePreferences;
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
public final class UserRepository_Factory implements Factory<UserRepository> {
  private final Provider<Application> applicationProvider;

  private final Provider<SecurePreferences> securePreferencesProvider;

  public UserRepository_Factory(Provider<Application> applicationProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    this.applicationProvider = applicationProvider;
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public UserRepository get() {
    return newInstance(applicationProvider.get(), securePreferencesProvider.get());
  }

  public static UserRepository_Factory create(Provider<Application> applicationProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    return new UserRepository_Factory(applicationProvider, securePreferencesProvider);
  }

  public static UserRepository newInstance(Application application,
      SecurePreferences securePreferences) {
    return new UserRepository(application, securePreferences);
  }
}
