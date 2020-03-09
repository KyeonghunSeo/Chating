package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.StickerManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_sticker_picker.view.*
import kotlinx.android.synthetic.main.list_item_sticker_picker_tab.view.*
import kotlinx.android.synthetic.main.pager_item_sticker_picker.view.*
import java.util.*


class StickerPickerDialog(private val onResult: (StickerManager.Sticker) -> Unit) : BottomSheetDialog() {
    var currentPack: StickerManager.StickerPack? = null

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_sticker_picker)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheetBehavior.isHideable = false
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        setViewPager()
        setTab()
        root.rootLy.setOnClickListener { dismiss() }
        root.settingBtn.setOnClickListener {
            showDialog(EditStickerPackDialog(activity as Activity) { result ->
                if(result) {
                    StickerManager.saveCurrentPack()
                    setLayout()
                    toast(R.string.long_tab_to_move)
                }
            }, true, true, true, false)
        }
    }

    private fun setViewPager() {
        root.viewPager.adapter = StickerPagerAdapter()
        root.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
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
            val stickerImgs = arrayOf(
                    v.colorImg0, v.colorImg1, v.colorImg2, v.colorImg3,
                    v.colorImg4, v.colorImg5, v.colorImg6, v.colorImg7,
                    v.colorImg8, v.colorImg9, v.colorImg10, v.colorImg11,
                    v.colorImg12, v.colorImg13, v.colorImg14, v.colorImg15,
                    v.colorImg16, v.colorImg17, v.colorImg18, v.colorImg19)
            if(position == 0) {
                val recentPack = StickerManager.recentPack
                if(recentPack.isEmpty()) {
                    v.emptyLy.visibility = View.VISIBLE
                    v.contentLy.visibility = View.GONE
                }else {
                    v.emptyLy.visibility = View.GONE
                    v.contentLy.visibility = View.VISIBLE
                    setStickerImage(stickerImgs, recentPack)
                }
            }else {
                v.emptyLy.visibility = View.GONE
                v.contentLy.visibility = View.VISIBLE
                val stickerPack = StickerManager.packs[position - 1]
                setStickerImage(stickerImgs, stickerPack.items.toList())
            }
            container.addView(v)
            return v
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

        private fun setStickerImage(stickerImgs: Array<ImageView>, items: List<StickerManager.Sticker>) {
            stickerImgs.forEachIndexed { index, view ->
                if(index < items.size) {
                    val sticker = items[index]
                    view.visibility = View.VISIBLE
                    Glide.with(context!!).load(sticker.resId).into(view)
                    view.setOnClickListener {
                        StickerManager.updateRecentSticker(sticker)
                        onResult.invoke(sticker)
                        dismiss()
                    }
                }else {
                    view.visibility = View.INVISIBLE
                }
            }
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
            fun onItemClear() { (container as CardView).cardElevation = dpToPx(0f) }
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
                val p = dpToPx(5)
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
                    v.iconImg.setColorFilter(AppTheme.primary)
                    v.iconImg.alpha = 1f
                }else {
                    removeImageViewFilter(v.iconImg)
                }
            }else {
                v.iconImg.setBackgroundColor(Color.TRANSPARENT)
                if(position == 0) {
                    v.iconImg.setColorFilter(AppTheme.disableText)
                    v.iconImg.alpha = 1f
                }else {
                    setImageViewGrayFilter(v.iconImg)
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
                    root.viewPager.setCurrentItem(StickerManager.packs.indexOf(currentPack) + 1, false)
                    isDeleted = false
                    dragPack = null
                    root.settingBtn.setImageResource(R.drawable.setting)
                    root.settingBtn.setBackgroundResource(AppTheme.selectableItemBackground)
                    root.settingBtn.setColorFilter(AppTheme.secondaryText)
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
