package com.hellowo.journey.ui.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TemplateAdapter
import com.hellowo.journey.model.Template
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.TemplateActivity
import kotlinx.android.synthetic.main.view_template.view.*
import java.util.*

class TemplateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    val layoutManager = LinearLayoutManager(context)
    val items = ArrayList<Template>()
    var selectedPosition = 0
    var isExpanded = false
    val adapter = TemplateAdapter(context, items) { template, mode ->
        if(mode == 0) {
            selectItem(template)
            collapseNoAnim()
            MainActivity.getViewModel()?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
        }else {
            MainActivity.instance?.let {
                val intent = Intent(it, TemplateActivity::class.java)
                intent.putExtra("id", template.id)
                it.startActivity(intent)
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template, this, true)
        initViews()
        recyclerView.layoutManager = layoutManager
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
            MainActivity.getViewModel()?.makeNewTimeObject(0)
            return@setOnLongClickListener false
        }

        editTemplateBtn.setOnClickListener {
            if(adapter.mode == 0) {
                adapter.mode = 1
                editTemplateBtn.setTextColor(AppTheme.blueColor)
                editTemplateBtn.text = context.getString(R.string.done)
                addNewBtn.visibility = View.VISIBLE
            }else {
                adapter.mode = 0
                editTemplateBtn.setTextColor(AppTheme.primaryText)
                editTemplateBtn.text = context.getString(R.string.edit_template)
                addNewBtn.visibility = View.GONE
            }
            adapter.notifyItemRangeChanged(0, items.size)
        }

        addNewBtn.setOnClickListener {
            MainActivity.instance?.let { it.startActivity(Intent(it, TemplateActivity::class.java)) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDate(dtStart: Long, dtEnd: Long) {
        val folder = MainActivity.getTargetFolder()
        if(folder.isCalendar()) {
            startDateText.text = folder.name
            startCal.timeInMillis = dtStart
            endCal.timeInMillis = dtEnd
            if(isSameDay(startCal, endCal)) {
                endDateText.text = AppDateFormat.mdeDate.format(startCal.time)
            }else {
                endDateText.text = "${AppDateFormat.mdeDate.format(startCal.time)}\n~ ${AppDateFormat.mdeDate.format(endCal.time)}"
            }
        }else {
            startDateText.text = folder.name
            endDateText.text = ""
        }
    }

    fun expand(dtStart: Long, dtEnd: Long) {
        vibrate(context)
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
        setDate(dtStart, dtEnd)
        setCurrentFolderTemplate()
        backgroundLy.setBackgroundColor(AppTheme.backgroundColor)
        backgroundLy.setOnClickListener { collapse() }
        backgroundLy.isClickable = true
        recyclerView.visibility = View.VISIBLE
        backgroundLy.visibility = View.VISIBLE
        dateLy.visibility = View.VISIBLE
        controlLy.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f).start()
        elevation = dpToPx(30f)
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
        addNewBtn.visibility = View.GONE
        isExpanded = false
    }

    private fun selectItem(template: Template) {
        selectedPosition = items.indexOf(template)
        MainActivity.getViewModel()?.targetTemplate?.value = template
    }

    private fun setCurrentFolderTemplate() {
        items.clear()
        MainActivity.getViewModel()?.templateList?.value?.filter {
            it.folder?.id == MainActivity.getTargetFolder().id
        }?.forEach {
            l(it.toString())
            items.add(it)
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun clearTemplate() {
        items.clear()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun notifyListChanged() {
        if(isExpanded) {
            setCurrentFolderTemplate()
        }
    }
}