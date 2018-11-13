package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.hellowo.journey.R
import com.hellowo.journey.adapter.TemplateSelectAdapter
import com.hellowo.journey.model.Template
import com.hellowo.journey.ui.activity.MainActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.view_template_select.view.*

class BottomTemplateSelectView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    val layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    val items = ArrayList<Template>()
    var selectedPosition = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template_select, this, true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = TemplateSelectAdapter(context, items) {
            selectItem(it)
            MainActivity.instance?.viewModel?.makeNewTimeObject()
        }
    }

    fun notify(it: List<Template>) {
        items.clear()
        items.addAll(it)
        it.firstOrNull { it.id == Prefs.getInt("last_template_id", 0) }?.let {
            selectItem(it)
            recyclerView.scrollToPosition(selectedPosition)
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun selectItem(template: Template) {
        Prefs.putInt("last_template_id", template.id)
        selectedPosition = items.indexOf(template)
        MainActivity.instance?.viewModel?.targetTemplate?.value = template
    }
}