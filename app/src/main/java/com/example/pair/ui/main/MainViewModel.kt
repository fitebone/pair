package com.example.pair.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pair.Card
import com.google.android.gms.nearby.connection.Payload

class MainViewModel : ViewModel() {

    // Initialize all variables to empty or zero, populate on app start
    val byteDataReceived: MutableLiveData<MutableMap<String, ByteArray>> = MutableLiveData(mutableMapOf())
    val fileDataReceived: MutableLiveData<MutableMap<String, Payload>> = MutableLiveData(mutableMapOf())

    val showNavigation: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    override fun onCleared() {
        super.onCleared()
    }
}