package com.example.pair.data

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.example.pair.AppDatabase
import com.example.pair.PairApplication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NukeRepo(fragmentActivity: FragmentActivity) {
    private val db = PairApplication.room!!
    private val userDao = (db as AppDatabase).userDao()
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
    private var sharedPref = fragmentActivity.getPreferences(Context.MODE_PRIVATE)

    fun nukePairs() {
        runBlocking { launch(dispatcher) {
            userDao.nukeSwaps()
        } }
    }

    fun nukeCards() {
        val id = sharedPref.getString("account_id", "")!!
        runBlocking { launch(dispatcher) {
            userDao.nukeCards(id)
        } }
    }

    fun nukeAll() {
        val id = sharedPref.getString("account_id", "")!!
        runBlocking { launch(dispatcher) {
            userDao.nukeCards(id)
            userDao.nukeSwaps()
        } }
    }
}