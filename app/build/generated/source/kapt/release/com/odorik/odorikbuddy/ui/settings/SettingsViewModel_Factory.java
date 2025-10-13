package com.odorik.odorikbuddy.ui.settings;

import com.odorik.odorikbuddy.data.local.ThemeManager;
import com.odorik.odorikbuddy.data.repository.UserRepository;
import com.odorik.odorikbuddy.domain.usecase.GetLineInfoUseCase;
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<GetLinesUseCase> getLinesUseCaseProvider;

  private final Provider<GetLineInfoUseCase> getLineInfoUseCaseProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<ThemeManager> themeManagerProvider;

  public SettingsViewModel_Factory(Provider<GetLinesUseCase> getLinesUseCaseProvider,
      Provider<GetLineInfoUseCase> getLineInfoUseCaseProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<ThemeManager> themeManagerProvider) {
    this.getLinesUseCaseProvider = getLinesUseCaseProvider;
    this.getLineInfoUseCaseProvider = getLineInfoUseCaseProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.themeManagerProvider = themeManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(getLinesUseCaseProvider.get(), getLineInfoUseCaseProvider.get(), userRepositoryProvider.get(), themeManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<GetLinesUseCase> getLinesUseCaseProvider,
      Provider<GetLineInfoUseCase> getLineInfoUseCaseProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<ThemeManager> themeManagerProvider) {
    return new SettingsViewModel_Factory(getLinesUseCaseProvider, getLineInfoUseCaseProvider, userRepositoryProvider, themeManagerProvider);
  }

  public static SettingsViewModel newInstance(GetLinesUseCase getLinesUseCase,
      GetLineInfoUseCase getLineInfoUseCase, UserRepository userRepository,
      ThemeManager themeManager) {
    return new SettingsViewModel(getLinesUseCase, getLineInfoUseCase, userRepository, themeManager);
  }
}
