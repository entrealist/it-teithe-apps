package gr.cpaleop.teithe_apps.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gr.cpaleop.core.data.AuthenticationRepositoryImpl
import gr.cpaleop.core.data.remote.AuthenticationApi
import gr.cpaleop.core.dispatchers.DefaultDispatcher
import gr.cpaleop.core.dispatchers.IODispatcher
import gr.cpaleop.core.dispatchers.MainDispatcher
import gr.cpaleop.core.domain.behavior.Authentication
import gr.cpaleop.core.domain.behavior.DownloadFolder
import gr.cpaleop.core.domain.repositories.AuthenticationRepository
import gr.cpaleop.network.interceptors.ConnectionInterceptor
import gr.cpaleop.network.interceptors.RefreshTokenInterceptor
import gr.cpaleop.network.interceptors.TokenInterceptor
import gr.cpaleop.teithe_apps.BuildConfig
import gr.cpaleop.teithe_apps.data.RemoteAnnouncementConverterFactory
import gr.cpaleop.teithe_apps.data.RemoteAnnouncementMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { RemoteAnnouncementMapper() }
    single { provideRetrofit(get(), get(), get()) }
    single(named<Authentication>()) {
        provideAuthenticationRetrofit(
            get(named<Authentication>()),
            get()
        )
    }
    single { provideOkHttpClient(get(), get(), get(), get()) }
    single(named<Authentication>()) { provideAuthenticationOkHttpClient(get(), get(), get()) }
    single { provideGson() }
    single<AuthenticationRepository> {
        AuthenticationRepositoryImpl(
            get(),
            get()
        )
    }
    single<CoroutineDispatcher>(named<MainDispatcher>()) { Dispatchers.Main }
    single(named<DefaultDispatcher>()) { Dispatchers.Default }
    single(named<IODispatcher>()) { Dispatchers.IO }
    single { provideAuthenticationApi(get(named<Authentication>())) }
    single(named<DownloadFolder>()) { provideDownloadFolder(get()) }
    single(named<Authority>()) { provideAuthority(get()) }
}

@Authority
private fun provideAuthority(applicationContext: Context): String {
    return applicationContext.packageName + ".fileprovider"
}

@DownloadFolder
private fun provideDownloadFolder(applicationContext: Context): File {
    return applicationContext.externalCacheDir ?: applicationContext.cacheDir
}

private fun provideAuthenticationApi(retrofit: Retrofit): AuthenticationApi {
    return retrofit.create(AuthenticationApi::class.java)
}

private fun provideRetrofit(
    okHttpClient: OkHttpClient,
    gson: Gson,
    remoteAnnouncementMapper: RemoteAnnouncementMapper
): Retrofit {
    val gsonConverterFactory = GsonConverterFactory.create(gson)
    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            RemoteAnnouncementConverterFactory.create(
                remoteAnnouncementMapper,
                gson,
                gsonConverterFactory
            )
        )
        .build()
}

// The order of interceptors matters.
private fun provideOkHttpClient(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    connectionInterceptor: ConnectionInterceptor,
    tokenInterceptor: TokenInterceptor,
    refreshTokenInterceptor: RefreshTokenInterceptor
): OkHttpClient {
    val dispatcher = Dispatcher()
    dispatcher.maxRequests = 1

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(connectionInterceptor)
        .addInterceptor(refreshTokenInterceptor)
        .addInterceptor(tokenInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .dispatcher(dispatcher)

    if (BuildConfig.DEBUG) {
        okHttpClient.addInterceptor(httpLoggingInterceptor)
    }

    return okHttpClient.build()
}

private fun provideAuthenticationRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.LOGIN_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

private fun provideAuthenticationOkHttpClient(
    httpLoggingInterceptor: HttpLoggingInterceptor,
    connectionInterceptor: ConnectionInterceptor,
    tokenInterceptor: TokenInterceptor
): OkHttpClient {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(connectionInterceptor)
        .addInterceptor(tokenInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)

    if (BuildConfig.DEBUG) {
        okHttpClient.addInterceptor(httpLoggingInterceptor)
    }

    return okHttpClient.build()
}

private fun provideGson(): Gson {
    return GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()
}