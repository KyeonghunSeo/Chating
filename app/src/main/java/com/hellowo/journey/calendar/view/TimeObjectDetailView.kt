package com.hellowo.journey.calendar.view

import android.animation.LayoutTransition
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hellowo.journey.*
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.calendar.dialog.AddMoreOptionDialog
import com.hellowo.journey.calendar.dialog.ColorPickerDialog
import com.hellowo.journey.calendar.dialog.DateTimePickerDialog
import com.hellowo.journey.calendar.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.MapActivity
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*


class TimeObjectDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {

    }

    private var originalData: TimeObject? = null
    private val timeObject: TimeObject = TimeObject()
    private var googleMap: GoogleMap? = null
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_timeobject_detail, this, true)
        contentPanel.visibility = View.INVISIBLE
        contentLy.setOnClickListener {}

        confirmBtn.setOnClickListener {
            confirm()
            MainActivity.instance?.viewModel?.clearTargetTimeObject()
        }

        deleteBtn.setOnClickListener {
            originalData?.let { TimeObjectManager.delete(it) }
            MainActivity.instance?.viewModel?.clearTargetTimeObject()
        }

        addOptionBtn.setOnClickListener {
            AddMoreOptionDialog(this@TimeObjectDetailView).show(MainActivity.instance?.supportFragmentManager, null)
        }

        initControllBtn()
        initType()
        initTitle()
        initDateTime()
    }

    private fun initControllBtn() {
        colorBtn.setOnClickListener {
            showDialog(ColorPickerDialog(MainActivity.instance!!, timeObject.color) { color, fontColor ->
                timeObject.color = color
                timeObject.fontColor = fontColor
                updateUI()
            }, true, true, true, false)
        }
    }

    private fun initType() {

    }

    private fun initTitle() {
        titleInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
            }
            return@setOnEditorActionListener false
        }
        titleInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                timeObject.title = titleInput.text.toString()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        titleInput.isFocusableInTouchMode = false
        titleInput.clearFocus()
        titleInput.setOnClickListener { showTitleKeyPad() }


    }

    private fun initDateTime() {
        timeLy.setOnClickListener {
            DateTimePickerDialog(MainActivity.instance!!, timeObject) { sCal, eCal, allday ->
                timeObject.dtStart = sCal.timeInMillis
                timeObject.dtEnd = eCal.timeInMillis
                timeObject.allday = allday
                updateUI()
            }.show(MainActivity.instance?.supportFragmentManager, null)
        }
    }

    fun initMap() {
        (MainActivity.instance?.supportFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { map ->
            googleMap = map
            mapTouchView.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }
        }
    }

    private fun showTitleKeyPad() {
        titleInput.isFocusableInTouchMode = true
        if (titleInput.requestFocus()) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(titleInput, 0)
        }
    }

    private fun setData(data: TimeObject) {
        originalData = data
        timeObject.copy(data)
        if(data.isManaged) {
            deleteBtn.visibility = View.VISIBLE
        }else {
            deleteBtn.visibility = View.GONE
        }
    }

    private fun updateUI() {
        colorBtn.setCardBackgroundColor(timeObject.color)
        fontColorText.setColorFilter(timeObject.fontColor)

        updateTitleUI()
        updateLocationUI()
    }

    private fun updateTitleUI() {
        when(timeObject.type) {
            TimeObject.Type.NOTE.ordinal -> {
                titleInput.hint = context.getString(R.string.what_do_you_think)
                titleInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                titleInput.typeface = AppRes.regularFont
            }
            else -> {
                titleInput.hint = context.getString(R.string.title)
                titleInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32f)
                titleInput.typeface = AppRes.boldFont
            }
        }
        titleInput.setText(timeObject.title)
    }

    private fun updateLocationUI() {
        if(timeObject.location.isNullOrBlank()) {
            locationLy.visibility = View.GONE
        }else {
            locationLy.visibility = View.VISIBLE
            locationText.text = timeObject.location

            val latLng = LatLng(timeObject.latitude, timeObject.longitude)
            googleMap?.clear()
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            googleMap?.addMarker(MarkerOptions().position(latLng))

            locationText.setOnClickListener { openPlacePicker() }
            mapTouchView.setOnClickListener{ openMapActivity() }
        }
    }

    fun confirm() {
        TimeObjectManager.save(timeObject)
    }

    fun show(timeObject: TimeObject) {
        l("=======SHOW DetailView=======\n$timeObject")
        viewMode = ViewMode.OPENED
        MainActivity.instance?.let {
            it.onDimDark(true, true)
        }
        //layoutParams.height = insertModeHeight
        //requestLayout()
        setData(timeObject)
        updateUI()

        val t1 = makeFromBottomSlideTransition()
        t1.addTarget(contentPanel)

        val transitionSet = TransitionSet()
        transitionSet.addTransition(t1)
        transitionSet.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                //contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        contentPanel.visibility = View.VISIBLE

        if(!timeObject.isManaged) {
            //showTitleKeyPad()
        }
    }

    fun hide() {
        l("=======HIDE DetailView=======")
        viewMode = ViewMode.CLOSED
        MainActivity.instance?.let {
            it.offDimDark(true, true)
        }
        val t1 = makeFromBottomSlideTransition()
        t1.addTarget(contentPanel)

        val transitionSet = TransitionSet()
        transitionSet.addTransition(t1)
        transitionSet.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                hideKeyPad(windowToken, titleInput)
            }
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        //contentLy.layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
        contentPanel.visibility = View.INVISIBLE
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_LOCATION && resultCode == RESULT_OK) {
            val place = PlacePicker.getPlace(data, context)
            timeObject.location = "${place.name}\n${place.address}"
            timeObject.latitude = place.latLng.latitude
            timeObject.longitude = place.latLng.longitude
            updateLocationUI()
        }
    }

    fun openPlacePicker() {
        val builder = PlacePicker.IntentBuilder()
        MainActivity.instance?.startActivityForResult(builder.build(MainActivity.instance), RC_LOCATION)
    }

    private fun openMapActivity() {
        MainActivity.instance?.let {
            val intent = Intent(it, MapActivity::class.java)
            intent.putExtra("location", timeObject.location)
            intent.putExtra("lat", timeObject.latitude)
            intent.putExtra("lng", timeObject.longitude)
            it.startActivity(intent)
        }
    }
}