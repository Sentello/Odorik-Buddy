package com.odorik.odorikbuddy.data.repository;

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
public final class HistoryRepository_Factory implements Factory<HistoryRepository> {
  private final Provider<OdorikApi> apiServiceProvider;

  public HistoryRepository_Factory(Provider<OdorikApi> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public HistoryRepository get() {
    return newInstance(apiServiceProvider.get());
  }

  public static HistoryRepository_Factory create(Provider<OdorikApi> apiServiceProvider) {
    return new HistoryRepository_Factory(apiServiceProvider);
  }

  public static HistoryRepository newInstance(OdorikApi apiService) {
    return new HistoryRepository(apiService);
  }
}
