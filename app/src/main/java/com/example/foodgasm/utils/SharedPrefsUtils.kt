package com.example.foodgasm.utils

import android.content.Context
import androidx.core.content.edit
import com.example.foodgasm.FoodgasmApplication

object SharedPrefsUtils {
    private const val sharedPreferencesName = "Foodgasm"
    const val KEY_EMAIL = "EMAIL"
    const val KEY_PASSWORD = "PASSWORD"

    private val preferences by lazy {
        FoodgasmApplication.instance.getSharedPreferences(
            sharedPreferencesName,
            Context.MODE_PRIVATE
        )
    }

    fun saveString(key: String, value: String) {
        preferences.edit { putString(key, value) }
    }

    fun saveLogin(email: String, password: String) {
        preferences.edit {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
        }
    }

    fun getString(key: String) = preferences.getString(key, "")

}