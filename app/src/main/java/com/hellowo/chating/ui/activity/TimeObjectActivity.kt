package com.hellowo.chating.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellowo.chating.R
import com.hellowo.colosseum.viewmodel.TimeObjectViewModel


class TimeObjectActivity : AppCompatActivity() {
    private lateinit var viewModel: TimeObjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_object)
        viewModel = ViewModelProviders.of(this).get(TimeObjectViewModel::class.java)
    }
}