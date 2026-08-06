package com.touristapp.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/** Name of the secondary [FirebaseApp] that carries the owner session. */
private const val ADMIN_APP_NAME = "admin"
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings { })
        }
    }

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Secondary [FirebaseApp] sharing the default app's project config but keeping
     * its own auth state. Reuses the existing instance if one is already
     * registered, since [FirebaseApp.initializeApp] throws when the name is taken
     * and Hilt may rebuild the graph after a process restart.
     */
    @Provides
    @Singleton
    @AdminScope
    fun provideAdminApp(@ApplicationContext context: Context): FirebaseApp =
        runCatching { FirebaseApp.getInstance(ADMIN_APP_NAME) }.getOrElse {
            FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, ADMIN_APP_NAME)
        }

    /**
     * Starts signed out on every process start. Firebase persists auth state per
     * app name, so without this an owner who walked away mid-session would still
     * be authenticated after a reboot.
     */
    @Provides
    @Singleton
    @AdminScope
    fun provideAdminAuth(@AdminScope app: FirebaseApp): FirebaseAuth =
        FirebaseAuth.getInstance(app).apply { signOut() }

    @Provides
    @Singleton
    @AdminScope
    fun provideAdminFirestore(@AdminScope app: FirebaseApp): FirebaseFirestore =
        FirebaseFirestore.getInstance(app)

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(Android)

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}
