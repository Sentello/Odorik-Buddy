package com.odorik.odorikbuddy.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.odorik.odorikbuddy.BuildConfig  // Add this import for BuildConfig
import com.odorik.odorikbuddy.data.local.HistoryDao
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
import com.odorik.odorikbuddy.data.local.ThemeManager // Added import for ThemeManager
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import retrofit2.converter.gson.GsonConverterFactory

import com.google.gson.GsonBuilder
import com.google.gson.FieldNamingPolicy

import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.data.local.OdorikDatabase
import androidx.room.Room

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOdorikApi(): OdorikApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://www.odorik.cz/api/v1/") // Changed base URL
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // Added for plain text
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create())) // For JSON conversion with snake_case to camelCase conversion
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

    @Provides
    @Singleton
    fun provideOdorikDatabase(app: Application): OdorikDatabase {
        return Room.databaseBuilder(
            app,
            OdorikDatabase::class.java,
            "odorik_database"
        ).addMigrations(OdorikDatabase.MIGRATION_1_2).build()
    }

    @Provides
    fun provideHistoryDao(db: OdorikDatabase): HistoryDao = db.historyDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(app: Application): SharedPreferences = app.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
}