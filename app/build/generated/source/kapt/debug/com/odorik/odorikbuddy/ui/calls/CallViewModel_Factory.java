package com.odorik.odorikbuddy.ui.calls;

import com.odorik.odorikbuddy.data.local.SecurePreferences;
import com.odorik.odorikbuddy.domain.usecase.CallUseCase;
import com.odorik.odorikbuddy.domain.usecase.GetCallListUseCase;
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
public final class CallViewModel_Factory implements Factory<CallViewModel> {
  private final Provider<GetCallListUseCase> getCallListUseCaseProvider;

  private final Provider<GetLinesUseCase> getLinesUseCaseProvider;

  private final Provider<CallUseCase> callUseCaseProvider;

  private final Provider<SecurePreferences> securePreferencesProvider;

  public CallViewModel_Factory(Provider<GetCallListUseCase> getCallListUseCaseProvider,
      Provider<GetLinesUseCase> getLinesUseCaseProvider, Provider<CallUseCase> callUseCaseProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    this.getCallListUseCaseProvider = getCallListUseCaseProvider;
    this.getLinesUseCaseProvider = getLinesUseCaseProvider;
    this.callUseCaseProvider = callUseCaseProvider;
    this.securePreferencesProvider = securePreferencesProvider;
  }

  @Override
  public CallViewModel get() {
    return newInstance(getCallListUseCaseProvider.get(), getLinesUseCaseProvider.get(), callUseCaseProvider.get(), securePreferencesProvider.get());
  }

  public static CallViewModel_Factory create(
      Provider<GetCallListUseCase> getCallListUseCaseProvider,
      Provider<GetLinesUseCase> getLinesUseCaseProvider, Provider<CallUseCase> callUseCaseProvider,
      Provider<SecurePreferences> securePreferencesProvider) {
    return new CallViewModel_Factory(getCallListUseCaseProvider, getLinesUseCaseProvider, callUseCaseProvider, securePreferencesProvider);
  }

  public static CallViewModel newInstance(GetCallListUseCase getCallListUseCase,
      GetLinesUseCase getLinesUseCase, CallUseCase callUseCase,
      SecurePreferences securePreferences) {
    return new CallViewModel(getCallListUseCase, getLinesUseCase, callUseCase, securePreferences);
  }
}
