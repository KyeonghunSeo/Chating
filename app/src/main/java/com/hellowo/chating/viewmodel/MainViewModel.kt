package com.hellowo.chating.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellowo.chating.USER_URL
import com.hellowo.chating.bitmapToByteArray
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.makeViewToBitmap
import com.hellowo.chating.model.AppUser
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    val loading = MutableLiveData<Boolean>()
    val targetTimeObject = MutableLiveData<TimeObject?>()
    val appUser = MutableLiveData<AppUser?>()

    init {
        SyncUser.current()?.let {
            val config = SyncConfiguration.Builder(it, USER_URL).partialRealm().build()
            Realm.setDefaultConfiguration(config)
        }
        loadAppUser()
    }

    private fun loadAppUser() {
        val user = realm.where(AppUser::class.java).findFirst()
        if(user == null) {
            realm.executeTransaction {
                appUser.value = realm.createObject(AppUser::class.java, UUID.randomUUID().toString())
            }
        }else {
            appUser.value = user
        }
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    fun saveProfileImage(resource: Bitmap) {
        realm.executeTransaction {
            appUser.value?.profileImg = bitmapToByteArray(resource)
            appUser.value = appUser.value
        }
    }
}