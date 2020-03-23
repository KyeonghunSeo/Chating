package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.ui.activity.MainActivity
import kotlinx.android.synthetic.main.dialog_popup_option.*
import kotlinx.android.synthetic.main.list_item_popup_option.view.*


class PopupOptionDialog(activity: Activity, private val items: Array<Item>, private val targetView: View,
                        private val isFitTargetView: Boolean, private val onResult: (Int) -> Unit) : Dialog(activity) {
    private val buttonSize = dpToPx(45)

    class Item(val title: String, val icon: Int = Int.MIN_VALUE, val color: Int = AppTheme.secondaryText,
               val subText: String = "", var isActive: Boolean = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogFadeAnimation
        setContentView(R.layout.dialog_popup_option)
        setLayout()
        setOnShowListener {
            rootLy.postDelayed({ expand() }, 1)
        }
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        rootLy.layoutParams.width = MainActivity.getMainPanel()?.width ?: 0
        rootLy.layoutParams.height = MainActivity.getMainPanel()?.height ?: 0
        rootLy.setOnClickListener { dismiss() }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ItemAdapter()
        recyclerView.visibility = View.GONE

        val location = IntArray(2)
        targetView.getLocationOnScreen(location)

        val w = MainActivity.getMainPanel()?.width ?: 0
        val h = MainActivity.getMainPanel()?.height ?: 0
        val left = location[0]
        var top = location[1] - AppStatus.statusBarHeight
        if(top <= 0) top = dpToPx(5)

        (contentLy.layoutParams as FrameLayout.LayoutParams).let {
            if(left < w/2) {
                if(top + buttonSize * items.size < h) {
                    it.topMargin = top
                    it.leftMargin = left
                    it.gravity = Gravity.LEFT
                }else {
                    it.leftMargin = left
                    it.bottomMargin = buttonSize / 8
                    it.gravity = Gravity.BOTTOM or Gravity.LEFT
                }
            }else {
                if(top + buttonSize * items.size < h) {
                    it.topMargin = top
                    it.rightMargin = buttonSize / 2
                    it.gravity = Gravity.RIGHT
                }else {
                    it.rightMargin = buttonSize / 2
                    it.bottomMargin = buttonSize / 8
                    it.gravity = Gravity.BOTTOM or Gravity.RIGHT
                }
            }

        }

        if(isFitTargetView) {
            (recyclerView.layoutParams as FrameLayout.LayoutParams).width = targetView.width
        }
    }

    private fun expand() {
        val transition = ChangeBounds()
        transition.duration = 150L
        TransitionManager.beginDelayedTransition(contentLy, transition)
        recyclerView.visibility = View.VISIBLE
    }

    inner class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_popup_option, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView

            if(item.icon == Int.MIN_VALUE) {
                v.iconImg.visibility = View.GONE
            }else {
                v.iconImg.visibility = View.VISIBLE
                v.iconImg.setImageResource(item.icon)
                v.iconImg.setColorFilter(item.color)
            }

            v.titleText.text = item.title
            if(item.subText.isNotEmpty()) {
                v.subText.text = item.subText
            }else {
                v.subText.visibility = View.GONE
            }
            v.titleText.setTextColor(item.color)

            if(item.isActive) {
                v.contentLy.alpha = 1f
                v.setOnClickListener {
                    onResult.invoke(position)
                    dismiss()
                }
            }else {
                v.contentLy.alpha = 0.4f
                v.setOnClickListener (null)
            }
        }
    }

}
