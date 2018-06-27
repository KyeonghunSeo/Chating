package com.hellowo.chating.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class BasicViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Boolean>()

    init {
    }
}