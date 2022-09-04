package com.example.pair.data

import com.example.pair.AppDatabase
import com.example.pair.Card
import com.example.pair.PairApplication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object AccountRepo {
    private val db = PairApplication.room!!
    private val userDao = (db as AppDatabase).userDao()
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default

    fun getID(): String {
        return PrefRepo.getAccountID()
    }

    fun setID(id: String) {
        PrefRepo.setAccountID(id)
    }

    fun getAccount(): Card {
        val id = getID()
        var card = Card("", "", 0.toLong(), 0)
        runBlocking { launch(dispatcher) {
            card = userDao.getCard(id)
        } }
        return card
    }
}