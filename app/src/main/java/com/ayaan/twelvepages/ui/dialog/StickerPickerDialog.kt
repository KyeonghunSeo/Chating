package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.StickerManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.BaseActivity
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_sticker_picker.*
import kotlinx.android.synthetic.main.dialog_sticker_picker.view.*
import kotlinx.android.synthetic.main.list_item_sticker_picker_tab.view.*
import kotlinx.android.synthetic.main.pager_item_sticker_picker.*
import kotlinx.android.synthetic.main.pager_item_sticker_picker.view.*
import java.util.*
import kotlin.collections.ArrayList


class StickerPickerDialog(private val record: Record? = null,
                          private val onResult: (StickerManager.Sticker, Int) -> Unit) : BottomSheetDialog() {
    var currentPack: StickerManager.StickerPack? = null
    var stickerPosition = record?.getStickerLink()?.intParam1 ?: 0
    var isConfirm = record != null

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_sticker_picker)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheetBehavior.isHideable = false
        setLayout()
        dialog.setOnShowListener {
            if(AppStatus.isPremium()) {
                root.adView.visibility = View.GONE
            }else {
                root.adView.visibility = View.VISIBLE
                val adRequest = AdRequest.Builder().build()
                root.adView.loadAd(adRequest)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(isConfirm) {
            record?.getSticker()?.let { onResult.invoke(it, stickerPosition) }
        }
    }

    private fun setLayout() {
        initPositionBtns()
        setViewPager()
        setTab()
        record?.getStickerLink()?.let {
            root.viewPager.setCurrentItem(StickerManager.getPackIndex(it.intParam0), false)
        }
        root.rootLy.setOnClickListener { dismiss() }
        root.positionLy.setOnClickListener {}
        root.settingBtn.setOnClickListener {
            showDialog(EditStickerPackDialog(activity as BaseActivity) { result ->
                if(result) {
                    StickerManager.saveCurrentPack()
                    setLayout()
                    toast(R.string.long_tab_to_move)
                }
            }, true, true, true, false)
        }
    }

    private fun initPositionBtns() {
        val btns = arrayOf(root.positionBtn0, root.positionBtn1, root.positionBtn2, root.positionBtn3, root.positionBtn4)
        btns.forEachIndexed { index, btn ->
            btn?.setOnClickListener {
                stickerPosition = index
                setPositionBtns()
            }
        }
        setPositionBtns()
    }

    private fun setPositionBtns() {
        val btns = arrayOf(root.positionBtn0, root.positionBtn1, root.positionBtn2, root.positionBtn3, root.positionBtn4)
        btns.forEachIndexed { index, btn ->
            if(index == stickerPosition) {
                btn?.alpha = 1f
            }else {
                btn?.alpha = 0.15f
            }
        }
    }

    private fun setViewPager() {
        root.viewPager.adapter = StickerPagerAdapter()
        root.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                if(root.viewPager.adapter?.count == 2 || root.viewPager.adapter?.count == 1) {
                    root.viewPager.getChildAt(0)?.findViewById<NestedScrollView>(R.id.scrollView)?.isNestedScrollingEnabled = false
                    root.viewPager.getChildAt(1)?.findViewById<NestedScrollView>(R.id.scrollView)?.isNestedScrollingEnabled = false
                    root.viewPager.getChildAt(position)?.findViewById<NestedScrollView>(R.id.scrollView)?.isNestedScrollingEnabled = true
                }

                currentPack = if(position == 0) {
                    null
                }else {
                    StickerManager.packs[position - 1]
                }
                root.recyclerView.scrollToPosition(position)
                root.recyclerView.adapter?.notifyDataSetChanged()
            }
        })
    }

    private fun setTab() {
        root.recyclerView.setBackgroundColor(AppTheme.background)
        root.recyclerView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        root.recyclerView.adapter = TabAdapter()
    }

    inner class StickerPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = LayoutInflater.from(context).inflate(R.layout.pager_item_sticker_picker, null, false)
            if(position == 0) {
                val recentPack = StickerManager.recentPack
                if(recentPack.isEmpty()) {
                    v.emptyLy.visibility = View.VISIBLE
                    v.stickerPackGridView.visibility = View.GONE
                }else {
                    v.emptyLy.visibility = View.GONE
                    v.stickerPackGridView.visibility = View.VISIBLE
                    v.stickerPackGridView.layoutManager = GridLayoutManager(context, 5)
                    v.stickerPackGridView.adapter = StickerAdapter(Array(recentPack.size) { i -> recentPack[i] })
                }
            }else {
                v.emptyLy.visibility = View.GONE
                val stickerPack = StickerManager.packs[position - 1]
                v.stickerPackGridView.layoutManager = GridLayoutManager(context, 5)
                v.stickerPackGridView.adapter = StickerAdapter(stickerPack.items)
            }
            container.addView(v)
            return v
        }

        inner class StickerAdapter(val items: Array<StickerManager.Sticker>) : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

            override fun getItemCount(): Int = items.size

            inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

            override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                    = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_sticker, parent, false))

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val sticker = items[position]
                val v = holder.itemView
                Glide.with(context!!).load(sticker.resId).into(v.findViewById(R.id.imageView))
                v.setOnClickListener {
                    StickerManager.updateRecentSticker(sticker)
                    isConfirm = false
                    onResult.invoke(sticker, stickerPosition)
                    dismiss()
                }
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { container.removeView(`object` as View) }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = StickerManager.packs.size + 1
        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)
            for (i in 0 until count) {
                container.getChildAt(i)?.findViewById<NestedScrollView>(R.id.scrollView)?.isNestedScrollingEnabled = false
            }
            (`object` as View).findViewById<NestedScrollView>(R.id.scrollView)?.isNestedScrollingEnabled = true
        }
    }

    inner class TabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var itemTouchHelper: ItemTouchHelper? = null
        var dragPack: StickerManager.StickerPack? = null

        init {
            val callback = SimpleItemTouchHelperCallback(this)
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper?.attachToRecyclerView(root.recyclerView)
        }

        override fun getItemCount(): Int = StickerManager.packs.size + 1

        inner class ViewHolder(val container: View) : RecyclerView.ViewHolder(container) {
            init { onItemClear() }
            fun onItemSelected() {}
            fun onItemClear() {
                container.postDelayed({
                    (container as CardView).cardElevation = dpToPx(0f)
                }, 100)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_sticker_picker_tab, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView

            if(position == 0) {
                val p = dpToPx(10)
                v.iconImg.setPadding(p,p,p,p)
                v.iconImg.setImageResource(R.drawable.recent)
                v.setOnClickListener { root.viewPager.currentItem = 0 }
                v.setOnLongClickListener(null)
            }else {
                val stickerPack = StickerManager.packs[position - 1]
                val p = dpToPx(7)
                v.iconImg.setPadding(p,p,p,p)
                v.iconImg.setImageResource(stickerPack.items[0].resId)
                v.setOnClickListener { root.viewPager.currentItem = StickerManager.packs.indexOf(stickerPack) + 1 }
                v.setOnLongClickListener {
                    root.settingBtn.setImageResource(R.drawable.delete)
                    root.settingBtn.setColorFilter(AppTheme.red)
                    dragPack = stickerPack
                    itemTouchHelper?.startDrag(holder)
                    return@setOnLongClickListener true
                }
            }

            if(position == root.viewPager.currentItem) {
                v.iconImg.setBackgroundColor(AppTheme.lightLine)
                if(position == 0) {
                    v.iconImg.setColorFilter(AppTheme.blue)
                    v.iconImg.alpha = 1f
                }else {
                    removeImageViewFilter(v.iconImg)
                    v.iconImg.alpha = 1f
                }
            }else {
                v.iconImg.setBackgroundColor(Color.TRANSPARENT)
                if(position == 0) {
                    v.iconImg.setColorFilter(AppTheme.disableText)
                    v.iconImg.alpha = 1f
                }else {
                    setImageViewGrayFilter(v.iconImg)
                    v.iconImg.alpha = 0.5f
                }
            }
        }

        private fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            if(toPosition > 0) {
                Collections.swap(StickerManager.packs, fromPosition - 1, toPosition - 1)
                notifyItemMoved(fromPosition, toPosition)
                return true
            }
            return false
        }

        inner class SimpleItemTouchHelperCallback(private val mAdapter: TabAdapter) : ItemTouchHelper.Callback() {
            private val dragYOffset = -dpToPx(40)

            override fun isLongPressDragEnabled(): Boolean = false
            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            private var isDeleted = false

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                // We only want the active item to change
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder is ViewHolder) {
                        // Let the view holder know that this item is being moved or dragged
                        val itemViewHolder = viewHolder as ViewHolder?
                        itemViewHolder?.onItemSelected()
                    }
                }else if(actionState == ItemTouchHelper.ACTION_STATE_IDLE){
                    if(isDeleted) {
                        if(StickerManager.packs.size > 1) {
                            dragPack?.let { StickerManager.deletePack(it) }
                            setTab()
                            toast(R.string.deleted, R.drawable.delete)
                        }else {
                            toast(R.string.cant_deleted_last_one, R.drawable.info)
                        }
                    }
                    StickerManager.saveCurrentPack()
                    setViewPager()
                    currentPack?.let { root.viewPager.setCurrentItem(StickerManager.packs.indexOf(it) + 1, false) }
                    isDeleted = false
                    dragPack = null
                    root.settingBtn.setImageResource(R.drawable.setting)
                    root.settingBtn.setBackgroundResource(AppTheme.selectableItemBackground)
                    root.settingBtn.setColorFilter(AppTheme.disableText)
                }

                super.onSelectedChanged(viewHolder, actionState)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                viewHolder.itemView.alpha = 1f
                (viewHolder.itemView as CardView).cardElevation = dpToPx(10f)
                super.onChildDraw(c, recyclerView, viewHolder,
                        dX, dY + if(isCurrentlyActive) dragYOffset else 0, actionState, isCurrentlyActive)

                val location = IntArray(2)
                viewHolder.itemView.getLocationInWindow(location)
                val x = location[0] + viewHolder.itemView.width
                val y = location[1] + viewHolder.itemView.height
                val deleteLocation = IntArray(2)
                root.settingBtn.getLocationInWindow(deleteLocation)
                isDeleted = if(x > deleteLocation[0] && y > deleteLocation[1] + dragYOffset) {
                    root.settingBtn.setBackgroundColor(AppTheme.lightLine)
                    true
                }else {
                    root.settingBtn.setBackgroundResource(AppTheme.selectableItemBackground)
                    false
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1f
                (viewHolder as? ViewHolder)?.onItemClear()
            }
        }
    }

}
