package com.hellowo.journey.viewmodel

import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.*
import com.hellowo.journey.R
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.*
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.view.CalendarView
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val realm = MutableLiveData<Realm>()
    val loading = MutableLiveData<Boolean>()
    val targetTimeObject = MutableLiveData<TimeObject?>()
    val targetView = MutableLiveData<View?>()
    val appUser = MutableLiveData<AppUser?>()
    val targetTemplate = MutableLiveData<Template>()
    val templateList = MutableLiveData<RealmResults<Template>>()
    val colorTagList = MutableLiveData<List<ColorTag>>()
    val targetFolder = MutableLiveData<Folder>()
    val folderList = MutableLiveData<RealmResults<Folder>>()
    val targetTime = MutableLiveData<Long>()
    val targetCalendarView = MutableLiveData<CalendarView>()

    private var realmAsyncTask: RealmAsyncTask? = null

    init {
        targetTime.value = System.currentTimeMillis()
    }

    fun initRealm(syncUser: SyncUser?) {
        if(syncUser == null) {
            realm.value = Realm.getDefaultInstance()
            loading.value = false
            loadTemplate()
            loadFolder()
            loadAppUser()
        }else {
            loading.value = true
            val config = SyncUser.current()
                    .createConfiguration(USER_URL)
                    .fullSynchronization()
                    .waitForInitialRemoteData()
                    .build()
            realmAsyncTask = Realm.getInstanceAsync(config, object : Realm.Callback() {
                override fun onSuccess(db: Realm) {
                    l("Realm 준비 완료")
                    Realm.setDefaultConfiguration(config)
                    realm.value = db
                    loading.value = false
                    loadAppUser()
                    loadTemplate()
                    loadFolder()
                }
            })
        }
    }

    private fun loadAppUser() {
        realm.value?.let { realm ->
            realm.where(AppUser::class.java).findAllAsync().addChangeListener { result, _ ->
                if(result.size > 0) {
                    appUser.value = result[0]
                }else {
                    l("[새로운 유저 생성]")
                    realm.executeTransaction {
                        it.createObject(AppUser::class.java, FirebaseAuth.getInstance().uid)
                    }
                }
            }
        }
    }

    private fun loadTemplate() {
        realm.value?.let { realm ->
            templateList.value = realm.where(Template::class.java).sort("order", Sort.ASCENDING).findAll()
            templateList.value?.addChangeListener { result, _ ->
                templateList.value = result
            }
            return@let
        }
    }

    private fun loadFolder() {
        realm.value?.let { realm ->
            folderList.value = realm.where(Folder::class.java).sort("order", Sort.ASCENDING).findAll()
            folderList.value?.addChangeListener { result, _ ->
                folderList.value = result
            }
        }
    }

    fun saveProfileImage(profileImgUrl: String) {
        realm.value?.executeTransaction {
            appUser.value?.profileImgUrl = profileImgUrl
            appUser.value = appUser.value
        }
    }

    fun saveMotto(text: String) {
        realm.value?.executeTransaction {
            appUser.value?.motto = text
            appUser.value = appUser.value
        }
    }

    fun setTargetTimeObjectById(id: String?) {
        realm.value?.let { realm ->
            id?.let {
                targetTimeObject.value = realm.where(TimeObject::class.java)
                        .equalTo("id", it)
                        .findFirst()
            }
        }
    }

    fun setTargetFolder() {
        var folder = folderList.value?.firstOrNull()
        if(folder == null) {
            l("[새 폴더 생성]")
            realm.value?.executeTransaction { realm ->
                folder = realm.createObject(Folder::class.java, UUID.randomUUID().toString()).apply {
                    name = App.context.getString(R.string.keep)
                    order = 0
                }
            }
        }
        targetFolder.value = folder
    }

    fun clearTargetFolder() {
        targetFolder.value = null
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
                    inCalendar = it.inCalendar
                    tags.addAll(it.tags)
                    return@let
                }
                folder = targetFolder.value
            }

    fun saveDirectByTemplate() {
        MainActivity.getTargetCal()?.let {
            TimeObjectManager.save(makeTimeObjectByTatgetTemplate(getCalendarTime0(it), getCalendarTime23(it)))
        }
    }

    override fun onCleared() {
        super.onCleared()
        realm.value?.removeAllChangeListeners()
        realm.value?.close()
        realm.value = null
        realmAsyncTask?.cancel()
    }

}