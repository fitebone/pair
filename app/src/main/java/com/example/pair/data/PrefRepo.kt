package com.example.pair.data

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity

// !! Only other repositories should use this one !!
// NOTE: apply immediately async replaces SP in memory and lazily updates disk
//       commit immediately sync replaces SP in disk
object PrefRepo {
    // Core SharedPreferences instances
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    // SharedPreferences strings as vars
    private const val accountID = "account_id"
    private const val accountURI = "account_image_uri"

    fun init(activity: FragmentActivity) {
        pref = activity.getPreferences(Context.MODE_PRIVATE)
        editor = pref.edit()
    }

    fun getAccountID(): String {
        return pref.getString(accountID, "")!!
    }

    fun setAccountID(id: String) {
        editor.putString(accountID, id).apply()
    }

    fun getAccountAvatarURI(): String {
        return pref.getString(accountURI, "")!!
    }

    fun setAccountAvatarURI(uri: String) {
        editor.putString(accountURI, uri).apply()
    }

}