package com.hellowo.chating.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.hellowo.chating.R
import com.hellowo.chating.ui.adapter.ChatRoomAdapter
import com.hellowo.chating.viewmodel.MainViewModel
import com.hellowo.colosseum.ui.dialog.EnterCommentDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        titleText.setOnClickListener {
            EnterCommentDialog{
                viewModel.insert(it)
            }.showNow(supportFragmentManager, null)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChatRoomAdapter(this, viewModel.loadChatRoom()!!){

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }
}
