package com.odorik.odorikbuddy.ui.login;

import com.odorik.odorikbuddy.data.repository.UserRepository;
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<GetCreditUseCase> getCreditUseCaseProvider;

  public LoginViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<GetCreditUseCase> getCreditUseCaseProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.getCreditUseCaseProvider = getCreditUseCaseProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(userRepositoryProvider.get(), getCreditUseCaseProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<GetCreditUseCase> getCreditUseCaseProvider) {
    return new LoginViewModel_Factory(userRepositoryProvider, getCreditUseCaseProvider);
  }

  public static LoginViewModel newInstance(UserRepository userRepository,
      GetCreditUseCase getCreditUseCase) {
    return new LoginViewModel(userRepository, getCreditUseCase);
  }
}
