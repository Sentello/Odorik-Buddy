package com.odorik.odorikbuddy.ui.balance;

import com.odorik.odorikbuddy.data.local.SecureStorage;
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
public final class BalanceViewModel_Factory implements Factory<BalanceViewModel> {
  private final Provider<OdorikApi> odorikApiProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public BalanceViewModel_Factory(Provider<OdorikApi> odorikApiProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.odorikApiProvider = odorikApiProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public BalanceViewModel get() {
    return newInstance(odorikApiProvider.get(), secureStorageProvider.get());
  }

  public static BalanceViewModel_Factory create(Provider<OdorikApi> odorikApiProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new BalanceViewModel_Factory(odorikApiProvider, secureStorageProvider);
  }

  public static BalanceViewModel newInstance(OdorikApi odorikApi, SecureStorage secureStorage) {
    return new BalanceViewModel(odorikApi, secureStorage);
  }
}
