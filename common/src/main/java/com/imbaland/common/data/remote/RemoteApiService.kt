package com.imbaland.common.data.remote

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.imbaland.common.data.local.SecureSharedPreferences
import com.imbaland.common.data.util.MoshiUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

abstract class RemoteApiService<Api: Any>() {
    abstract val baseUrl: String
    private val headerValues: HashMap<String, String> = HashMap()
    protected lateinit var apiService: Api

    fun launchService(context: Context) {
        configureHeaders(context)
        apiService = createApiService(getInstance(context))
    }

    protected open fun configureHeaders(context: Context) {
        addHeader("X-Device-Name", String.format("%s %s", encodeForHeaderValue(Build.MANUFACTURER), encodeForHeaderValue(Build.MODEL)))
        addHeader("X-Device-Identifier", Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
        addHeader("X-Device-Version", "Android ${Build.VERSION.RELEASE}")
        addHeader("Accept-Language", Locale.getDefault().language)
        addHeader("Accept", "application/json")
    }

    protected fun addHeader(key: String, value: String) {
        headerValues[key] = value
    }

    protected fun removeHeader(key: String) {
        headerValues.remove(key)
    }

    private fun encodeForHeaderValue(rawValue: String): String {
        return Uri.encode(rawValue, " ")
    }

    fun setAccessToken(token: String?, signingSpecial: String?) {
        SecureSharedPreferences.write("access_token", token)
    }

    abstract fun createApiService(retrofit: Retrofit): Api

    private fun getInstance(context: Context): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
//            .addInterceptor(ErrorInterceptor { code, body ->
//                runBlocking {
//                    errorHandler.handleApiError(code, body)
//                }
//            })
            .addInterceptor(APIHeaderInterceptor(context))

        val debugLoggingInterceptor = HttpLoggingInterceptor()
        debugLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(debugLoggingInterceptor)

        return Retrofit.Builder().baseUrl(baseUrl)
            .addParsingFactories()
            .client(okHttpClient.build())
            .build()
    }

    open fun Retrofit.Builder.addParsingFactories(): Retrofit.Builder {
        addConverterFactory(MoshiUtils.genericMoshiConverterFactory)
//        addCallAdapterFactory(ResultCallAdapterFactory())
        return this
    }

    inner class APIHeaderInterceptor(val context: Context) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            with(chain.request().newBuilder()) {
                headerValues.keys.forEach { key ->
                    headerValues[key]?.let { value ->
                        addHeader(key, value)
                    }
                }
                return chain.proceed(this.build())
            }
        }
    }
}