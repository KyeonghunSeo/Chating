package com.ayaan.twelvepages.ui.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.TemplateAdapter
import com.ayaan.twelvepages.adapter.util.TemplateDiffCallback
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.TemplateActivity
import kotlinx.android.synthetic.main.view_template.view.*
import java.util.*
import kotlin.collections.ArrayList

class TemplateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    val items = ArrayList<Template>()
    var selectedPosition = 0
    var isExpanded = false
    val adapter = TemplateAdapter(context, items) { template, mode ->
        if(template != null) {
            if(mode == 0) {
                selectItem(template)
                MainActivity.getViewModel()?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
            }else {
                MainActivity.instance?.let {
                    val intent = Intent(it, TemplateActivity::class.java)
                    intent.putExtra("id", template.id)
                    it.startActivity(intent)
                }
            }
        }else {
            MainActivity.instance?.let { it.startActivity(Intent(it, TemplateActivity::class.java)) }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template, this, true)
        initViews()
        recyclerView.layoutManager = LinearLayoutManager(context)
        //recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        addBtn.setOnClickListener {
            if(isExpanded) {
                collapse()
            }else {
                MainActivity.getTargetTime()?.let { expand(it, it) }
            }
        }

        addBtn.setOnLongClickListener {
            MainActivity.getViewModel()?.targetTemplate?.value = null
            MainActivity.getViewModel()?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
            return@setOnLongClickListener false
        }

        editTemplateBtn.setOnClickListener {
            vibrate(context)
            if(adapter.mode == 0) {
                adapter.mode = 1
                editTemplateBtn.setTextColor(AppTheme.blue)
                editTemplateBtn.text = context.getString(R.string.edit_done)
            }else {
                adapter.mode = 0
                editTemplateBtn.setTextColor(AppTheme.primaryText)
                editTemplateBtn.text = context.getString(R.string.edit_template)
            }
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDate() {
        val folder = MainActivity.getTargetFolder()
        if(folder.isCalendar()) {
            startDateText.text = folder.name
            endDateText.text = makeSheduleText(startCal.timeInMillis, endCal.timeInMillis)
        }else {
            startDateText.text = folder.name
            endDateText.text = ""
        }
    }

    fun expand(dtStart: Long, dtEnd: Long) {
        vibrate(context)
        startCal.timeInMillis = dtStart
        endCal.timeInMillis = dtEnd
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
        val t3 = makeFromLeftSlideTransition()
        t1.addTarget(recyclerView)
        t2.addTarget(backgroundLy)
        t3.addTarget(dateLy)
        t3.addTarget(controlLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.addTransition(t3)
        TransitionManager.beginDelayedTransition(this, transitionSet)
        setDate()
        notifyListChanged()
        backgroundLy.setBackgroundColor(AppTheme.background)
        backgroundLy.setOnClickListener { collapse() }
        backgroundLy.isClickable = true
        recyclerView.visibility = View.VISIBLE
        backgroundLy.visibility = View.VISIBLE
        dateLy.visibility = View.VISIBLE
        controlLy.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f).start()
        isExpanded = true
    }

    fun collapse() {
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        val t3 = makeFromLeftSlideTransition()
        t1.addTarget(recyclerView)
        t2.addTarget(backgroundLy)
        t3.addTarget(dateLy)
        t3.addTarget(controlLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.addTransition(t3)
        TransitionManager.beginDelayedTransition(this, transitionSet)
        initViews()
        ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f).start()
    }

    fun collapseNoAnim() {
        templateIconImg.rotation = 0f
        initViews()
    }

    private fun initViews() {
        clearTemplate()
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        recyclerView.visibility = View.GONE
        backgroundLy.visibility = View.GONE
        dateLy.visibility = View.GONE
        controlLy.visibility = View.GONE
        adapter.mode = 0
        editTemplateBtn.text = context.getString(R.string.edit_template)
        editTemplateBtn.setTextColor(AppTheme.primaryText)
        isExpanded = false
    }

    private fun selectItem(template: Template) {
        selectedPosition = items.indexOf(template)
        MainActivity.getViewModel()?.targetTemplate?.value = template
    }

    private fun clearTemplate() {
        items.clear()
        adapter.notifyDataSetChanged()
    }

    fun notifyListChanged() {
        if(isExpanded) {
            val newItems = ArrayList<Template>()
            filterCurrentFolder(newItems)
            Thread {
                val diffResult = DiffUtil.calculateDiff(TemplateDiffCallback(items, newItems))
                Handler(Looper.getMainLooper()).post{
                    if(isExpanded) {
                        items.clear()
                        items.addAll(newItems)
                        diffResult.dispatchUpdatesTo(adapter)
                    }
                }
            }.start()
        }else {
            filterCurrentFolder(items)
            adapter.notifyDataSetChanged()
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
}