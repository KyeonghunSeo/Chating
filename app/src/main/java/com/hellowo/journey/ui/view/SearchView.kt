package com.hellowo.journey.ui.view

import android.animation.LayoutTransition
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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.adapter.EventListAdapter
import com.hellowo.journey.adapter.TimeObjectListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.Tag
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.dialog.TagDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_saerch.view.*

class SearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val items = ArrayList<TimeObject>()
    private val newItmes = ArrayList<TimeObject>()
    private val adapter = TimeObjectListAdapter(context, items) { view, timeObject, action ->
        when(action) {
            0 -> {
                MainActivity.instance?.viewModel?.let {
                    it.targetTimeObject.value = timeObject
                    it.targetView.value = view
                }
            }
        }
    }
    private val tags = ArrayList<Tag>()

    init {
        LayoutInflater.from(context).inflate(R.layout.view_saerch, this, true)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        searchBtn.setOnClickListener {
            show()
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notifyDataChanged()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        searchInput.isFocusableInTouchMode = false
        searchInput.clearFocus()
        searchInput.setOnClickListener {
            searchInput.isFocusableInTouchMode = true
            if (searchInput.requestFocus()) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(searchInput, 0)
            }
        }
    }

    fun notifyDataChanged() {
        if(searchInput.text.toString().isNotEmpty()) {
            timeObjectList?.removeAllChangeListeners()
            timeObjectList = TimeObjectManager.getTimeObjectList(searchInput.text.toString(), tags)
            timeObjectList?.addChangeListener { result, changeSet ->
                l("==========서치뷰 데이터 변경 시작=========")
                val t = System.currentTimeMillis()
                if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    items.clear()
                    result.forEach { items.add(it.makeCopyObject()) }
                    adapter.notifyDataSetChanged()
                }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                    newItmes.clear()
                    result.forEach { newItmes.add(it.makeCopyObject()) }
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

    fun showTagDialog() {
        showDialog(TagDialog(MainActivity.instance!!, tags) {
            tags.clear()
            tags.addAll(it)
            notifyDataChanged()
        }, true, true, true, false)
    }

    fun show() {
        viewMode = ViewMode.ANIMATING
        val transiion = makeChangeBounceTransition()
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                viewMode = ViewMode.OPENED
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) { notifyDataChanged() }
        })
        TransitionManager.beginDelayedTransition(this@SearchView, transiion)
        layoutParams.height = MATCH_PARENT
        layoutParams.width = MATCH_PARENT
        requestLayout()
    }

    fun hide() {
        timeObjectList?.removeAllChangeListeners()
        viewMode = ViewMode.ANIMATING
        val transiion = makeChangeBounceTransition()
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                viewMode = ViewMode.CLOSED
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                searchInput.setText("")
                hideKeyPad(windowToken, searchInput)
            }
        })
        TransitionManager.beginDelayedTransition(this, transiion)
        layoutParams.height = dpToPx(50)
        layoutParams.width = dpToPx(50)
        requestLayout()
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}