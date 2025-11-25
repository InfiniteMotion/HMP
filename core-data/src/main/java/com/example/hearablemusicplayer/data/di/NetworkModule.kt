package com.example.hearablemusicplayer.data.di

import android.content.Context
import com.example.hearablemusicplayer.data.network.DeepSeekAPI
import com.example.hearablemusicplayer.data.network.DeepSeekAPIWrapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 10L * 1024 * 1024 // 10 MB
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val cacheControl = CacheControl.Builder()
                .maxAge(1, TimeUnit.HOURS) // 缓存1小时
                .build()
            
            val newRequest = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
            
            chain.proceed(newRequest)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        cacheInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor(cacheInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            // 添加重试拦截器
            .addInterceptor(RetryInterceptor(maxRetries = 5))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.deepseek.com")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideDeepSeekAPI(retrofit: Retrofit): DeepSeekAPI {
        return retrofit.create(DeepSeekAPI::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDeepSeekAPIWrapper(api: DeepSeekAPI): DeepSeekAPIWrapper {
        return DeepSeekAPIWrapper(api)
    }
}

/**
 * 重试拦截器 - 支持指数退避策略
 */
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        var response: okhttp3.Response? = null
        var exception: Exception? = null
        var tryCount = 0
        
        while (tryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                
                // 如果响应成功或是客户端错误(4xx),不重试
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }
                
                // 服务器错误或网络错误,准备重试
                response.close()
                
            } catch (e: Exception) {
                exception = e
            }
            
            tryCount++
            if (tryCount < maxRetries) {
                // 指数退避:第1次重试等待1秒,第2次2秒,第3次4秒
                val waitTime = (1000L * (1 shl (tryCount - 1)))
                Thread.sleep(waitTime)
            }
        }
        
        // 重试次数耗尽,返回最后的响应或抛出异常
        return response ?: throw exception ?: Exception("Max retries exceeded")
    }
}
