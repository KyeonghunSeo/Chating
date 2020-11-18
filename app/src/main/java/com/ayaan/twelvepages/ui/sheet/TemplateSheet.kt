package com.ayaan.twelvepages.ui.sheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.adapter.TemplateAdapter
import com.ayaan.twelvepages.adapter.util.TemplateDiffCallback
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Photo
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.TemplateActivity
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.ayaan.twelvepages.ui.dialog.StickerPickerDialog
import com.ayaan.twelvepages.viewmodel.MainViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.sheet_template.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class TemplateSheet(dtStart: Long, dtEnd: Long, val photo: Photo? = null) : BottomSheetDialog() {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    private val items = ArrayList<Template>()
    private var layoutMode = 0
    private lateinit var adapter: TemplateAdapter

    private fun makeTemplateAdapter(layout: Int) = TemplateAdapter(App.context, items, layout) { template, action ->
        if (template != null) {
            when (action) {
                0 -> {
                    if (photo != null) {
                        saveRecordWithPhoto(template, action, photo)
                    } else {
                        MainActivity.getViewModel()?.startNewRecordSheet(template, startCal.timeInMillis, endCal.timeInMillis)
                        dismiss()
                    }
                }
                1 -> {
                    MainActivity.instance?.let {
                        val intent = Intent(it, TemplateActivity::class.java)
                        intent.putExtra("id", template.id)
                        it.startActivity(intent)
                    }
                }
                else -> {
                    if (photo != null) {
                        saveRecordWithPhoto(template, action, photo)
                    } else {
                        MainActivity.getViewModel()?.saveRecordDirectly(template, startCal.timeInMillis, endCal.timeInMillis)
                        dismiss()
                        toast(R.string.saved)
                    }
                }
            }
        } else {
            if (items.size >= 6 && !AppStatus.isPremium()) {
                showPremiumDialog(MainActivity.instance!!)
            } else {
                MainActivity.instance?.let { it.startActivity(Intent(it, TemplateActivity::class.java)) }
            }
        }
    }

    init {
        layoutMode = Prefs.getInt("templateLayoutMode", 0)
        startCal.timeInMillis = dtStart
        endCal.timeInMillis = dtEnd
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.sheet_template)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setLayout()
        val mainViewModel: MainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        mainViewModel.templateList.observe(this, androidx.lifecycle.Observer { notifyListChanged() })
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        setTemplateListView()

        if (photo != null) {
            root.decoTitleText.visibility = View.GONE
            root.decoBtns.visibility = View.GONE
            root.photoLy.visibility = View.VISIBLE
            Glide.with(context!!).load(photo.url).into(root.photoView)
        } else {
            root.decoTitleText.visibility = View.VISIBLE
            root.decoBtns.visibility = View.VISIBLE
            root.photoLy.visibility = View.GONE
        }

        root.stickerBtn.setOnClickListener { addSticker() }
        root.dateBgBtn.setOnClickListener { addDatePoint() }
        root.layoutBtn.setOnClickListener { changeLayout() }
        setDate()
        initViews()

        if (true) {
            root.adView.visibility = View.GONE
        } else {
            root.adView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            root.adView.loadAd(adRequest)
        }
    }

    private fun changeLayout() {
        layoutMode = if (layoutMode == 0) 1 else 0
        Prefs.putInt("templateLayoutMode", layoutMode)
        setTemplateListView()
    }

    private fun setTemplateListView() {
        root.layoutBtn.setImageResource(if (layoutMode == 0) R.drawable.module else R.drawable.column)
        adapter = makeTemplateAdapter(if (layoutMode == 0) R.layout.list_item_template else R.layout.grid_list_item_template)
        root.recyclerView.layoutManager = if (layoutMode == 0) LinearLayoutManager(context, HORIZONTAL, false)
        else GridLayoutManager(context, 3)
        root.recyclerView.adapter = adapter
        root.recyclerView.post { root.recyclerView.scrollToPosition(0) }
        adapter.itemTouchHelper?.attachToRecyclerView(root.recyclerView)
    }

    private fun addSticker() {
        MainActivity.instance?.let {
            StickerPickerDialog { sticker, position ->
                val records = ArrayList<Record>()
                while (startCal <= endCal) {
                    val dtStart = getCalendarTime0(startCal)
                    val dtEnd = getCalendarTime23(startCal)
                    records.add(RecordManager.makeNewRecord(dtStart, dtEnd).apply {
                        id = "sticker_${UUID.randomUUID()}"
                        dtCreated = System.currentTimeMillis()
                        setFormula(RecordCalendarAdapter.Formula.STICKER)
                        setSticker(sticker, position)
                    })
                    startCal.add(Calendar.DATE, 1)
                }
                RecordManager.save(records)
                toast(R.string.saved, R.drawable.done)
                dismiss()
            }.show(it.supportFragmentManager, null)
        }
    }

    private fun addDatePoint() {
        MainActivity.instance?.let {
            /*
            ColorPickerDialog(0){
                val record = RecordManager.makeNewRecord(getCalendarTime0(startCal), getCalendarTime23(endCal)).apply {
                    id = "bg_${UUID.randomUUID()}"
                    dtCreated = System.currentTimeMillis()
                    setFormula(RecordCalendarAdapter.Formula.BACKGROUND)
                    setBg(0)
                    colorKey = it
                }
                RecordManager.save(record)
                toast(R.string.saved, R.drawable.done)
                dismiss()
            }.show(it.supportFragmentManager, null)
             */
            val record = RecordManager.makeNewRecord(getCalendarTime0(startCal), getCalendarTime23(endCal)).apply {
                id = "bg_${UUID.randomUUID()}"
                dtCreated = System.currentTimeMillis()
                setFormula(RecordCalendarAdapter.Formula.BACKGROUND)
                setBg(0)
            }
            EditDateBgSheet(record) { result ->
                RecordManager.save(record)
                toast(R.string.saved, R.drawable.done)
                dismiss()
            }.show(it.supportFragmentManager, null)

        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDate() {
        val folder = MainActivity.getTargetFolder()
        if (folder.isCalendar()) {
            root.templateDateText.text = makeSheduleText(startCal.timeInMillis, endCal.timeInMillis,
                    false, false, false, true)
        } else {
            root.templateDateText.text = folder.name
        }
    }

    private fun initViews() {
        root.contentLy.setOnClickListener {
            if (adapter.mode == 1) {
                adapter.endEditMode()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MainActivity.instance?.clearCalendarHighlight()
    }

    var isInit = true

    private fun notifyListChanged() {
        val newItems = ArrayList<Template>()
        filterCurrentFolder(newItems)
        if (isInit) {
            items.clear()
            items.addAll(newItems)
            adapter.notifyDataSetChanged()
            isInit = false
        } else {
            Thread {
                val diffResult = DiffUtil.calculateDiff(TemplateDiffCallback(items, newItems))
                Handler(Looper.getMainLooper()).post {
                    items.clear()
                    items.addAll(newItems)
                    diffResult.dispatchUpdatesTo(adapter)
                }
            }.start()
        }
    }

    private fun filterCurrentFolder(result: ArrayList<Template>) {
        result.clear()
        MainActivity.getViewModel()?.templateList?.value?.filter {
            it.folder?.id == MainActivity.getTargetFolder().id
        }?.forEach {
            val template = Template()
            template.copy(it)
            result.add(template)
        }
    }

    private fun saveRecordWithPhoto(template: Template, action: Int, photo: Photo) {
        try {
            MainActivity.instance?.let { mainActivity ->
                mainActivity.showProgressDialog(null)
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = BitmapFactory.decodeFile(photo.url)
                    l("사진 크기 : ${bitmap.rowBytes} 바이트")
                    val imageId = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().reference
                            .child("${FirebaseAuth.getInstance().uid}/$imageId.jpg")
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val uploadTask = ref.putBytes(baos.toByteArray())

                    withContext(Dispatchers.Main) {
                        uploadTask.addOnFailureListener {
                            mainActivity.hideProgressDialog()
                        }.addOnSuccessListener {
                            ref.downloadUrl.addOnCompleteListener {
                                l("다운로드 url : ${it.result}")
                                val photoLink = Link(imageId, Link.Type.IMAGE.ordinal, strParam0 = it.result.toString())
                                mainActivity.hideProgressDialog()
                                if (action == 0) {
                                    MainActivity.getViewModel()?.startNewRecordSheet(template,
                                            startCal.timeInMillis, endCal.timeInMillis, photoLink)
                                    dismiss()
                                } else {
                                    MainActivity.getViewModel()?.saveRecordDirectly(template,
                                            startCal.timeInMillis, endCal.timeInMillis, photoLink)
                                    dismiss()
                                    toast(R.string.saved)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
