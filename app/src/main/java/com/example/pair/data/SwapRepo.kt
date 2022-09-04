package com.example.pair.data

import com.example.pair.AppDatabase
import com.example.pair.PairApplication
import com.example.pair.Swap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object SwapRepo {
    private val db = PairApplication.room!!
    private val userDao = (db as AppDatabase).userDao()
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default

    fun getAllPairs() : List<Swap> {
        var pairs = listOf<Swap>()
        runBlocking { launch(dispatcher) {
            pairs = userDao.getAllSwaps()
        } }
        return pairs
    }

    fun insertPair(swap: Swap) {
        runBlocking { launch(dispatcher) {
            userDao.insertSwap(swap)
        } }
    }

    fun getLatestSwapWithPeer(id: String) : Swap {
        var swap = Swap("", "", "", 0.toLong())
        runBlocking { launch(dispatcher) {
            swap = userDao.getLatestSwap(id)
        } }
        return swap
    }

    fun doesSwapExistWithPeer(id: String) : Boolean {
        var code = 0
        runBlocking { launch(dispatcher) {
            code = userDao.doesSwapExistWithPeer(id)
        } }
        return code != 0
    }
}