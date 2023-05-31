package com.pontimovil.hypo.googlemaps

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pontimovil.hypo.R

class LocationService : Service() {
    private lateinit var locationManager: LocationManager
    //FireStore
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Update Firestore database with the new location here
            CreateFirebaseUpdate(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationService","Accesdio al servicio")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundWithNotification()
        } else {
            startForeground(1, Notification())
        }

        startLocationUpdates()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundWithNotification() {
        val channelId = "LocationService"
        val channelName = "Location Service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Updating location in the background")
            .setSmallIcon(R.drawable.icon_background_display)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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
}
