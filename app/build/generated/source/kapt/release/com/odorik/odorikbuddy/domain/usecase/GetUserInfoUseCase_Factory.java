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
public final class GetUserInfoUseCase_Factory implements Factory<GetUserInfoUseCase> {
  private final Provider<OdorikApi> odorikApiProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public GetUserInfoUseCase_Factory(Provider<OdorikApi> odorikApiProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.odorikApiProvider = odorikApiProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public GetUserInfoUseCase get() {
    return newInstance(odorikApiProvider.get(), userRepositoryProvider.get());
  }

  public static GetUserInfoUseCase_Factory create(Provider<OdorikApi> odorikApiProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new GetUserInfoUseCase_Factory(odorikApiProvider, userRepositoryProvider);
  }

  public static GetUserInfoUseCase newInstance(OdorikApi odorikApi, UserRepository userRepository) {
    return new GetUserInfoUseCase(odorikApi, userRepository);
  }
}
