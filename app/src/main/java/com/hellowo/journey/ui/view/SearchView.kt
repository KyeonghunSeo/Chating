package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import androidx.transition.*
import com.hellowo.journey.*
import com.hellowo.journey.R
import com.hellowo.journey.adapter.RecordListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.manager.RecordManager
import com.hellowo.journey.model.Tag
import com.hellowo.journey.model.Record
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.dialog.TagDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_saerch.view.*
import java.util.*

class SearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED
    private var recordList: RealmResults<Record>? = null
    private val items = ArrayList<Record>()
    private val newItmes = ArrayList<Record>()
    private val adapter = RecordListAdapter(context, items, Calendar.getInstance()) { view, timeObject, action ->
        when(action) {
            0 -> {
                MainActivity.instance?.viewModel?.let {
                    it.targetTimeObject.value = timeObject
                }
            }
        }
    }
    private val tags = ArrayList<Tag>()

    init {
        LayoutInflater.from(context).inflate(R.layout.view_saerch, this, true)
        //recyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(searchInput.text.isNotEmpty()) {
                    clearBtn.visibility = View.VISIBLE
                }else {
                    clearBtn.visibility = View.GONE
                }
                notifyDataChanged()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        tagInput.setOnClickListener {
            showDialog(TagDialog(MainActivity.instance!!, tags) {
                if(tags.isNotEmpty()) {
                    tagInput.text = tags.joinToString("") { "#${it.id}" }
                }else {
                    tagInput.text = ""
                }
                notifyDataChanged()
            }, true, true, true, false)
        }

        clearBtn.setOnClickListener { clear() }
        clearBtn.visibility = View.GONE
        setOnClickListener {}
    }

    fun notifyDataChanged() {
        if(searchInput.text.toString().isNotEmpty() || tags.isNotEmpty()) {
            recordList?.removeAllChangeListeners()
            recordList = RecordManager.getRecordList(searchInput.text.toString(), tags)
            recordList?.addChangeListener { result, changeSet ->
                l("==========서치뷰 데이터 변경 시작=========")
                val t = System.currentTimeMillis()
                adapter.query = searchInput.text.toString()
                if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    items.clear()
                    result.forEach { items.add(it.makeCopyObject()) }
                    if(tags.isEmpty()) {
                        OsCalendarManager.searchEvents(context, searchInput.text.toString()).forEach { items.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                    newItmes.clear()
                    result.forEach { newItmes.add(it.makeCopyObject()) }
                    if(tags.isEmpty()) {
                        OsCalendarManager.searchEvents(context, searchInput.text.toString()).forEach { items.add(it) }
                    }
                    Thread {
                        val diffResult = DiffUtil.calculateDiff(ListDiffCallback(items, newItmes))
                        items.clear()
                        items.addAll(newItmes)
                        Handler(Looper.getMainLooper()).post{
                            diffResult.dispatchUpdatesTo(adapter)
                        }
                    }.start()
                }
                l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
                l("==========서치뷰 데이터 변경 종료=========")
            }
        }else {
            items.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private fun clear() {
        tags.clear()
        tagInput.text = ""
        searchInput.setText("")
    }

    fun show() {
        visibility = View.VISIBLE
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(headerLy, "elevation", 0f, dpToPx(10f)))
        animSet.duration = 150
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                val transitionSet = TransitionSet()
                val t1 = makeChangeBounceTransition()
                val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
                t1.setPathMotion(ArcMotion())
                t1.addTarget(headerLy)
                t2.addTarget(backgroundLy)
                transitionSet.addTransition(t1)
                transitionSet.addTransition(t2)
                transitionSet.addListener(object : TransitionListenerAdapter(){
                    override fun onTransitionEnd(transition: Transition) {
                        showKeyPad(searchInput)
                    }
                })
                TransitionManager.beginDelayedTransition(this@SearchView, transitionSet)
                backgroundLy.visibility = View.VISIBLE
                (headerLy.layoutParams as FrameLayout.LayoutParams).let {
                    it.width = MATCH_PARENT
                    it.height = dpToPx(80)
                    it.setMargins(0, 0, 0, 0)
                }
                headerLy.requestLayout()
            }
        })
        animSet.start()
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        clear()
        val transitionSet = TransitionSet()
        val t1 = makeChangeBounceTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.setPathMotion(ArcMotion())
        t1.addTarget(headerLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.addListener(object : TransitionListenerAdapter(){
            override fun onTransitionEnd(transition: Transition) {
                val animSet = AnimatorSet()
                animSet.playTogether(
                        ObjectAnimator.ofFloat(headerLy, "elevation", 10f, dpToPx(0f)))
                animSet.duration = 150
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator?) {
                        visibility = View.GONE
                    }
                })
                animSet.start()
            }
        })
        TransitionManager.beginDelayedTransition(this@SearchView, transitionSet)
        backgroundLy.visibility = View.GONE
        (headerLy.layoutParams as FrameLayout.LayoutParams).let {
            it.width = dpToPx(50)
            it.height = dpToPx(50)
            it.setMargins(dpToPx(10), dpToPx(10), 0, 0)
        }
        headerLy.requestLayout()
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}