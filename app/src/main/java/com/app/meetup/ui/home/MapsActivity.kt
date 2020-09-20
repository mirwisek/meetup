package com.app.meetup.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationProvider
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.ActivityViewModel
import com.app.meetup.MainActivity
import com.app.meetup.R
import com.app.meetup.utils.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var location: LatLng? = null
    private lateinit var locationProvider: FusedLocationProviderClient
    private val callback = getLocationCallback()
    private lateinit var eventFragment: EventFragment

    companion object {
        const val RC_NEW_EVENT = 125
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        eventFragment = (supportFragmentManager.findFragmentByTag(EventFragment.TAG)
            ?: EventFragment()) as EventFragment

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.events_fragment, eventFragment)
            commit()
        }

        autocompleteFragment.apply {
            setCountries("PK")
            setTypeFilter(TypeFilter.ESTABLISHMENT)
        }

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.PHOTO_METADATAS
            )
        )

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                log("Place: ${place.name}, ${place.address}, ${place.photoMetadatas}")
                navigateToLocation(place.latLng!!)
                dropMarker(place.latLng!!)
                eventFragment.onPlaceSelected(place)
            }

            override fun onError(status: Status) {
                log("An error occurred: $status")
            }

        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getCurrentLocation()
    }

    fun dropMarker(loc: LatLng) {
        map.clear()
        map.addMarker(
            MarkerOptions()
            .title("Selected Location")
            .position(loc))
    }

    fun navigateToLocation(loc: LatLng) {
        val position = CameraPosition.builder()
            .target(loc)
            .zoom(14f)
            .build()

        map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        locationProvider = LocationServices.getFusedLocationProviderClient(this)

        // Because last know location isn't reliable we want to listen to location updates anyway
        locationProvider.requestLocationUpdates(
            getLocationRequest(),
            callback,
            Looper.myLooper()
        ).addOnFailureListener {
            toast("Couldn't find your location")
            it.printStackTrace()
        }
    }

    private fun getLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                p0?.locations?.let { loc ->
                    sharedPref.edit(true) {
                        putString(Constants.KEY_CURRENT_LOCATION, loc[0].toText())
                    }
                    location = loc[0].toLatLng()
                    navigateToLocation(location!!)
                    // We need location once, remove listener
                    locationProvider.removeLocationUpdates(callback)
                }

            }
        }
    }
}