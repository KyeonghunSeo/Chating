package com.hellowo.journey.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R

class MapActivity : AppCompatActivity() {
    private var location: String? = null
    private var lat: Double = 0.toDouble()
    private var lng: Double = 0.toDouble()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        location = intent.getStringExtra("location")
        lat = intent.getDoubleExtra("lat", 0.0)
        lng = intent.getDoubleExtra("lng", 0.0)

        setMap(lat, lng)
    }

    private fun setMap(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            map.moveCamera(CameraUpdateFactory.zoomTo(16f))
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            map.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
                override fun getInfoContents(p0: Marker?): View {
                    val info = LinearLayout(this@MapActivity)
                    info.orientation = LinearLayout.VERTICAL

                    val title = TextView(this@MapActivity)
                    title.setTextColor(Color.BLACK)
                    title.gravity = Gravity.CENTER
                    title.typeface = AppTheme.boldFont
                    title.text = p0?.title
/*
                    val snippet = TextView(this@MapActivity)
                    snippet.setTextColor(Color.GRAY)
                    snippet.text = p0?.snippet
*/
                    info.addView(title)
                    //info.addView(snippet)

                    return info
                }
                override fun getInfoWindow(p0: Marker?): View?  = null
            })
            map.addMarker(MarkerOptions().position(latLng).title(location))?.showInfoWindow()
        }
    }
}