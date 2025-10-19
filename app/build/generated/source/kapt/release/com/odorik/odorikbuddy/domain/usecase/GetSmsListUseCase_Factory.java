package com.odorik.odorikbuddy.domain.usecase;

import com.odorik.odorikbuddy.data.remote.OdorikApi;
import com.odorik.odorikbuddy.data.repository.UserRepository;
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
public final class GetSmsListUseCase_Factory implements Factory<GetSmsListUseCase> {
  private final Provider<OdorikApi> odorikApiProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public GetSmsListUseCase_Factory(Provider<OdorikApi> odorikApiProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.odorikApiProvider = odorikApiProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public GetSmsListUseCase get() {
    return newInstance(odorikApiProvider.get(), userRepositoryProvider.get());
  }

  public static GetSmsListUseCase_Factory create(Provider<OdorikApi> odorikApiProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new GetSmsListUseCase_Factory(odorikApiProvider, userRepositoryProvider);
  }

  public static GetSmsListUseCase newInstance(OdorikApi odorikApi, UserRepository userRepository) {
    return new GetSmsListUseCase(odorikApi, userRepository);
  }
}
