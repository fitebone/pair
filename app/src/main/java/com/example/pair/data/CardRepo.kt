package com.example.pair.data

import android.content.Context
import com.example.pair.AppDatabase
import com.example.pair.Card
import com.example.pair.PairApplication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

object CardRepo {
    private val db = PairApplication.room!!
    private val userDao = (db as AppDatabase).userDao()
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default

    fun getCard(id: String) : Card {
        var card = Card("", "", 0.toLong(), 0)
        runBlocking { launch(dispatcher) {
            card = userDao.getCard(id)
        } }
        return card
    }

    fun getAllCards(notID: String) : List<Card> {
        var cards = listOf<Card>()
        runBlocking { launch(dispatcher) {
            cards = userDao.getAllCards(notID)
        } }
        return cards
    }

    fun doesCardExist(id: String) : Boolean {
        var code = 0
        runBlocking { launch(dispatcher) {
            code = userDao.checkCardExists(id)
        } }
        return code != 0
    }

    fun insertCard(card: Card) {
        runBlocking { launch(dispatcher) {
            userDao.insertCard(card)
        } }
    }

    fun getCardAvatar(id: String, context: Context) : File? {
        var file: File? = null
        if (id == AccountRepo.getID()) {
            val imageFile = File(context.filesDir, "account_image")
            if (imageFile.exists()) {
                file = imageFile
            }
        } else {
            val imageFile = File(context.filesDir, id)
            if (imageFile.exists()) {
                file = imageFile
            }
        }
        return file
    }
}