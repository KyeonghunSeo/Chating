package com.ayaan.twelvepages.ui.dialog

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.ui.activity.BaseActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_color_picker.view.*
import kotlinx.android.synthetic.main.list_item_color_picker_tab.view.*
import kotlinx.android.synthetic.main.pager_item_color_picker.view.*
import java.util.*


class ColorPickerDialog(private val selectedColorKey: Int, private val onResult: (Int) -> Unit) : BottomSheetDialog() {
    var isCoverOpened = false
    var currentPack: ColorManager.ColorPack? = null

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_color_picker)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheetBehavior.isHideable = false
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        setViewPager()
        setTab()
        root.viewPager.setCurrentItem(ColorManager.getPackIndex(selectedColorKey), false)
        root.rootLy.setOnClickListener { dismiss() }
        root.settingBtn.setOnClickListener {
            showDialog(EditColorPackDialog(activity as BaseActivity) { result ->
                if(result) {
                    ColorManager.saveCurrentPack()
                    setLayout()
                    toast(R.string.long_tab_to_move)
                }
            }, true, true, true, false)
        }
    }

    private fun setViewPager() {
        val pagerAdapter = ColorPagerAdapter()
        root.viewPager.adapter = pagerAdapter
        root.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
                if(!isCoverOpened) {
                    isCoverOpened = true
                    TransitionManager.beginDelayedTransition(root.viewPagerContainer, makeChangeBounceTransition())
                    for (i in 0 until pagerAdapter.count) {
                        pagerAdapter.container?.getChildAt(i)?.coverLy?.visibility = View.VISIBLE
                    }
                    (root.viewPagerContainer.layoutParams as FrameLayout.LayoutParams).let {
                        it.height += dpToPx(220)
                        it.topMargin = dpToPx(20)
                    }
                    root.viewPagerContainer.requestLayout()
                }
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                currentPack = if(position == 0) {
                    null
                }else {
                    ColorManager.packs[position - 1]
                }
                root.recyclerView.scrollToPosition(position)
                root.recyclerView.adapter?.notifyDataSetChanged()
            }
        })
        root.viewPager.setPageTransformer(true) { view, position ->
            val pageWidth = view.width
            when {
                position > -1 && position < 0 -> {
                    view.coverLy.translationX = pageWidth * position * -0.4f
                    //view.packLy.translationX = pageWidth * position * -0.8f
                }
                position >= 0 -> {
                    view.coverLy.translationX = pageWidth * position * -0.4f
                    //view.packLy.translationX = pageWidth * position * -0.8f
                }
                else -> {
                    view.coverLy.translationX = 0f
                    //view.packLy.translationX = 0f
                }
            }
        }
    }

    private fun setTab() {
        root.recyclerView.setBackgroundColor(AppTheme.background)
        root.recyclerView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        root.recyclerView.adapter = TabAdapter()
    }

    inner class ColorPagerAdapter : PagerAdapter() {
        var container: ViewGroup? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            this.container = container
            val v = LayoutInflater.from(context).inflate(R.layout.pager_item_color_picker, null, false)
            val stickerImgs = arrayOf(
                    v.colorImg0, v.colorImg1, v.colorImg2, v.colorImg3, v.colorImg4,
                    v.colorImg5, v.colorImg6, v.colorImg7, v.colorImg8, v.colorImg9)
            setGlobalTheme(v)

            v.packLy.setBackgroundColor(AppTheme.background)
            if(isCoverOpened) {
                v.coverLy.visibility = View.VISIBLE
            }else {
                v.coverLy.visibility = View.GONE
            }

            if(position == 0) {
                v.titleText.text = str(R.string.recent_color_pack)
                Glide.with(context!!).load(R.drawable.color_palette_recent).into(v.coverImg)
                val recentPack = ColorManager.recentPack
                v.emptyLy.visibility = View.GONE
                stickerImgs.forEachIndexed { index, imageView ->
                    if(index < recentPack.size) {
                        val colorKey = recentPack[index]
                        setColorBtn(imageView, colorKey, ColorManager.getColor(colorKey))
                    }else {
                        setColorBtn(imageView, 9, AppTheme.background)
                    }
                }
            }else {
                val colorPack = ColorManager.packs[position - 1]
                v.titleText.text = str(colorPack.titleId)
                Glide.with(context!!).load(colorPack.coverImgId).into(v.coverImg)
                stickerImgs.forEachIndexed { index, imageView ->
                    val colorKey = colorPack.ordinal * ColorManager.colorPaletteSize + index
                    setColorBtn(imageView, colorKey, colorPack.items[index])
                }
            }
            container.addView(v)
            return v
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { container.removeView(`object` as View) }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = ColorManager.packs.size + 1

        private fun setColorBtn(v: ImageView, colorKey: Int, color: Int) {
            if(colorKey == selectedColorKey) {
                v.setImageResource(R.drawable.selected_color)
            }else {
                v.setImageResource(R.drawable.normal_rect_fill)
            }
            v.setColorFilter(color)
            v.setOnClickListener {
                ColorManager.updateRecentColor(colorKey)
                onResult.invoke(colorKey)
                dismiss()
            }
        }
    }

    inner class TabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var itemTouchHelper: ItemTouchHelper? = null
        var dragPack: ColorManager.ColorPack? = null

        init {
            val callback = SimpleItemTouchHelperCallback(this)
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper?.attachToRecyclerView(root.recyclerView)
        }

        override fun getItemCount(): Int = ColorManager.packs.size + 1

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
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_color_picker_tab, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView

            if(position == 0) {
                val p = dpToPx(10)
                v.iconImg.setPadding(p,p,p,p)
                v.iconImg.setImageResource(R.drawable.recent)
                v.setOnClickListener { root.viewPager.currentItem = 0 }
                v.setOnLongClickListener(null)
            }else {
                val colorPack = ColorManager.packs[position - 1]
                val p = dpToPx(7)
                v.iconImg.setPadding(p,p,p,p)
                Glide.with(this@ColorPickerDialog).load(colorPack.coverImgId).into(v.iconImg)
                v.setOnClickListener { root.viewPager.currentItem = ColorManager.packs.indexOf(colorPack) + 1 }
                v.setOnLongClickListener {
                    root.settingBtn.setImageResource(R.drawable.delete)
                    root.settingBtn.setColorFilter(AppTheme.red)
                    dragPack = colorPack
                    itemTouchHelper?.startDrag(holder)
                    return@setOnLongClickListener true
                }
            }

            if(position == root.viewPager.currentItem) {
                if(position == 0) {
                    v.iconImg.setBackgroundColor(AppTheme.lightLine)
                    v.iconImg.setColorFilter(AppTheme.blue)
                    v.iconImg.alpha = 1f
                }else {
                    removeImageViewFilter(v.iconImg)
                    v.iconImg.alpha = 1f
                }
            }else {
                if(position == 0) {
                    v.iconImg.setBackgroundColor(Color.TRANSPARENT)
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
                Collections.swap(ColorManager.packs, fromPosition - 1, toPosition - 1)
                notifyItemMoved(fromPosition, toPosition)
                return true
            }
            return false
        }

        inner class SimpleItemTouchHelperCallback(private val mAdapter: TabAdapter) : ItemTouchHelper.Callback() {
            var dragYOffset = -dpToPx(40)

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
                        if(ColorManager.packs.size > 1) {
                            dragPack?.let { ColorManager.deletePack(it) }
                            setTab()
                            toast(R.string.deleted, R.drawable.delete)
                        }else {
                            toast(R.string.cant_deleted_last_one, R.drawable.info)
                        }
                    }
                    ColorManager.saveCurrentPack()
                    setViewPager()
                    currentPack?.let { root.viewPager.setCurrentItem(ColorManager.packs.indexOf(it) + 1, false) }
                    isDeleted = false
                    dragPack = null
                    root.settingBtn.setImageResource(R.drawable.pin)
                    root.settingBtn.setBackgroundResource(AppTheme.selectableItemBackground)
                    root.settingBtn.setColorFilter(AppTheme.disableText)
                }

                super.onSelectedChanged(viewHolder, actionState)
            }

            private var isDeleted = false

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
