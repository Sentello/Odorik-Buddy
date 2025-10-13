package com.odorik.odorikbuddy;

import com.odorik.odorikbuddy.data.local.ThemeManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<ThemeManager> themeManagerProvider;

  public MainActivity_MembersInjector(Provider<ThemeManager> themeManagerProvider) {
    this.themeManagerProvider = themeManagerProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<ThemeManager> themeManagerProvider) {
    return new MainActivity_MembersInjector(themeManagerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectThemeManager(instance, themeManagerProvider.get());
  }

  @InjectedFieldSignature("com.odorik.odorikbuddy.MainActivity.themeManager")
  public static void injectThemeManager(MainActivity instance, ThemeManager themeManager) {
    instance.themeManager = themeManager;
  }
}
