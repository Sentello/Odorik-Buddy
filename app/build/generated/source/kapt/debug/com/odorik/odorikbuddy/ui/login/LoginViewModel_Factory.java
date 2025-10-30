package com.odorik.odorikbuddy.ui.login;

import android.content.Context;
import com.odorik.odorikbuddy.data.repository.UserRepository;
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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

  private final Provider<Context> contextProvider;

  public LoginViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<GetCreditUseCase> getCreditUseCaseProvider, Provider<Context> contextProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.getCreditUseCaseProvider = getCreditUseCaseProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(userRepositoryProvider.get(), getCreditUseCaseProvider.get(), contextProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<GetCreditUseCase> getCreditUseCaseProvider, Provider<Context> contextProvider) {
    return new LoginViewModel_Factory(userRepositoryProvider, getCreditUseCaseProvider, contextProvider);
  }

  public static LoginViewModel newInstance(UserRepository userRepository,
      GetCreditUseCase getCreditUseCase, Context context) {
    return new LoginViewModel(userRepository, getCreditUseCase, context);
  }
}
