package com.odorik.odorikbuddy.di;

import com.odorik.odorikbuddy.data.remote.OdorikApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
  @Override
  public OdorikApi get() {
    return provideOdorikApi();
  }

  public static AppModule_ProvideOdorikApiFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OdorikApi provideOdorikApi() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideOdorikApi());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideOdorikApiFactory INSTANCE = new AppModule_ProvideOdorikApiFactory();
  }
}
