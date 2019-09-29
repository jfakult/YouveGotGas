package net.fakult.youvegotgas

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.firebase.database.DatabaseReference
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.abs


class GeoFence
{
    val GEOFENCE_TYPE_HOME = 0
    val GEOFENCE_TYPE_WORK = 1
    val GEOFENCE_TYPE_MOTION_DETECTOR = 2
    val GEOFENCE_TYPE_UNKNOWN = 99

    val GEOFENCE_RADIUS: Double = 5000.0

    fun getCurrentLocation(context: Context): Array<Double>
    {
        val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager

        var latitude = 0.0
        var longitude = 0.0

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            longitude = location!!.longitude
            latitude = location.latitude
        }
        else
        {
            Log.d("Location", "No permission")
        }

        Log.d("Got latlng", "$latitude, $longitude")

        val latLng = doubleArrayOf(latitude, longitude)
        return latLng.toTypedArray()
    }

    fun createGeofence(activity: Activity, geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent, databaseReference: DatabaseReference, firebaseID: String, type: Int, lat: Double, lng: Double): Boolean
    {
        val geofenceJSON = loadGeofences(activity)

        for (i in 0 until geofenceJSON.length())
        {
            val geofence = geofenceJSON.getJSONObject(i)

            if ((geofence.getInt("type") == type) && (isInSameArea(geofence.getDouble("lat"), lat, geofence.getDouble("lng"), lng)))
            {
                Toast.makeText(activity.applicationContext, "Already created a geofence here", Toast.LENGTH_LONG)
                    .show()
                return false
            }
        }

        val geofence = buildGeofence(lat, lng, GEOFENCE_RADIUS, type)

        //Do something with firebase based on type

        //val request = buildGeofencingRequest(geofence)

        geofencingClient.addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
            ?.run {
                addOnSuccessListener {
                    Toast.makeText(geofencingClient.applicationContext, "Successfully made geofence!", Toast.LENGTH_LONG)
                        .show()

                    updateGeofences(activity, geofenceJSON, databaseReference, firebaseID, type, lat, lng)
                }
                addOnFailureListener {
                    Log.d("error", it.message!!)
                    Toast.makeText(geofencingClient.applicationContext, "Failed to make geofence", Toast.LENGTH_LONG)
                        .show()
                }
            }

        return true
    }

    fun removeAllGeofences(geofencingClient: GeofencingClient, pendingIntent: PendingIntent, activity: Activity, databaseReference: DatabaseReference, firebaseID: String)
    {
        geofencingClient.removeGeofences(pendingIntent)
            ?.run {
                addOnSuccessListener {
                    Toast.makeText(geofencingClient.applicationContext, "Successfully removed all geofences!", Toast.LENGTH_LONG)
                    //.show()
                }
                addOnFailureListener {
                    Toast.makeText(geofencingClient.applicationContext, "failed to remove geofence", Toast.LENGTH_LONG)
                        .show()
                }
            }

        deleteGeofenceData(activity, databaseReference, firebaseID, "*")
    }

    fun removeGeofence(geofencingClient: GeofencingClient, activity: Activity, databaseReference: DatabaseReference, firebaseID: String, geofenceID: String)
    {
        geofencingClient.removeGeofences(listOf(geofenceID))
            ?.run {
                addOnSuccessListener {
                    Toast.makeText(geofencingClient.applicationContext, "Successfully removed geofence!", Toast.LENGTH_LONG)
                    //.show()
                }
                addOnFailureListener {
                    Toast.makeText(geofencingClient.applicationContext, "failed to remove geofence", Toast.LENGTH_LONG)
                        .show()
                }
            }

        deleteGeofenceData(activity, databaseReference, firebaseID, geofenceID)
    }

    private fun buildGeofence(latitude: Double, longitude: Double, radius: Double, type: Int): Geofence
    {
        return Geofence.Builder()
            //.setRequestId("$type+$latitude+$longitude")
            .setRequestId("$type+$latitude+$longitude")
            .setCircularRegion(latitude, longitude, radius.toFloat())
            //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setLoiteringDelay(5000)
            .build()
    }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest
    {
        return GeofencingRequest.Builder()
            //.setInitialTrigger(0)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL or GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(listOf(geofence))
            .build()
    }

    // Rounds to the nearest .01 degree (within ~.7 mile or 1000 meters of each other on the X and Z planes)
    private fun isInSameArea(lat1: Double, lat2: Double, lng1: Double, lng2: Double): Boolean
    {
        return (abs(lat1 - lat2) < 0.01) && (abs(lng1 - lng2) < 0.01)
    }

    private fun updateGeofences(activity: Activity, geofenceJSON: JSONArray, databaseReference: DatabaseReference, firebaseID: String, type: Int, lat: Double, lng: Double)
    {
        val newGeofence = JSONObject("{\"type\": $type, \"lat\": $lat, \"lng\": $lng}")
        geofenceJSON.put(newGeofence)

        activity.getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putString("registeredGeofences", geofenceJSON.toString())
            .apply()

        Log.d("Geofences", "updating to firebase")
        //Upload result to firebase asynchronously as well
        databaseReference.child("users")
            .child(firebaseID)
            .child("registeredGeofences")
            .setValue("$type+$lat+$lng")
    }

    fun getGeofencePendingIntent(context: Context) : PendingIntent
    {
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return geofencePendingIntent
    }

    private fun loadGeofences(activity: Activity) : JSONArray
    {
        val registeredGeofences = activity.getPreferences(Context.MODE_PRIVATE)
            .getString("registeredGeofences", "")

        var geofenceJSON = JSONArray("[]")
        try
        {
            geofenceJSON = JSONArray(registeredGeofences)
        }
        catch (e: JSONException)
        {
            // Do nothing
        }

        return geofenceJSON
    }

    private fun deleteGeofenceData(activity: Activity, databaseReference: DatabaseReference, firebaseID: String, id : String)
    {
        val geofencesJSON = loadGeofences(activity)

        var i = 0
        while (i < geofencesJSON.length())
        {
            val geofence = geofencesJSON.getJSONObject(i)

            val geoID = geofence.getInt("type").toString() + "+" + geofence.getDouble("lat").toString() + "+" + geofence.getDouble("lng").toString()
            if (id == "*" || geoID == id)
            {
                geofencesJSON.remove(i)
                i--
            }

            i++
        }

        activity.getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putString("registeredGeofences", geofencesJSON.toString())
            .apply()

        //Upload result to firebase asynchronously as well
        databaseReference.child("users").child(firebaseID).child("registeredGeofences")
            .setValue(geofencesJSON.toString())
    }
}