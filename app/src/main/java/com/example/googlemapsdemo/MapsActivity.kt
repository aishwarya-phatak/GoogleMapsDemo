package com.example.googlemapsdemo

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.googlemapsdemo.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var puneMarker: Marker
    private lateinit var mumbaiMarker: Marker

    private var count = 0
    private var markers = ArrayList<Marker>()
    private lateinit var circle : Circle
    private lateinit var polygon: Polygon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        initMapSettings()
        addMarkers()
        addOnMapClickListener()
        addOnMarkerClickListener()
        addOnInfoWindowClickListener()
        addOnMarkerDragListener()

        addShapes()
        setInfoWindowAdapter()
        miscellaneous()

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun miscellaneous(){
        mMap.setOnPoiClickListener {
            Log.e("tag","${it.latLng} -- ${it.name}")
            mt("Poi ${it.name}")
        }
    }

    private fun setInfoWindowAdapter(){
        mMap.setInfoWindowAdapter(MyInfoWindowAdapter())
    }

    private inner class MyInfoWindowAdapter : GoogleMap.InfoWindowAdapter{
        override fun getInfoWindow(marker: Marker): View? {
            return null
        }

        override fun getInfoContents(marker: Marker): View? {
           var view = layoutInflater.inflate(R.layout.my_infowindow,null)
            var binding = com.example.googlemapsdemo.databinding.MyInfowindowBinding.bind(view)
            binding.txtTitle.setText(marker.title)
            binding.imgInfo.setImageResource(R.mipmap.ic_launcher)

            return view
        }
    }

    private fun addShapes(){
        circle = mMap.addCircle(
            CircleOptions()
                .center(puneMarker.position)
                .radius(1000.0)
                .strokeColor(Color.MAGENTA)
                .fillColor(Color.argb(90,255,0,0))
        )

        polygon = mMap.addPolygon(
            PolygonOptions()
                .add(LatLng(13.0827,80.2707))
                .add(LatLng(12.2958,76.6394))
                .add(LatLng(10.8505,76.2711))
                .add(LatLng(7.8731,80.7718))
                .fillColor(Color.argb(90,0,0,255))
        )
    }

    private fun addOnMarkerDragListener(){
        mMap.setOnMarkerDragListener(
            object : GoogleMap.OnMarkerDragListener{
                override fun onMarkerDragStart(marker: Marker) {
                    mt("Drag Started ${marker.position.latitude} -- ${marker.position.longitude}")
                }

                override fun onMarkerDrag(marker: Marker) {
                    Log.e("tag","Drag ${marker.title} -- ${marker.position.latitude},${marker.position.longitude}")
                }

                override fun onMarkerDragEnd(marker: Marker) {
                    mt("Drag End ${marker.position.latitude} -- ${marker.position.longitude}")
                }
            }
        )
    }

    private fun addOnInfoWindowClickListener(){
        mMap.setOnInfoWindowClickListener {
            mt("InfoWindow Click: ${it.title}")
        }
    }

    private fun addOnMarkerClickListener(){
        mMap.setOnMarkerClickListener(
            object : GoogleMap.OnMarkerClickListener{
                override fun onMarkerClick(marker: Marker): Boolean {
                   mt("Marker Clicked : ${marker.title}")
                    return false
                }
            }
        )
    }

    private fun addOnMapClickListener(){
        mMap.setOnMapClickListener {
            markers.add(
                mMap.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title("Marker $count")
                )!!
            )
            count++

         var cameraPosition = CameraPosition.Builder()
                .tilt(40F)
                .zoom(50F)
                .bearing(20F)
                .target(puneMarker.position)
                .build()

            var cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            mMap.animateCamera(cameraUpdate,5000,MyAnimationCancellableCallback())
        }
    }

    inner class MyAnimationCancellableCallback : GoogleMap.CancelableCallback {
        override fun onCancel() {
           mt("Animation Cancelled")
        }

        override fun onFinish() {
            mt("Animation Finished")
        }
    }

    private fun mt(text:String){
        Toast.makeText(this,text, Toast.LENGTH_LONG).show()
    }

    private fun addMarkers(){
        puneMarker = mMap.addMarker(
            MarkerOptions()
                .title("Pune")
                .snippet("This is Pune!")
                .position(LatLng(18.5167,73.856))
                .visible(true)
                .rotation(20F)
                .draggable(true)
                .anchor(0.1F,0.1F)
                .zIndex(60F)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        )!!

        var scaledBitMap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources,R.drawable.city_icon),100,100,false
        )

        var cityIcon = BitmapDescriptorFactory.fromBitmap(scaledBitMap)
        mumbaiMarker = mMap.addMarker(
            MarkerOptions()
                .title("Mumbai")
                .snippet("This is Mumbai")
                .icon(cityIcon)
                .position(LatLng(19.0760,72.8774))
        )!!
    }

    @SuppressLint("MissingPermission")
    private fun initMapSettings(){
        mMap.isIndoorEnabled = true
        mMap.isMyLocationEnabled = true
        mMap.isBuildingsEnabled = true
        mMap.isTrafficEnabled = true

        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
    }
}