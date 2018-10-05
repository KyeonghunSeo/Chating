package com.hellowo.journey.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BasicViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Boolean>()

    init {
    }
}