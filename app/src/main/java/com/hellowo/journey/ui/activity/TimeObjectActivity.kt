package com.hellowo.journey.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.hellowo.journey.R
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.l
import com.hellowo.journey.viewmodel.TimeObjectViewModel
import kotlinx.android.synthetic.main.activity_time_object.*


class TimeObjectActivity : AppCompatActivity() {
    private lateinit var viewModel: TimeObjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_object)
        viewModel = ViewModelProviders.of(this).get(TimeObjectViewModel::class.java)
        viewModel.init(intent)
        viewModel.editingTimeObjectLiveData.observe(this, Observer { it?.let { updateUI(it) }})

        titleInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { viewModel.setTitle(p0.toString()) }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

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