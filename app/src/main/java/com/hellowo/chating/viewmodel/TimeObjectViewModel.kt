package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class TimeObjectViewModel : ViewModel() {
    var loading = MutableLiveData<Boolean>()

    init {
    }
}