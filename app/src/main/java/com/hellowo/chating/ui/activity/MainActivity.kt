package com.hellowo.chating.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hellowo.chating.R
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import me.everything.android.ui.overscroll.IOverScrollState.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        /*
        titleText.setOnClickListener {
            EnterCommentDialog{
                viewModel.insert(it)
            }.showNow(supportFragmentManager, null)
        }

        titleText.setOnLongClickListener {
            SyncUser.current()?.logOut()
            finish()
            return@setOnLongClickListener false
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChatRoomAdapter(this, viewModel.loadChatRoom()!!){}
        */
        val decor = OverScrollDecoratorHelper.setUpOverScroll(scrollView)
        decor.setOverScrollStateListener{ decor, oldState, newState ->
            when (newState) {
                STATE_IDLE -> {
                    Log.d("aaa", "STATE_IDLE")
                }
                STATE_DRAG_START_SIDE -> {
                    Log.d("aaa", "STATE_DRAG_START_SIDE")
                }
                STATE_DRAG_END_SIDE -> {
                    Log.d("aaa", "STATE_DRAG_END_SIDE")
                }
                STATE_BOUNCE_BACK -> {
                    if (oldState == STATE_DRAG_START_SIDE) {
                        Log.d("aaa", "STATE_BOUNCE_BACK -> STATE_DRAG_START_SIDE")
                    } else { // i.e. (oldState == STATE_DRAG_END_SIDE)
                        Log.d("aaa", "STATE_BOUNCE_BACK -> STATE_DRAG_END_SIDE")
                    }
                }
            }
        }

        decor.setOverScrollUpdateListener { decor, state, offset ->
            val view = decor.view
            if (offset > 0) {
                // 'view' is currently being over-scrolled from the top.
            } else if (offset < 0) {
                // 'view' is currently being over-scrolled from the bottom.
            } else {

            }
        }
    }
}