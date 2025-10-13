package com.odorik.odorikbuddy.ui.dashboard;

import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase;
import com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<GetCreditUseCase> getCreditUseCaseProvider;

  private final Provider<GetUserInfoUseCase> getUserInfoUseCaseProvider;

  public DashboardViewModel_Factory(Provider<GetCreditUseCase> getCreditUseCaseProvider,
      Provider<GetUserInfoUseCase> getUserInfoUseCaseProvider) {
    this.getCreditUseCaseProvider = getCreditUseCaseProvider;
    this.getUserInfoUseCaseProvider = getUserInfoUseCaseProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(getCreditUseCaseProvider.get(), getUserInfoUseCaseProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<GetCreditUseCase> getCreditUseCaseProvider,
      Provider<GetUserInfoUseCase> getUserInfoUseCaseProvider) {
    return new DashboardViewModel_Factory(getCreditUseCaseProvider, getUserInfoUseCaseProvider);
  }

  public static DashboardViewModel newInstance(GetCreditUseCase getCreditUseCase,
      GetUserInfoUseCase getUserInfoUseCase) {
    return new DashboardViewModel(getCreditUseCase, getUserInfoUseCase);
  }
}
