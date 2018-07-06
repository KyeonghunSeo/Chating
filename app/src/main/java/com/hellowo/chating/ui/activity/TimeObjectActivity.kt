package com.hellowo.chating.ui.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.chating.R
import com.hellowo.chating.calendar.TimeObject
import com.hellowo.chating.l
import com.hellowo.chating.viewmodel.TimeObjectViewModel
import kotlinx.android.synthetic.main.activity_test.*


class TimeObjectActivity : AppCompatActivity() {
    private lateinit var viewModel: TimeObjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        tap.setOnClickListener {
            val constraintSet = ConstraintSet()
            constraintSet.clone(this, R.layout.activity_test_detail)

            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(2.0f)
            transition.duration = 800
            transition.addListener(object : Transition.TransitionListener{
                override fun onTransitionEnd(transition: Transition) {
                    l("hohohohohoho")
                }
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {
                    l("hahahahaha")
                }
            })

            TransitionManager.beginDelayedTransition(constraint, transition)
            constraintSet.applyTo(constraint)
        }

        /*
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
        }*/
    }

    private fun updateUI(timeObject: TimeObject) {
        l(timeObject.toString())
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}