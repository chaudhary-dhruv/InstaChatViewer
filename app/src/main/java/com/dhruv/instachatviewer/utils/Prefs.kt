package com.dhruv.instachatviewer.utils

import android.content.Context
import android.content.SharedPreferences

object Prefs {

    private const val NAME = "instachatviewer_prefs"
    private const val KEY_IMPORTED = "has_imported"
    private const val KEY_OWNER_NAME = "owner_name"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun setImported(context: Context, imported: Boolean) {
        prefs(context).edit().putBoolean(KEY_IMPORTED, imported).apply()
    }

    fun hasImported(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IMPORTED, false)

    fun clearImportState(context: Context) {
        prefs(context).edit().remove(KEY_IMPORTED).remove(KEY_OWNER_NAME).apply()
    }

    fun setOwnerName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_OWNER_NAME, name).apply()
    }

    fun getOwnerName(context: Context): String? =
        prefs(context).getString(KEY_OWNER_NAME, null)
}
