package com.odorik.odorikbuddy.di

import android.app.Application
import com.odorik.odorikbuddy.BuildConfig  
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton
import com.odorik.odorikbuddy.data.local.SecureStorage 
import com.odorik.odorikbuddy.data.local.ThemeManager 
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import okhttp3.Credentials
import retrofit2.converter.gson.GsonConverterFactory

import com.odorik.odorikbuddy.data.local.LanguagePreferences

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOdorikApi(securePreferences: SecurePreferences): OdorikApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val user = securePreferences.getUser()
                val password = securePreferences.getPassword()
                val requestBuilder = original.newBuilder()
                if (user != null && password != null) {
                    val credentials = Credentials.basic(user, password)
                    requestBuilder.header("Authorization", credentials)
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://www.odorik.cz/api/v1/") 
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) 
            .addConverterFactory(GsonConverterFactory.create()) 
            .build()
            .create(OdorikApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(application: Application, securePreferences: com.odorik.odorikbuddy.data.local.SecurePreferences): UserRepository {
        return UserRepository(application, securePreferences)
    }

    @Provides
    @Singleton
    fun provideSecureStorage(application: Application): SecureStorage {
        return SecureStorage(application)
    }

    @Provides
    @Singleton
    fun provideThemeManager(application: Application): ThemeManager {
        return ThemeManager(application)
    }

    @Provides
    @Singleton
    fun provideLocaleManager(application: Application): LocaleManager {
        return LocaleManager(application)
    }

    @Provides
    @Singleton
    fun provideSecurePreferences(application: Application): com.odorik.odorikbuddy.data.local.SecurePreferences {
        return com.odorik.odorikbuddy.data.local.SecurePreferences(application)
    }
}