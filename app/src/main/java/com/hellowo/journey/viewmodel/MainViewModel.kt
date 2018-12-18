package com.hellowo.journey.viewmodel

import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellowo.journey.*
import com.hellowo.journey.R
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.*
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val loading = MutableLiveData<Boolean>()
    val targetTimeObject = MutableLiveData<TimeObject?>()
    val targetView = MutableLiveData<View?>()
    val appUser = MutableLiveData<AppUser?>()
    val targetTemplate = MutableLiveData<Template>()
    val templateList = MutableLiveData<RealmResults<Template>>()
    val colorTagList = MutableLiveData<List<ColorTag>>()
    val isCalendarSettingOpened = MutableLiveData<Boolean>()
    val targetFolder = MutableLiveData<Folder>()
    val folderList = MutableLiveData<RealmResults<Folder>>()
    val targetTime = MutableLiveData<Long>()

    var realm: Realm? = null
    private var timeObjectList: RealmResults<TimeObject>? = null

    init {
        targetTime.value = System.currentTimeMillis()
    }

    fun init(realm: Realm, syncUser: SyncUser) {
        this.realm = realm
        loadAppUser(syncUser)
        loadTemplate()
        loadFolder()
    }

    private fun loadAppUser(syncUser: SyncUser) {
        realm?.let { it ->
            it.where(AppUser::class.java).findAllAsync().addChangeListener { result, _ ->
                if(result.size > 0) {
                    appUser.value = result[0]
                }else {
                    l("[새로운 유저 생성]")
                    realm?.executeTransaction {
                        it.createObject(AppUser::class.java, syncUser.identity)
                    }
                }
            }
        }
    }

    private fun loadTemplate() {
        realm?.let { it ->
            templateList.value = it.where(Template::class.java).sort("order", Sort.ASCENDING).findAll()
            templateList.value?.addChangeListener { result, _ ->
                templateList.value = result
            }
            return@let
        }
    }

    private fun loadFolder() {
        realm?.let { it ->
            folderList.value = it.where(Folder::class.java).sort("order", Sort.ASCENDING).findAll()
            folderList.value?.addChangeListener { result, _ ->
                folderList.value = result
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        realm?.removeAllChangeListeners()
        realm?.close()
        realm = null
    }

    fun saveProfileImage(profileImgUrl: String) {
        realm?.executeTransaction {
            appUser.value?.profileImgUrl = profileImgUrl
            appUser.value = appUser.value
        }
    }

    fun clearTargetTimeObject() {
        targetTimeObject.value = null
    }

    fun makeNewTimeObject(type: Int) {
        val timeObject = makeTimeObjectByTatgetTemplate(getCalendarTime0(targetTime.value!!), getCalendarTime23(targetTime.value!!))
        timeObject.type = type
        targetTimeObject.value = timeObject
    }

    fun makeNewTimeObject() {
        targetTimeObject.value = makeTimeObjectByTatgetTemplate(
                getCalendarTime0(targetTime.value!!), getCalendarTime23(targetTime.value!!))
    }

    fun makeNewTimeObject(startTime: Long, endTime: Long) {
        targetTimeObject.value = makeTimeObjectByTatgetTemplate(startTime, endTime)
    }

    fun makeTimeObjectByTatgetTemplate(startTime: Long, endTime: Long) =
            TimeObjectManager.makeNewTimeObject(startTime, endTime).apply {
                targetTemplate.value?.let {
                    type = it.type
                    style = it.style
                    colorKey = it.colorKey
                    fontColor = it.fontColor
                    inCalendar = it.inCalendar
                    tags.addAll(it.tags)
                    return@let
                }
                folder = targetFolder.value
            }

    fun setTargetTimeObjectById(id: String?) {
        realm?.let { realm ->
            id?.let {
                targetTimeObject.value = realm.where(TimeObject::class.java)
                        .equalTo("id", it)
                        .findFirst()
            }
        }
    }

    fun saveDirectByTemplate() {
        MainActivity.instance?.getCalendarView()?.let {
            TimeObjectManager.save(makeTimeObjectByTatgetTemplate(
                    getCalendarTime0(it.targetCal), getCalendarTime23(it.targetCal)))
        }
    }

    fun setTargetFolder() {
        var folder = folderList.value?.firstOrNull()
        if(folder == null) {
            l("[새 폴더 생성]")
            realm?.executeTransaction {
                folder = it.createObject(Folder::class.java, UUID.randomUUID().toString()).apply {
                    name = App.context.getString(R.string.keep)
                    order = 0
                }
            }
        }
        targetFolder.value = folder
    }
}