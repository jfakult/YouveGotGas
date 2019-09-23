package net.fakult.youvegotgas

import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest

class GeoFence
{
    val GEOFENCE_TYPE_HOME = 0
    val GEOFENCE_TYPE_WORK = 1
    val GEOFENCE_TYPE_MOTION_DETECTOR = 2
    val GEOFENCE_TYPE_UNKNOWN = 99

    val GEOFENCE_RADIUS : Double = 100.0

    fun getCurrentLocation() : Array<Double>
    {
        val latLng = doubleArrayOf(0.0, 0.0)

        return latLng.toTypedArray()
    }

    fun createGeofence(geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent, type: Int, lat: Double, lng: Double)
    {
        val geofence = buildGeofence(lat, lng, GEOFENCE_RADIUS, type)

        //Do something with firebase based on type

        //val request = buildGeofencingRequest(geofence)

        geofencingClient.addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(geofencingClient.applicationContext, "Successfully made geofence!", Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                Toast.makeText(geofencingClient.applicationContext, "Failed to make geofence", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun removeGeofence(geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent)
    {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(geofencingClient.applicationContext, "Successfully removed geofence!", Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                Toast.makeText(geofencingClient.applicationContext, "failed to remove geofence", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun buildGeofence(latitude: Double, longitude: Double, radius: Double, type: Int): Geofence
    {
        val roundedLat = round_coordinate(latitude)
        val roundedLng = round_coordinate(longitude)
        return Geofence.Builder()
            .setRequestId("$type+$latitude+$longitude")
            .setCircularRegion(latitude, longitude, radius.toFloat())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest
    {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

    fun round_coordinate(coord : Double) : Double
    {
        return coord
    }
}