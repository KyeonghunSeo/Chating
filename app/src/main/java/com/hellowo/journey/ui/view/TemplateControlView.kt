package com.hellowo.journey.ui.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TemplateAdapter
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.TemplateEditActivity
import com.hellowo.journey.ui.dialog.TypePickerDialog
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.view_template_control.view.*
import java.util.*

class TemplateControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    val layoutManager = LinearLayoutManager(context)
    val items = ArrayList<Template>()
    var selectedPosition = 0
    var autoScrollFlag = 0
    var isExpanded = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template_control, this, true)
        dateLy.visibility = View.GONE
        listLy.visibility = View.GONE
        controlLy.visibility = View.GONE
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = TemplateAdapter(context, items, startCal, endCal) {
            selectItem(it)
            MainActivity.instance?.viewModel?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
            collapseNoAnim()
        }

        addBtn.setOnClickListener { _ ->
            if(isExpanded) {
                collapse()
            }else {
                MainActivity.instance?.viewModel?.targetTime?.value?.let { expand(it, it) }
            }
        }

        editTemplateBtn.setOnClickListener { _ ->
            MainActivity.instance?.let {
            it.startActivity(Intent(it, TemplateEditActivity::class.java)) }}

        addNewBtn.setOnClickListener { addNew() }

        callAfterViewDrawed(this, Runnable{ restoreViews() })
    }

    @SuppressLint("SetTextI18n")
    private fun setDate(dtStart: Long, dtEnd: Long) {
        val folder = MainActivity.instance?.viewModel?.targetFolder?.value
        if(folder != null) {
            startDateText.text = folder.name
            endDateText.text = ""
        }else {
            startCal.timeInMillis = dtStart
            endCal.timeInMillis = dtEnd
            startDateText.text = AppDateFormat.mdeDate.format(startCal.time)
            if(isSameDay(startCal, endCal)) {
                endDateText.text = ""
            }else {
                endDateText.text = "  ~ ${AppDateFormat.mdeDate.format(endCal.time)}"
            }
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun addNew() {
        MainActivity.instance?.let { showDialog(TypePickerDialog(it, TimeObject.Type.EVENT) { type ->
                collapse()
                it.viewModel.makeNewTimeObject(type.ordinal)
            }, true, true, true, false)}
    }

    fun expand(dtStart: Long, dtEnd: Long) {
        setDate(dtStart, dtEnd)
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
        t1.addTarget(listLy)
        t2.addTarget(backgroundLy)
        t2.addTarget(dateLy)
        t2.addTarget(controlLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.setBackgroundColor(AppTheme.backgroundColor)
        backgroundLy.setOnClickListener { collapse() }
        backgroundLy.isClickable = true
        backgroundLy.visibility = View.VISIBLE
        dateLy.visibility = View.VISIBLE
        listLy.visibility = View.VISIBLE
        controlLy.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f).start()
        elevation = dpToPx(11f)
        isExpanded = true
    }

    fun collapse() {
        autoScrollFlag = 0
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.addTarget(listLy)
        t2.addTarget(backgroundLy)
        t2.addTarget(dateLy)
        t2.addTarget(controlLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        backgroundLy.visibility = View.GONE
        dateLy.visibility = View.GONE
        listLy.visibility = View.GONE
        controlLy.visibility = View.GONE
        ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f).start()
        elevation = dpToPx(0f)
        isExpanded = false
    }

    private fun collapseNoAnim() {
        autoScrollFlag = 0
        templateIconImg.rotation = 0f
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        backgroundLy.visibility = View.GONE
        dateLy.visibility = View.GONE
        listLy.visibility = View.GONE
        controlLy.visibility = View.GONE
        elevation = dpToPx(0f)
        isExpanded = false
    }

    fun notify(it: List<Template>) {
        l("[템플릿 뷰 갱신]")
        items.clear()
        items.addAll(it)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun selectItem(template: Template) {
        Prefs.putInt("last_template_id", template.id)
        selectedPosition = items.indexOf(template)
        MainActivity.instance?.viewModel?.targetTemplate?.value = template
    }

    private fun restoreViews() {

    }

    fun hideDecoBtn() {
        decoBtn.visibility = View.GONE
    }

    fun showDecoBtn() {
        decoBtn.visibility = View.GONE
    }
}