package net.fakult.youvegotgas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver()
{
    // ...
    override fun onReceive(context: Context?, intent: Intent?)
    {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError())
        {
            //val errorMessage = GeofenceErrorMessages.getErrorString(this, geofencingEvent.errorCode)
            Log.e("Geofence-broad", "Error")
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) or (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT))
        {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            var type : String = "---"
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                type = "ENTER"
            else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
                type = "EXIT"

            Toast.makeText(context, "Received a $type geofence transition!!", Toast.LENGTH_LONG).show()

            //Launch a notification based on the information received
        }
        else
        {
            // Log the error.
            Log.e("geofence-broad2", "Invalid transition type")
        }
    }
}