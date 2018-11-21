package com.hellowo.journey.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellowo.journey.*
import com.hellowo.journey.App.Companion.resource
import com.hellowo.journey.R
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.*
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    val loading = MutableLiveData<Boolean>()
    val targetTimeObject = MutableLiveData<TimeObject?>()
    val targetView = MutableLiveData<View?>()
    val appUser = MutableLiveData<AppUser?>()
    val targetTemplate = MutableLiveData<Template>()
    val templateList = MutableLiveData<List<Template>>()
    val colorTagList = MutableLiveData<List<ColorTag>>()
    val isCalendarSettingOpened = MutableLiveData<Boolean>()
    val targetFolder = MutableLiveData<Folder>()

    init {
        loadAppUser()
        loadTemplate()
        loadColors()
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

    fun loadTemplate() {
        val templates = realm.where(Template::class.java).sort("order", Sort.ASCENDING).findAll()
        if(templates.isEmpty()) {
            realm.executeTransaction {
                TimeObject.Type.values().forEachIndexed { index, t ->
                    realm.createObject(Template::class.java, index).run {
                        title = App.context.getString(t.titleId)
                        type = t.ordinal
                        order = index
                    }
                }
            }
            templateList.value = realm.where(Template::class.java).sort("order", Sort.ASCENDING).findAll()
        }else {
            templateList.value = templates
        }
    }

    private fun loadColors() {
        val colors = realm.where(ColorTag::class.java).sort("order", Sort.ASCENDING).findAll()
        if(colors.isEmpty()) {
            realm.executeTransaction {
                val colorPack = resource.getStringArray(R.array.color_pack_title)
                val fontColor = resource.getStringArray(R.array.font_colors)
                val colorTitle = resource.getStringArray(R.array.color_title)
                val colors = resource.getStringArray(R.array.colors)
                (0 until colors.size).forEach {
                    val note = realm.createObject(ColorTag::class.java, it)
                    note.title = colorTitle[it]
                    note.color = Color.parseColor(colors[it])
                    note.fontColor = Color.parseColor(fontColor[it])
                    note.order = it
                }
            }
            colorTagList.value = realm.where(ColorTag::class.java).sort("order", Sort.ASCENDING).findAll()
        }else {
            colorTagList.value = colors
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
                }
            }

    fun setTargetTimeObjectById(id: String?) {
        id?.let {
            targetTimeObject.value = realm.where(TimeObject::class.java)
                    .equalTo("id", it)
                    .findFirst()
        }
    }

    fun saveDirectByTemplate() {
        MainActivity.instance?.getCalendarView()?.let {
            TimeObjectManager.save(makeTimeObjectByTatgetTemplate(
                    getCalendarTime0(it.targetCal), getCalendarTime23(it.targetCal)))
        }
    }
}