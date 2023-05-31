package com.pontimovil.hypo.googlemaps

import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
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
import androidx.fragment.app.Fragment
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pontimovil.hypo.R
import com.pontimovil.hypo.modelo.Rollo
import com.pontimovil.hypo.modelo.Usuario
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.util.GeoPoint


class MapsFragment : Fragment() {

    private lateinit var mMap: GoogleMap;

    //FireStore
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val markerList: MutableList<Marker> = mutableListOf()

    //Location Managment
    private var FirstAnimationLoc = false
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var UserLoc: Location

    private lateinit var roadManager: OSRMRoadManager
    private lateinit var polyline: Polyline

    //Sensor de luminosidad
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener

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
        mMap = googleMap
        /*mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(), R.raw.defaultstylemaps
            ))*/
        if(!::UserLoc.isInitialized){
            val sydney = LatLng(-34.0, 151.0)
            googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
        else{
            val listaUsuarios = getRegistersFromUsers()
            for((index,usuario) in listaUsuarios.withIndex()){
                if(markerList.getOrNull(index) == null){
                    val Ubi = LatLng(usuario.Lat.toDouble(),usuario.Long.toDouble())
                    googleMap.addMarker(MarkerOptions().position(Ubi).title(usuario.email))
                        ?.let { markerList.add(index, it) }
                }
                else{
                    if(markerList[index].position.latitude != usuario.Lat.toDouble() || markerList[index].position.longitude != usuario.Long.toDouble()){
                        val Ubi = LatLng(usuario.Lat.toDouble(),usuario.Long.toDouble())
                        val marker = googleMap.addMarker(MarkerOptions().position(Ubi).title(usuario.email))
                        if (marker != null) {
                            markerList[index] = marker
                        }
                    }
                }
            }
            if(!::UserLocMarker.isInitialized){
                val userMarker = LatLng(UserLoc.latitude, UserLoc.longitude)
                UserLocMarker = googleMap.addMarker(MarkerOptions().position(userMarker).title("Your Location"))!!
            }
            else{
                val userMarker = LatLng(UserLoc.latitude, UserLoc.longitude)
                UserLocMarker.position = userMarker
            }
            if(!FirstAnimationLoc){
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UserLocMarker.position, 15F), 1000, null)
                FirstAnimationLoc = true
            }

        }
        googleMap.setPadding(0,0,0,150)
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.uiSettings.isZoomControlsEnabled = true

        val roll = Rollo.createMockRoll();
        //Pintar ruta
        CreateRollMarkers(roll,googleMap)
        Log.i("Ruta", "Se creo ruta")
        val polylineOptions = PolylineOptions().apply {
            addAll(CreateRouteOSM(roll))
            width(5F)
            color(Color.BLUE)
        }
        Log.i("Ruta", "Se pinto la ruta")
        if (!::polyline.isInitialized) {
            polyline = googleMap.addPolyline(polylineOptions)
        } else {
            polyline.remove()
            polyline = googleMap.addPolyline(polylineOptions)
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

        //Inicializar sensor de luminosidad
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensorListener = setupSensorListener()
        sensorManager.registerListener(
            lightSensorListener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
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
        CreateFirebaseUpdate(UserLoc)
    }

    private fun CreateRouteOSM(r : Rollo): List<LatLng> {
        val fotos = r.fotos
        val RoutePoints = ArrayList<GeoPoint>()
        RoutePoints.add(GeoPoint(UserLoc.latitude,UserLoc.longitude))
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
                val bitmap = BitmapFactory.decodeResource(resources,R.drawable.roll_orange)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
                val CustomMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
                googleMap.addMarker(MarkerOptions().position(p.location).icon(CustomMarker))?.let { RollMarkers.add(it) }
                cont = 0
            }
            cont++

        }

    }
    private fun setupSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                if (p0 != null) {
                    if (p0.values[0] > 1000) {
                        mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                requireContext(), R.raw.defaultstylemaps
                            )
                        )
                    } else {
                        mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                requireContext(),
                                R.raw.nightstyle
                            )
                        )
                    }
                }
            }
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                //Ignorar
            }
        }
    }

    private fun CreateFirebaseUpdate(UserLoc : Location){

        // Specify the collection name
        val collectionName = "users_loc"

        // Specify the field to query (in this case, "name")
        val fieldName = "email"

        // Specify the value to search for (the name parameter)
        val nameParam = auth.currentUser?.email.toString()

        // Query the collection for documents with matching name
        val query = db.collection("users")
            .whereEqualTo(fieldName, nameParam)

        // Execute the query
        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Retrieve the first document from the query results
                    val documentSnapshot = querySnapshot.documents[0]
                    val documentId = documentSnapshot.id

                    // Modify the data
                    val user: MutableMap<String, Any> = HashMap()
                    user["email"] = auth.currentUser?.email.toString()
                    user["Lat"] = UserLoc.latitude.toString()
                    user["Long"] = UserLoc.longitude.toString()

                    // Get a reference to the document by ID
                    val docRef = db.collection("users").document(documentId)

                    // Update the document with the modified data
                    docRef.update(user as Map<String, Any>)
                        .addOnSuccessListener {
                            // Document successfully updated
                            println("Document successfully updated")
                        }
                        .addOnFailureListener { e ->
                            // Error updating document
                            println("Error updating document: $e")
                        }
                } else {
                    val user: MutableMap<String, Any> = HashMap()
                    user["email"] = auth.currentUser?.email.toString()
                    user["Lat"] = UserLoc.latitude.toString()
                    user["Long"] = UserLoc.longitude.toString()

                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                "FireBase",
                                "DocumentSnapshot added with ID: " + documentReference.id
                            )
                        }
                        .addOnFailureListener { e -> Log.w("FireBase", "Error adding document", e) }
                }
            }
            .addOnFailureListener { e ->
                println("Error querying documents: $e")
            }
    }

    private fun getRegistersFromUsers(): MutableList<Usuario> {
        val collectionName = "yourCollectionName"
        val objectList = mutableListOf<Usuario>()
        // Get a reference to the collection
        val collectionRef = db.collection("users")

        // Fetch all documents in the collection
        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val email = document.get("email").toString()
                    val lat = document.get("Lat").toString()
                    val long = document.get("Long").toString()
                    if(email != auth.currentUser?.email){
                        val U1 = Usuario(email,lat,long)
                        objectList.add(U1)
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }

        return objectList
    }

}