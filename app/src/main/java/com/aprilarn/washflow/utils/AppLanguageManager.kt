package com.aprilarn.washflow.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(
    val languageTag: String,
    val nativeLabel: String,
    val speechLocaleTag: String
) {
    ENGLISH("en", "English", "en-US"),
    INDONESIAN("id", "Bahasa Indonesia", "id-ID");

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            return when (tag?.lowercase(Locale.US)) {
                "id", "in", "id-id" -> INDONESIAN
                else -> ENGLISH
            }
        }
    }
}

object AppLanguageManager {
    private const val PREFS_NAME = "WashFlowPrefs"
    private const val KEY_APP_LANGUAGE = "APP_LANGUAGE"

    fun getLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTag = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.ENGLISH.languageTag)
        return AppLanguage.fromTag(savedTag)
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_APP_LANGUAGE, language.languageTag).apply()
        Locale.setDefault(Locale.forLanguageTag(language.languageTag))
    }

    fun wrapContext(base: Context): Context {
        val language = getLanguage(base)
        val locale = Locale.forLanguageTag(language.languageTag)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    fun getSpeechRecognitionLocaleTag(context: Context): String {
        return getLanguage(context).speechLocaleTag
    }
}
