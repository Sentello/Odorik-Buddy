package com.odorik.odorikbuddy.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class LocaleManager_Factory implements Factory<LocaleManager> {
  private final Provider<Context> contextProvider;

  public LocaleManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LocaleManager get() {
    return newInstance(contextProvider.get());
  }

  public static LocaleManager_Factory create(Provider<Context> contextProvider) {
    return new LocaleManager_Factory(contextProvider);
  }

  public static LocaleManager newInstance(Context context) {
    return new LocaleManager(context);
  }
}
