package com.odorik.odorikbuddy.di;

import android.app.Application;
import com.odorik.odorikbuddy.data.local.SecureStorage;
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
public final class AppModule_ProvideSecureStorageFactory implements Factory<SecureStorage> {
  private final Provider<Application> applicationProvider;

  public AppModule_ProvideSecureStorageFactory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public SecureStorage get() {
    return provideSecureStorage(applicationProvider.get());
  }

  public static AppModule_ProvideSecureStorageFactory create(
      Provider<Application> applicationProvider) {
    return new AppModule_ProvideSecureStorageFactory(applicationProvider);
  }

  public static SecureStorage provideSecureStorage(Application application) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSecureStorage(application));
  }
}
