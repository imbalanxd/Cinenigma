package com.imbaland.common.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureSharedPreferences {
    private lateinit var preferenceFile: String
    private var secureSharedPreferences: SharedPreferences? = null
    fun initialize(context: Context, preferenceFile: String = context.packageName) {
        SecureSharedPreferences.preferenceFile = preferenceFile
        //EncryptedSharedPreferences not supported lower than 5.1
        secureSharedPreferences = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            EncryptedSharedPreferences.create(
                context,
                preferenceFile,
                context.masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } else {
            context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE)
        }
    }

    private val Context.masterKey: MasterKey
        get() {
            return MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        }

    fun write(key: String, value: Any?) {
        secureSharedPreferences?.edit()?.let{
            with(it) {
                when(value) {
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value.toString())
                    else -> putString(key, value.toString())
                }
                it.apply()
            }
        }
    }

    fun readString(key: String): String? {
        return secureSharedPreferences?.getString(key, null)
    }

    fun readLong(key: String): Long? {
        return secureSharedPreferences?.getLong(key, 0L)
    }
}