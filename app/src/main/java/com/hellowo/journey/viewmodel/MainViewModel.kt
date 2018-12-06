package com.hellowo.journey.viewmodel

import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellowo.journey.*
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.*
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.*

class MainViewModel : ViewModel() {
    val loading = MutableLiveData<Boolean>()
    val targetTimeObject = MutableLiveData<TimeObject?>()
    val targetView = MutableLiveData<View?>()
    val appUser = MutableLiveData<AppUser?>()
    val targetTemplate = MutableLiveData<Template>()
    val templateList = MutableLiveData<List<Template>>()
    val colorTagList = MutableLiveData<List<ColorTag>>()
    val isCalendarSettingOpened = MutableLiveData<Boolean>()
    val targetFolder = MutableLiveData<Folder>()

    var realm: Realm? = null
    private var timeObjectList: RealmResults<TimeObject>? = null

    init {}

    fun init(realm: Realm, syncUser: SyncUser) {
        this.realm = realm
        loadAppUser(syncUser)
        loadTemplate()
    }

    private fun loadAppUser(syncUser: SyncUser) {
        realm?.let {
            l("[loadAppUser]")
            it.where(AppUser::class.java).findAllAsync().addChangeListener { result, _ ->
                if(result.size > 0) {
                    appUser.value = result[0]
                }else {
                    l("[새로운 유저 생성]")
                    realm?.executeTransaction {
                        realm?.createObject(AppUser::class.java, syncUser.identity)
                    }
                }
            }
        }
    }

    private fun loadTemplate() {
        realm?.let {
            l("[loadTemplate]")
            it.where(Template::class.java).sort("order", Sort.ASCENDING).findAllAsync()
                    .addChangeListener { result, _ ->
                        l("[]" + result.isValid)
                        templateList.value = result
                    }
        }
        /*
        realm.executeTransaction {
            TimeObject.Type.values().forEachIndexed { index, t ->
                realm.createObject(Template::class.java, index).run {
                    title = App.context.getString(t.titleId)
                    type = t.ordinal
                    order = index
                }
            }
        }*/
    }

    override fun onCleared() {
        super.onCleared()
        realm?.removeAllChangeListeners()
        realm?.close()
        realm = null
    }

    fun saveProfileImage(resource: Bitmap) {
        realm?.executeTransaction {
            appUser.value?.profileImg = bitmapToByteArray(resource)
            appUser.value = appUser.value
        }
    }

    fun clearTargetTimeObject() {
        targetTimeObject.value = null
    }

    fun makeNewTimeObject(type: Int) {
        MainActivity.instance?.getCalendarView()?.let {
            targetTimeObject.value = TimeObjectManager.makeNewTimeObject(
                    getCalendarTime0(it.targetCal), getCalendarTime23(it.targetCal)).apply {
                this.type = type
            }
        }
    }

    fun makeNewTimeObject() {
        MainActivity.instance?.getCalendarView()?.let {
            makeNewTimeObject(getCalendarTime0(it.targetCal), getCalendarTime23(it.targetCal))
        }
    }

    fun makeNewTimeObject(startTime: Long, endTime: Long) {
        MainActivity.instance?.getCalendarView()?.let {
            targetTimeObject.value = makeTimeObjectByTatgetTemplate(startTime, endTime)
        }
    }

    fun makeTimeObjectByTatgetTemplate(startTime: Long, endTime: Long) =
            TimeObjectManager.makeNewTimeObject(startTime, endTime).apply {
                targetTemplate.value?.let {
                    type = it.type
                    style = it.style
                    colorKey = it.colorKey
                    fontColor = it.fontColor
                    inCalendar = it.inCalendar
                    folder = targetFolder.value
                    tags.addAll(it.tags)
                }
            }

    fun setTargetTimeObjectById(id: String?) {
        id?.let {
            val realm = Realm.getDefaultInstance()
            targetTimeObject.value = realm.where(TimeObject::class.java)
                    .equalTo("id", it)
                    .findFirst()
            realm.close()
        }
    }

    fun saveDirectByTemplate() {
        MainActivity.instance?.getCalendarView()?.let {
            TimeObjectManager.save(makeTimeObjectByTatgetTemplate(
                    getCalendarTime0(it.targetCal), getCalendarTime23(it.targetCal)))
        }
    }
}