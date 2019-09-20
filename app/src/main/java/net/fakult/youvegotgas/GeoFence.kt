package net.fakult.youvegotgas

import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest

class GeoFence
{
    fun getCurrentLocation() : Array<Double>
    {
        val latLng = doubleArrayOf(0.0, 0.0)

        return latLng.toTypedArray()
    }

    fun createGeofence(geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent, type: String, lat: Double, lng: Double)
    {
        val geofence = buildGeofence(lat, lng, 100.0)

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

    private fun buildGeofence(latitude: Double, longitude: Double, radius: Double): Geofence
    {
        return Geofence.Builder()
            .setRequestId("$latitude-$longitude")
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
}