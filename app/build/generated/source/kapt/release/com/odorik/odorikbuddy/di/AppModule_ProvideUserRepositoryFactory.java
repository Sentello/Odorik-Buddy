package com.odorik.odorikbuddy.di;

import android.app.Application;
import com.odorik.odorikbuddy.data.local.SecurePreferences;
import com.odorik.odorikbuddy.data.repository.UserRepository;
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
public final class AppModule_ProvideUserRepositoryFactory implements Factory<UserRepository> {
  private final Provider<Application> applicationProvider;

  private final Provider<SecurePreferences> securePreferencesProvider;

  public AppModule_ProvideUserRepositoryFactory(Provider<Application> applicationProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    this.applicationProvider = applicationProvider;
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public UserRepository get() {
    return provideUserRepository(applicationProvider.get(), securePreferencesProvider.get());
  }

  public static AppModule_ProvideUserRepositoryFactory create(
      Provider<Application> applicationProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    return new AppModule_ProvideUserRepositoryFactory(applicationProvider, securePreferencesProvider);
  }

  public static UserRepository provideUserRepository(Application application,
      SecurePreferences securePreferences) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideUserRepository(application, securePreferences));
  }
}
