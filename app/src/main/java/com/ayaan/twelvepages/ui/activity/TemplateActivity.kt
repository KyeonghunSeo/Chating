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

        updateColorUI()
        updateCalendarBlockStyleUI()
        updateSymbolUI()
        updateAlarmUI()
        updateMemoUI()
        updateCheckBoxUI()
        updateTitleUI()
        updateFolderUI()
        updateTagUI()
        updataInitTextUI()
    }

    private fun updateColorUI() {
        colorBg.setColorFilter(ColorManager.getColor(template.colorKey))
        colorBtn.setOnClickListener {
            ColorPickerDialog(template.colorKey){ colorKey ->
                template.colorKey = colorKey
                updateSymbolUI()
            }.show(supportFragmentManager, null)
        }
    }

    private fun updateCalendarBlockStyleUI() {
        recordViewStyleText.text = RecordView.getStyleText(template.style)
        recordViewStyleBtn.setOnClickListener {
            showDialog(RecordViewStyleDialog(this, null, template) { style, colorKey ->
                template.style = style
                template.colorKey = colorKey
                updateCalendarBlockStyleUI()
                updateSymbolUI()
            }, true, true, true, false)
        }
    }

    private fun updateSymbolUI() {
        val color = ColorManager.getColor(template.colorKey)
        val fontColor = ColorManager.getFontColor(color)
        symbolColor.setColorFilter(color)
        symbalImg.setColorFilter(fontColor)
        symbalImg.setImageResource(SymbolManager.getSymbolResId(template.symbol))
        symbolBtn.setOnClickListener {
            SymbolPickerDialog(template.symbol){
                template.symbol = it.name
                symbalImg.setImageResource(SymbolManager.getSymbolResId(template.symbol))
            }.show(supportFragmentManager, null)
        }
    }

    private fun updateAlarmUI() {
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