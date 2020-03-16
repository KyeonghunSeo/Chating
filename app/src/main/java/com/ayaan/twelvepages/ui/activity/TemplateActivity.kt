package com.ayaan.twelvepages.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.SymbolManager
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.dialog.*
import com.ayaan.twelvepages.ui.view.RecordView
import com.ayaan.twelvepages.ui.view.TagView
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
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if(scrollY > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }

        backBtn.setOnClickListener { onBackPressed() }
        moreBtn.setOnClickListener {
            showDialog(PopupOptionDialog(this@TemplateActivity,
                    arrayOf(PopupOptionDialog.Item(getString(R.string.delete), R.drawable.delete, AppTheme.red)),
                    moreBtn, false) { index ->
                if(index == 0) {
                    showDialog(CustomDialog(this@TemplateActivity,
                            getString(R.string.delete),
                            getString(R.string.delete_template),
                            null,
                            R.drawable.delete) { result, _, _ -> if(result) { delete() }
                    }, true, true, true, false)
                }
            }, true, false, true, false)
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

        if(!template.title.isNullOrBlank()) {
            titleInput.setText(template.title.toString())
        }

        updateTagUI()
        updateSymbolUI()
        updateColorUI()
        updateCalendarBlockStyleUI()
        updateTitleUI()
        updateMemoUI()
        updateDateUI()
        updateCheckBoxUI()
        updateFolderUI()
    }

    private fun updateTagUI() {
        if(template.tags.isEmpty()) {
            tagBtn.visibility = View.VISIBLE
            tagBtn.setOnClickListener { showTagDialog() }
        }else {
            tagBtn.visibility = View.GONE
        }
        tagView.setItems(template.tags, null)
        tagView.onSelected = { _, _ -> showTagDialog() }
    }

    private fun showTagDialog() {
        showDialog(TagDialog(this@TemplateActivity, ArrayList(template.tags)) { tags ->
            template.tags.clear()
            template.tags.addAll(tags)
            updateTagUI()
        }, true, true, true, false)
    }

    private fun updateSymbolUI() {
        val symbolRes = SymbolManager.getSymbolResId(template.symbol)
        if(symbolRes == R.drawable.blank) {
            symbalImg.setColorFilter(AppTheme.disableText)
            symbalImg.setBackgroundResource(R.drawable.circle_stroke_dash)
        }else {
            symbalImg.setColorFilter(AppTheme.primaryText)
            symbalImg.setBackgroundResource(R.drawable.blank)
        }
        symbalImg.setImageResource(symbolRes)
        symbolBtn.setOnClickListener {
            SymbolPickerDialog(template.symbol){
                template.symbol = it.name
                updateSymbolUI()
            }.show(supportFragmentManager, null)
        }
    }

    private fun updateColorUI() {
        colorBg.setColorFilter(ColorManager.getColor(template.colorKey))
        colorBtn.setOnClickListener {
            ColorPickerDialog(template.colorKey){ colorKey ->
                template.colorKey = colorKey
                updateColorUI()
                updateSymbolUI()
            }.show(supportFragmentManager, null)
        }
    }

    private fun updateCalendarBlockStyleUI() {
        recordViewStyleBtn.visibility = View.VISIBLE
        recordViewStyleText.text = RecordView.getStyleText(template.style)
        recordViewStyleBtn.setOnClickListener {
            showDialog(RecordViewStyleDialog(this, null, template) { style, colorKey, symbol ->
                template.style = style
                template.colorKey = colorKey
                template.symbol = symbol
                updateCalendarBlockStyleUI()
                updateSymbolUI()
                updateColorUI()
            }, true, true, true, false)
        }
    }

    private fun updateTitleUI() {
        if(template.isSetTitle()) {
            titleText.setTextColor(AppTheme.primaryText)
            titleText.text = getString(R.string.use)
            initTitleLy.visibility = View.VISIBLE
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
        }else {
            titleText.setTextColor(AppTheme.disableText)
            titleText.text = getString(R.string.unuse)
            initTitleLy.visibility = View.GONE
        }
        titleBtn.setOnClickListener {
            if(template.isSetTitle()) template.clearTitle()
            else template.setTitle()
            updateTitleUI()
        }
    }

    private fun updateMemoUI() {
        if(template.isSetMemo()) {
            memoText.setTextColor(AppTheme.primaryText)
            memoText.text = getString(R.string.use)
            initMemoLy.visibility = View.VISIBLE
            val memo = template.recordMemo ?: ""
            initMemoInput.setText(memo)
            initMemoCursorText.text = String.format(getString(R.string.cursor_pos), template.recordMemoSelection)
            if(template.recordMemoSelection <= memo.length) {
                initMemoInput.setSelection(template.recordMemoSelection)
            }
            initMemoInput.onSelectionChanged = { startPos, _ ->
                template.recordMemoSelection = startPos
                initMemoCursorText.text = String.format(getString(R.string.cursor_pos), startPos)
            }
        }else {
            memoText.setTextColor(AppTheme.disableText)
            memoText.text = getString(R.string.unuse)
            initMemoLy.visibility = View.GONE
        }
        memoBtn.setOnClickListener {
            if(template.isSetMemo()) template.clearMemo()
            else template.setMemo()
            updateMemoUI()
        }
    }

    private fun updateDateUI() {
        timeBtn.visibility = View.VISIBLE
        alarmBtn.visibility = View.VISIBLE
        updateTimeUI()
        updateAlarmUI()
    }

    private fun updateTimeUI() {
        if(template.isSetTime()) {
            timeText.setTextColor(AppTheme.primaryText)
            timeText.text = getString(R.string.use)
        }else {
            timeText.setTextColor(AppTheme.disableText)
            timeText.text = getString(R.string.unuse)
        }
        timeBtn.setOnClickListener {
            if(template.isSetTime()) template.clearTime()
            else template.setTime()
            updateTimeUI()
        }
    }

    private fun updateAlarmUI() {
        if(template.alarmDayOffset != Int.MIN_VALUE) {
            alarmText.setTextColor(AppTheme.primaryText)
        }else {
            alarmText.setTextColor(AppTheme.disableText)
        }
        alarmText.text = template.getAlarmText()
        alarmBtn.setOnClickListener {
            showDialog(AlarmPickerDialog(this, template.alarmDayOffset, template.alarmTime) { result, dayOffset, alarmTime ->
                if (result) {
                    template.alarmDayOffset = dayOffset
                    template.alarmTime = alarmTime
                } else {
                    template.alarmDayOffset = Int.MIN_VALUE
                }
                updateAlarmUI()
            }, true, true, true, false)
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

    private fun confirm() {
        template.title = titleInput.text.toString()
        template.recordTitle = initTextInput.text.toString()
        template.recordMemo = initMemoInput.text.toString()
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