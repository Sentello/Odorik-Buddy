package com.odorik.odorikbuddy.ui.history;

import com.odorik.odorikbuddy.data.local.SecurePreferences;
import com.odorik.odorikbuddy.data.repository.HistoryRepository;
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
public final class HistoryViewModel_Factory implements Factory<HistoryViewModel> {
  private final Provider<HistoryRepository> repositoryProvider;

  private final Provider<SecurePreferences> securePreferencesProvider;

  public HistoryViewModel_Factory(Provider<HistoryRepository> repositoryProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    this.repositoryProvider = repositoryProvider;
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public HistoryViewModel get() {
    return newInstance(repositoryProvider.get(), securePreferencesProvider.get());
  }

  public static HistoryViewModel_Factory create(Provider<HistoryRepository> repositoryProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    return new HistoryViewModel_Factory(repositoryProvider, securePreferencesProvider);
  }

  public static HistoryViewModel newInstance(HistoryRepository repository,
      SecurePreferences securePreferences) {
    return new HistoryViewModel(repository, securePreferences);
  }
}
