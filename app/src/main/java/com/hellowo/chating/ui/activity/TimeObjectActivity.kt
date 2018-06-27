package com.hellowo.chating.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellowo.chating.R
import com.hellowo.chating.calendar.TimeObject
import com.hellowo.chating.l
import com.hellowo.chating.viewmodel.TimeObjectViewModel
import kotlinx.android.synthetic.main.activity_time_object.*


class TimeObjectActivity : AppCompatActivity() {
    private lateinit var viewModel: TimeObjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_object)
        viewModel = ViewModelProviders.of(this).get(TimeObjectViewModel::class.java)
        viewModel.init(intent)
        viewModel.editingTimeObjectLiveData.observe(this, Observer { it?.let { updateUI(it) }})

        confirmBtn.setOnClickListener {
            viewModel.save()
            finish()
        }
    }

    private fun updateUI(timeObject: TimeObject) {
        l(timeObject.toString())
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}