package com.hellowo.chating.viewmodel

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellowo.chating.AUTH_URL
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
    val targetView = MutableLiveData<View?>()
    val appUser = MutableLiveData<AppUser?>()

    init {
        if(SyncUser.current() == null) {
            val credentials = SyncCredentials.nickname("hellowo", false)
            SyncUser.logInAsync(credentials, AUTH_URL, object: SyncUser.Callback<SyncUser> {
                override fun onError(error: ObjectServerError?) {
                    Log.e("Login error", error.toString())
                }
                override fun onSuccess(result: SyncUser?) {
                    val config = SyncConfiguration.Builder(result, USER_URL).partialRealm().build()
                    Realm.setDefaultConfiguration(config)
                }
            })
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