package com.ayaan.twelvepages.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.manager.RepeatManager
import com.ayaan.twelvepages.model.*
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.view.CalendarView
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val realm = MutableLiveData<Realm>()
    val loading = MutableLiveData<Boolean>()
    val targetRecord = MutableLiveData<Record?>()
    val clipRecord = MutableLiveData<Record?>()
    val appUser = MutableLiveData<AppUser?>()
    val targetTemplate = MutableLiveData<Template>()
    val templateList = MutableLiveData<RealmResults<Template>>()
    val targetFolder = MutableLiveData<Folder>()
    val folderList = MutableLiveData<RealmResults<Folder>>()
    val targetTime = MutableLiveData<Long>()
    val targetCalendarView = MutableLiveData<CalendarView>()
    val openFolder = MutableLiveData<Boolean>()
    val countdownRecords = MutableLiveData<RealmResults<Record>>()
    val undoneRecords = MutableLiveData<RealmResults<Record>>()

    private var realmAsyncTask: RealmAsyncTask? = null

    init {
        targetTime.value = System.currentTimeMillis()
        openFolder.value = false
        clipRecord.value = null
    }

    fun initRealm(syncUser: SyncUser?) {
        if(syncUser == null) {
            realm.value = Realm.getDefaultInstance()
            loading.value = false
            loadTemplate()
            loadFolder()
            loadAppUser()
            loadData()
        }else {
            loading.value = true
            val config = syncUser
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
            return@let
        }
    }

    private fun loadTemplate() {
        realm.value?.let { realm ->
            templateList.value = realm.where(Template::class.java).sort("order", Sort.ASCENDING).findAllAsync()
            templateList.value?.addChangeListener { result, _ ->
                templateList.postValue(result)
            }
            return@let
        }
    }

    private fun loadFolder() {
        realm.value?.let { realm ->
            folderList.value = realm.where(Folder::class.java).sort("order", Sort.ASCENDING).findAllAsync()
            folderList.value?.addChangeListener { result, _ ->
                folderList.postValue(result)
                if(result.size == 0) {
                    makePrimaryFolder()
                }else {
                    if(targetFolder.value == null) setTargetFolder()
                }
            }
            return@let
        }
    }

    private fun loadData() {
        realm.value?.let { realm ->
            countdownRecords.value = realm.where(Record::class.java)
                    .equalTo("links.type", Link.Type.DDAY.ordinal)
                    .notEqualTo("dtCreated", -1L)
                    .sort("dtStart", Sort.ASCENDING)
                    .findAllAsync()
            countdownRecords.value?.addChangeListener { result, _ ->
                countdownRecords.postValue(result)
            }

            undoneRecords.value = realm.where(Record::class.java)
                    .equalTo("links.type", Link.Type.DDAY.ordinal)
                    .notEqualTo("dtCreated", -1L)
                    .sort("dtStart", Sort.ASCENDING)
                    .findAllAsync()
            undoneRecords.value?.addChangeListener { result, _ ->
                undoneRecords.postValue(result)
            }
            return@let
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

    fun setTargetTimeObjectById(id: String?, dtStart: Long) {
        realm.value?.let { realm ->
            id?.let {
                realm.where(Record::class.java)
                        .equalTo("id", it)
                        .findFirst()?.let { record ->
                            if(dtStart != Long.MIN_VALUE) {
                                targetRecord.value = RepeatManager.makeRepeatInstance(record, dtStart)
                            }else {
                                targetRecord.value = record.makeCopyObject()
                            }

                        }
            }
        }
    }

    fun setCalendarFolder() {
        folderList.value?.first{ it.isCalendar() }?.let { setTargetFolder(it) }
    }

    fun setTargetFolder() {
        folderList.value?.get(0)?.let { setTargetFolder(it) }
    }

    fun setTargetFolder(folder: Folder) {
        targetFolder.postValue(folder)
    }

    private fun makePrimaryFolder() {
        l("[PRIMARY 보관함 생성]")
        realm.value?.executeTransaction { realm ->
            realm.createObject(Folder::class.java, UUID.randomUUID().toString()).apply {
                name = App.context.getString(R.string.keep)
                order = 0
            }
        }
    }

    fun clearTargetTimeObject() {
        targetRecord.value = null
    }

    fun makeNewTimeObject(startTime: Long, endTime: Long) {
        val folder = targetFolder.value
        targetRecord.value = if(folder?.type == 0) makeTimeObjectByTatgetTemplate(folder, startTime, endTime)
        else makeTimeObjectByTatgetTemplate(folder, Long.MIN_VALUE, Long.MIN_VALUE)
    }

    private fun makeTimeObjectByTatgetTemplate(targetFolder: Folder?, startTime: Long, endTime: Long) =
            RecordManager.makeNewRecord(startTime, endTime).apply {
                targetTemplate.value?.let {
                    title = it.recordTitle ?: ""
                    type = it.type
                    style = it.style
                    colorKey = it.colorKey
                    tags.addAll(it.tags)
                    if(it.isScheduled()) setSchedule()
                    if(it.isSetCheckBox()) setCheckBox()
                    if(it.alarmOffset != Long.MIN_VALUE) {
                        if(AlarmManager.getOffsetText(it.alarmOffset) != null) {
                            setAlarm(it.alarmOffset, 0)
                        }else {
                            tempCal.timeInMillis = it.alarmOffset
                            val hour = tempCal.get(Calendar.HOUR_OF_DAY)
                            val min = tempCal.get(Calendar.MINUTE)
                            tempCal.timeInMillis = startTime
                            tempCal.set(Calendar.HOUR_OF_DAY, hour)
                            tempCal.set(Calendar.MINUTE, min)
                            setAlarm(Long.MIN_VALUE, tempCal.timeInMillis)
                        }
                    }
                    return@let
                }
                folder = targetFolder
            }

    override fun onCleared() {
        super.onCleared()
        realm.value?.removeAllChangeListeners()
        realm.value?.close()
        realm.value = null
        realmAsyncTask?.cancel()
    }

    fun clip(record: Record) {
        clipRecord.value = record
    }

}