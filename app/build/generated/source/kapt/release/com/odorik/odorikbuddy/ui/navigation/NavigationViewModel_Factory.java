package com.odorik.odorikbuddy.ui.navigation;

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
public final class NavigationViewModel_Factory implements Factory<NavigationViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  public NavigationViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public NavigationViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static NavigationViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new NavigationViewModel_Factory(userRepositoryProvider);
  }

  public static NavigationViewModel newInstance(UserRepository userRepository) {
    return new NavigationViewModel(userRepository);
  }
}
