package com.ayaan.twelvepages.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.dialog.*
import com.ayaan.twelvepages.ui.view.RecordView
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_template.*
import java.util.*

class TemplateActivity : BaseActivity() {
    private val realm = Realm.getDefaultInstance()
    private val template = Template()
    private val originalTemplate = Template()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)
        initTheme(rootLy)
        backBtn.setOnClickListener { onBackPressed() }
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if(scrollY > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }

        deleteBtn.setOnClickListener {
            showDialog(CustomDialog(this@TemplateActivity, getString(R.string.delete),
                    getString(R.string.delete_template), null) { result, _, _ -> if(result) { delete() }
            }, true, true, true, false)
        }

        if(!intent.getStringExtra("id").isNullOrEmpty()) {
            realm.where(Template::class.java)
                    .equalTo("id", intent.getStringExtra("id"))
                    .findFirst()?.let {
                        template.copy(it)
                        originalTemplate.copy(it)
                    }
        }else {
            template.folder = MainActivity.getTargetFolder()
            originalTemplate.folder = MainActivity.getTargetFolder()
        }
        l(template.toString())

        updateCalendarBlockStyleUI()
        updateAlarmUI()
        updateMemoUI()
        updateCheckBoxUI()
        updateScheduleUI()
        updateTitleUI()
        updateFolderUI()
        updateColorUI()
        updateTagUI()
        updataInitTextUI()
    }

    private fun updateCalendarBlockStyleUI() {
        recordViewStyleText.text = RecordView.getStyleText(template.style)
        recordViewStyleBtn.setOnClickListener {
            showDialog(RecordViewStyleDialog(this, null, template) { style, colorKey ->
                template.style = style
                template.colorKey = colorKey
                updateCalendarBlockStyleUI()
                updateColorUI()
            }, true, true, true, false)
        }
    }

    private fun updateAlarmUI() {
        alarmText.text = template.getAlarmText()
        alarmBtn.setOnClickListener {
            showDialog(AlarmPickerDialog(this, if(template.alarmOffset != Long.MIN_VALUE) template.alarmOffset else 0,
                    Long.MAX_VALUE) { result, offset, dtAlarm ->
                if (result) {
                    if(offset != Long.MIN_VALUE) {
                        template.alarmOffset = offset
                    }else {
                        template.alarmOffset = dtAlarm
                    }
                } else {
                    template.alarmOffset = Long.MIN_VALUE
                }
                updateAlarmUI()
            }, true, true, true, false)
        }
    }

    private fun updateMemoUI() {
        if(template.isSetMemo()) {
            memoText.setTextColor(AppTheme.primaryText)
            memoText.text = getString(R.string.use)
        }else {
            memoText.setTextColor(AppTheme.disableText)
            memoText.text = getString(R.string.unuse)
        }
        memoBtn.setOnClickListener {
            if(template.isSetMemo()) template.clearMemo()
            else template.setMemo()
            updateMemoUI()
        }
    }

    private fun updateCheckBoxUI() {
        if(template.isSetCheckBox()) {
            checkBoxText.setTextColor(AppTheme.primaryText)
            checkBoxText.text = getString(R.string.use)
        }else {
            checkBoxText.setTextColor(AppTheme.disableText)
            checkBoxText.text = getString(R.string.unuse)
        }
        checkBoxBtn.setOnClickListener {
            if(template.isSetCheckBox()) template.clearCheckBox()
            else template.setCheckBox()
            updateCheckBoxUI()
        }
    }

    private fun updateScheduleUI() {
        if(template.isScheduled()) {
            scheduleText.setTextColor(AppTheme.primaryText)
            scheduleText.text = getString(R.string.use)
        }else {
            scheduleText.setTextColor(AppTheme.disableText)
            scheduleText.text = getString(R.string.unuse)
        }
        scheduleBtn.setOnClickListener {
            if(template.isScheduled()) template.clearSchdule()
            else template.setSchedule()
            updateScheduleUI()
        }
    }

    private fun updateTitleUI() {
        if(!template.title.isNullOrBlank()) {
            titleInput.setText(template.title.toString())
        }
    }

    private fun updateFolderUI() {
        folderBtn.setOnClickListener {
            MainActivity.getViewModel()?.folderList?.value?.let { folderList ->
                showDialog(CustomListDialog(this,
                        getString(R.string.folder),
                        null,
                        null,
                        false,
                        folderList.map { if(it.name.isNullOrBlank()) getString(R.string.empty) else it.name!! }) { index ->
                    template.folder = folderList[index]
                    updateFolderUI()
                }, true, true, true, false)
            }
        }
        folderText.text = template.folder?.name
    }

    private fun updateColorUI() {
        colorImg.setColorFilter(AppTheme.getColor(template.colorKey))
        colorBtn.setOnClickListener {
            val location = IntArray(2)
            colorImg.getLocationOnScreen(location)
            showDialog(ColorPickerDialog(this@TemplateActivity, template.colorKey, location) { colorKey ->
                template.colorKey = colorKey
                updateColorUI()
            }, true, true, true, false)
        }
    }

    private fun updateTagUI() {
        tagView.setItems(template.tags, null)
        tagView.onSelected = { _, _ ->
            showDialog(TagDialog(this@TemplateActivity, ArrayList(template.tags)) { tags ->
                template.tags.clear()
                template.tags.addAll(tags)
                updateTagUI()
            }, true, true, true, false)
        }
    }

    private fun updataInitTextUI() {
        val title = template.recordTitle ?: ""
        initTextInput.setText(title)
        initTextCursorText.text = String.format(getString(R.string.cursor_pos), template.recordTitleSelection)
        if(template.recordTitleSelection <= title.length) {
            initTextInput.setSelection(template.recordTitleSelection)
        }
        initTextInput.onSelectionChanged = { startPos, _ ->
            template.recordTitleSelection = startPos
            initTextCursorText.text = String.format(getString(R.string.cursor_pos), startPos)
        }
    }

    private fun confirm() {
        template.title = titleInput.text.toString()
        template.recordTitle = initTextInput.text.toString()
        if(template != originalTemplate) {
            realm.executeTransaction {
                if(template.id.isNullOrEmpty()) {
                    template.id = UUID.randomUUID().toString()
                    template.order = realm.where(Template::class.java).max("order")?.toInt()?.plus(1) ?: 0
                }
                realm.insertOrUpdate(template)
            }
            toast(R.string.saved, R.drawable.done)
        }
        finish()
    }

    private fun delete() {
        realm.executeTransaction {
            realm.where(Template::class.java).equalTo("id", template.id)
                    .findFirst()?.deleteFromRealm()
        }
        toast(R.string.deleted, R.drawable.delete)
        finish()
    }

    override fun onBackPressed() {
        confirm()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}