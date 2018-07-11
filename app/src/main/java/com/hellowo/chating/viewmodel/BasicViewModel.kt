package com.hellowo.chating.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BasicViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Boolean>()

    init {
    }
}