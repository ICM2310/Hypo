package com.pontimovil.hypo.googlemaps

import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.pontimovil.hypo.R
import com.pontimovil.hypo.modelo.Rollo
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.util.GeoPoint

class MapsFragment : Fragment() {

    //Location Managment
    private var FirstAnimationLoc = false
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var UserLoc: Location

    private lateinit var roadManager: OSRMRoadManager
    private lateinit var polyline: Polyline
    private lateinit var polylineJSON: Polyline

    private lateinit var UserLocMarker: Marker
    private val RollMarkers = mutableListOf<Marker>()


    //location permission
    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback {
            if (it) {
                Log.i("Permisos", "Hay permiso: " + it.toString())
                locationSettings()
            } else {
                Log.i("Permisos", "Permiso denegado")
            }
        }
    )

    //LocationSettings
    private val locationSettings =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            ActivityResultCallback {
                if (it.resultCode == AppCompatActivity.RESULT_OK) {
                    startLocationUpdates()
                } else {
                    Log.i("Permisos - Settings", "GPS is off")
                }
            })

    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.defaultstylemaps))
        if(!::UserLoc.isInitialized){
            val sydney = LatLng(-34.0, 151.0)
            googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
        else{
            val userMarker = LatLng(UserLoc.latitude, UserLoc.longitude)
            UserLocMarker = googleMap.addMarker(MarkerOptions().position(userMarker).title("Marker in Sydney"))!!
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(userMarker))
        }
        googleMap.setPadding(0,0,0,150)
        googleMap.uiSettings.setAllGesturesEnabled(true)
        if(!FirstAnimationLoc){
            googleMap.uiSettings.isZoomControlsEnabled = true
            FirstAnimationLoc = true
        }

        val roll = Rollo.createMockRoll();
        //Pintar ruta
        CreateRollMarkers(roll,googleMap)
        val polylineOptions = PolylineOptions().apply {
            addAll(CreateRouteOSM(roll))
            width(5F)
            color(Color.BLUE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Administracion de localizacion
        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = createLocationRequest()
        locationCallback = setupLocationCallback()

        //Inicializar gestor OSRM
        roadManager = OSRMRoadManager(requireContext(), "ANDROID")
        roadManager.addRequestOption("geometries=polyline")
        roadManager.addRequestOption("overview=full")
        roadManager.addRequestOption("annotations=true")
        roadManager.addRequestOption("steps=true")
        roadManager.addRequestOption("alternatives=true")
        //
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun locationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val isr: IntentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettings.launch(isr)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setInterval(1000)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }


    private fun setupLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    //updateWriteLoc(location)
                    UpdateUserLoc(location)
                    createMapFragment()
                }
            }
        }
    }

    private fun createMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.MapsFrag) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
    private fun UpdateUserLoc(location : Location){
        this.UserLoc = location
    }

    private fun CreateRouteOSM(r : Rollo): List<LatLng> {
        val fotos = r.fotos
        val RoutePoints = ArrayList<GeoPoint>()
        for(p in fotos){
            RoutePoints.add(GeoPoint(p.location.latitude, p.location.longitude))
        }
        val road = roadManager.getRoad(RoutePoints)
        val latLngList = road.mRouteHigh.map { LatLng(it.latitude, it.longitude) }
        return latLngList
    }

    private fun CreateRollMarkers(r: Rollo, googleMap: GoogleMap){
        val fotos = r.fotos
        var cont = 0
        for(p in fotos){
            if(cont == 0){
                val bitmap = BitmapFactory.decodeResource(resources,R.drawable.roll_blue)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
                val CustomMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                googleMap.addMarker(MarkerOptions().position(p.location).icon(CustomMarker))?.let { RollMarkers.add(it) }
            }
            if(cont == 1 ){
                val bitmap = BitmapFactory.decodeResource(resources,R.drawable.roll_mango)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
                val CustomMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                googleMap.addMarker(MarkerOptions().position(p.location).icon(CustomMarker))?.let { RollMarkers.add(it) }
            }
            if(cont == 2){
                val bitmap = BitmapFactory.decodeResource(resources,R.drawable.roll_pink)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
                val CustomMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                googleMap.addMarker(MarkerOptions().position(p.location).icon(CustomMarker))?.let { RollMarkers.add(it) }
            }
            if(cont == 3){
                cont = 0
            }
            cont++
        }
    }

}