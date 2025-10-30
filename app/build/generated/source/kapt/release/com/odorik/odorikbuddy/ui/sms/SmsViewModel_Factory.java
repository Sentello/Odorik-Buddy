package com.odorik.odorikbuddy.ui.sms;

import android.content.Context;
import com.odorik.odorikbuddy.data.local.SecurePreferences;
import com.odorik.odorikbuddy.data.remote.OdorikApi;
import com.odorik.odorikbuddy.data.repository.UserRepository;
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase;
import com.odorik.odorikbuddy.domain.usecase.SendSmsUseCase;
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
public final class SmsViewModel_Factory implements Factory<SmsViewModel> {
  private final Provider<SendSmsUseCase> sendSmsUseCaseProvider;

  private final Provider<GetLinesUseCase> getLinesUseCaseProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SecurePreferences> securePreferencesProvider;

  private final Provider<OdorikApi> apiProvider;

  private final Provider<Context> contextProvider;

  public SmsViewModel_Factory(Provider<SendSmsUseCase> sendSmsUseCaseProvider,
      Provider<GetLinesUseCase> getLinesUseCaseProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SecurePreferences> securePreferencesProvider, Provider<OdorikApi> apiProvider,
      Provider<Context> contextProvider) {
    this.sendSmsUseCaseProvider = sendSmsUseCaseProvider;
    this.getLinesUseCaseProvider = getLinesUseCaseProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.securePreferencesProvider = securePreferencesProvider;
    this.apiProvider = apiProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SmsViewModel get() {
    return newInstance(sendSmsUseCaseProvider.get(), getLinesUseCaseProvider.get(), userRepositoryProvider.get(), securePreferencesProvider.get(), apiProvider.get(), contextProvider.get());
  }

  public static SmsViewModel_Factory create(Provider<SendSmsUseCase> sendSmsUseCaseProvider,
      Provider<GetLinesUseCase> getLinesUseCaseProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SecurePreferences> securePreferencesProvider, Provider<OdorikApi> apiProvider,
      Provider<Context> contextProvider) {
    return new SmsViewModel_Factory(sendSmsUseCaseProvider, getLinesUseCaseProvider, userRepositoryProvider, securePreferencesProvider, apiProvider, contextProvider);
  }

  public static SmsViewModel newInstance(SendSmsUseCase sendSmsUseCase,
      GetLinesUseCase getLinesUseCase, UserRepository userRepository,
      SecurePreferences securePreferences, OdorikApi api, Context context) {
    return new SmsViewModel(sendSmsUseCase, getLinesUseCase, userRepository, securePreferences, api, context);
  }
}
