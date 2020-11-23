package com.ayaan.twelvepages.ui.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import com.ayaan.twelvepages.App
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Tag
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.str
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.ayaan.twelvepages.ui.view.RecordView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_init_template.*
import kotlinx.android.synthetic.main.activity_init_template.diaryBtn0
import kotlinx.android.synthetic.main.activity_init_template.diaryBtn1
import kotlinx.android.synthetic.main.activity_init_template.diaryBtn2
import kotlinx.android.synthetic.main.activity_init_template.diaryBtn3
import kotlinx.android.synthetic.main.activity_init_template.diarySelector0
import kotlinx.android.synthetic.main.activity_init_template.diarySelector1
import kotlinx.android.synthetic.main.activity_init_template.diarySelector2
import kotlinx.android.synthetic.main.activity_init_template.diarySelector3
import kotlinx.android.synthetic.main.activity_init_template.rootLy
import java.util.*

class InitTemplateActivity : BaseActivity() {
    var diaryNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_template)
        initTheme(rootLy)

        val user = FirebaseAuth.getInstance().currentUser
        optionTitleText.text = String.format(getString(R.string.init_setting_script_0), user?.displayName)
        Glide.with(this).load(R.drawable.basic_sample).into(diarySelector0)
        Glide.with(this).load(R.drawable.planner_sample).into(diarySelector1)
        Glide.with(this).load(R.drawable.diary_sample).into(diarySelector2)
        Glide.with(this).load(R.drawable.free_sample).into(diarySelector3)

        val btns = arrayOf(diaryBtn0, diaryBtn1, diaryBtn2, diaryBtn3)
        val selectors = arrayOf(diarySelector0, diarySelector1, diarySelector2, diarySelector3)
        btns.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                diaryNum = index
                selectors.forEachIndexed { i, imageView ->
                    ObjectAnimator.ofFloat(btns[i], "cardElevation", if(index == i) dpToPx(30f) else dpToPx(1f)).start()
                    ObjectAnimator.ofFloat(btns[i], "alpha", if(index == i) 1f else 0.5f).start()
                }
            }
        }

        confirmBtn.setOnClickListener {
            val dialog = CustomDialog(this, getString(R.string.init_template),
                    getString(R.string.ask_init), null) { result, _, _ ->
                if(result) {
                    initTemplate()
                    finish()
                }
            }
            com.ayaan.twelvepages.showDialog(dialog, true, true, true, false)
        }
    }

    private fun initTemplate() {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            try{
                realm.createObject(Folder::class.java, "calendar").apply {
                    name = App.context.getString(R.string.calendar)
                    type = 0
                    order = 0
                }
            }catch (e: Exception){e.printStackTrace()}

            realm.where(Folder::class.java).equalTo("id", "calendar").findFirst()?.let { f ->
                realm.where(Template::class.java).findAll()?.deleteAllFromRealm() /* 이부분 지워도 됨 */

                val record = Record()
                var od = realm.where(Template::class.java).max("order")?.toInt() ?: -1
                when(diaryNum) {
                    0 -> {
                        var importantTag: Tag? = null
                        val tagOrder = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                        importantTag = realm.where(Tag::class.java).equalTo("title", str(R.string.important)).findFirst()
                        if(importantTag == null) {
                            importantTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                            importantTag?.title = str(R.string.important)
                            importantTag?.order = tagOrder + 1
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                        record.setShape(RecordView.Shape.RECT_FILL_BLUR)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.event)
                            style = record.style
                            colorKey = 0
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                        record.setShape(RecordView.Shape.COLOR_PEN)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.todo)
                            style = record.style
                            colorKey = 0
                            setCheckBox()
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.MULTI_TEXT)
                        record.setShape(RecordView.Shape.COLOR_PEN)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.memo)
                            style = record.style
                            clearTitle()
                            setMemo()
                            colorKey = 0
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                        record.setShape(RecordView.Shape.RECT_FILL)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.important_event)
                            style = record.style
                            colorKey = 4
                            alarmDayOffset = 0
                            alarmTime = AlarmManager.defaultAlarmTime[0]
                            importantTag?.let { tags.add(it) }
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                        record.setShape(RecordView.Shape.RANGE)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.plan)
                            style = record.style
                            colorKey = 0
                        }
                    }
                    1 -> {
                        var importantTag: Tag? = null
                        var emerTag: Tag? = null
                        val tagOrder = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                        importantTag = realm.where(Tag::class.java).equalTo("title", str(R.string.important)).findFirst()
                        if(importantTag == null) {
                            importantTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                            importantTag?.title = str(R.string.important)
                            importantTag?.order = tagOrder + 1
                        }
                        emerTag = realm.where(Tag::class.java).equalTo("title", str(R.string.emergency)).findFirst()
                        if(emerTag == null) {
                            emerTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                            emerTag?.title = str(R.string.emergency)
                            emerTag?.order = tagOrder + 2
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                        record.setShape(RecordView.Shape.RECT_FILL)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.event)
                            style = record.style
                            colorKey = 2
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                        record.setShape(RecordView.Shape.BOLD_HATCHED)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.important_event)
                            style = record.style
                            alarmDayOffset = 0
                            alarmTime = AlarmManager.defaultAlarmTime[0]
                            colorKey = 4
                            setTime()
                            importantTag?.let { tags.add(it) }
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.DOT)
                        record.setShape(RecordView.Shape.BLANK)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.todo)
                            style = record.style
                            colorKey = 0
                            setCheckBox()
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                        record.setShape(RecordView.Shape.COLOR_PEN)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.important_todo)
                            style = record.style

                            colorKey = 0
                            alarmDayOffset = 0
                            alarmTime = AlarmManager.defaultAlarmTime[0]
                            setCheckBox()
                            emerTag?.let { tags.add(it) }
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                        record.setShape(RecordView.Shape.RANGE)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.plan)
                            style = record.style
                            colorKey = 6
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.MULTI_TEXT)
                        record.setShape(RecordView.Shape.COLOR_PEN)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.memo)
                            style = record.style
                            colorKey = 0
                            clearTitle()
                            setMemo()
                        }
                    }
                    2 -> {
                        var diaryTag: Tag? = null
                        val tagOrder = realm.where(Tag::class.java).max("order")?.toInt() ?: -1
                        diaryTag = realm.where(Tag::class.java).equalTo("title", str(R.string.diary)).findFirst()
                        if(diaryTag == null) {
                            diaryTag = realm.createObject(Tag::class.java, UUID.randomUUID().toString())
                            diaryTag?.title = str(R.string.diary)
                            diaryTag?.order = tagOrder + 1
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.MULTI_TEXT)
                        record.setShape(RecordView.Shape.COLOR_PEN)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.diary)
                            style = record.style
                            colorKey = 0
                            setMemo()
                            diaryTag?.let { tags.add(it) }
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.DOT)
                        record.setShape(RecordView.Shape.BLANK)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.memo)
                            style = record.style
                            colorKey = 0
                            clearTitle()
                            setMemo()
                        }

                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT)
                        record.setShape(RecordView.Shape.RANGE)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.plan)
                            style = record.style
                            colorKey = 0
                        }
                    }
                    else -> {
                        od++
                        record.setFormula(RecordCalendarAdapter.Formula.SINGLE_TEXT)
                        record.setShape(RecordView.Shape.COLOR_PEN)
                        realm.createObject(Template::class.java, UUID.randomUUID().toString())?.apply {
                            folder = f
                            order = od
                            title = str(R.string.new_record)
                            style = record.style
                            colorKey = 0
                        }

                    }
                }
            }
        }
        realm.close()
    }
}