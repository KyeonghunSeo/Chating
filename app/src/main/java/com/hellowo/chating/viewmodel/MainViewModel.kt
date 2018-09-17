package com.hellowo.chating.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellowo.chating.*
import com.hellowo.chating.R
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.model.Template
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.model.AppUser
import com.hellowo.chating.ui.activity.MainActivity
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    val loading = MutableLiveData<Boolean>()
    val targetTimeObject = MutableLiveData<TimeObject?>()
    val targetView = MutableLiveData<View?>()
    val appUser = MutableLiveData<AppUser?>()
    val templateList = MutableLiveData<List<Template>>()

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
        loadTemplate()
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

    private fun loadTemplate() {
        val templates = realm.where(Template::class.java).sort("order", Sort.ASCENDING).findAll()
        if(templates.isEmpty()) {
            realm.executeTransaction {
                val note = realm.createObject(Template::class.java, 0)
                note.title = App.context.getString(R.string.note)
                note.type = TimeObject.Type.NOTE.ordinal
                note.color = Color.parseColor("#434957")
                note.order = 0

                val event = realm.createObject(Template::class.java, 1)
                event.title = App.context.getString(R.string.event)
                event.type = TimeObject.Type.EVENT.ordinal
                event.color = Color.parseColor("#3fa9f5")
                event.order = 1

                val task = realm.createObject(Template::class.java, 2)
                task.title = App.context.getString(R.string.task)
                task.type = TimeObject.Type.TASK.ordinal
                task.color = Color.parseColor("#7ea0c4")
                task.order = 2
            }
            templateList.value = realm.where(Template::class.java).sort("order", Sort.ASCENDING).findAll()
        }else {
            templateList.value = templates
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

    fun clearTargetTimeObject() {
        targetTimeObject.value = null
    }

    fun makeNewTimeObject(item: Template) {
        MainActivity.instance?.getCalendarView()?.let {
            targetTimeObject.value = TimeObjectManager.makeNewTimeObject(
                    getCalendarTime0(it.selectedCal), getCalendarTime23(it.selectedCal)).apply {
                type = item.type
                color = item.color
            }
        }
    }
}