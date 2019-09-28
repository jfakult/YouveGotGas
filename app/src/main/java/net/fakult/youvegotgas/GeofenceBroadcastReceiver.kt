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
    override fun onReceive(context: Context?, intent: Intent?)
    {
        Log.d("Recieving", "!!!")
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

            var type: String = "---"
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                type = "ENTER"
            else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
                type = "EXIT"
            else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
                type = "DWELL"


            //for (geofence : Geofence in triggeringGeofences)
            //{
            val geofenceData = triggeringGeofences[0].requestId.split("+")
            val geofenceId: Int = geofenceData[0].toInt()
            val geofenceLat: Double = geofenceData[1].toDouble()
            val geofenceLng: Double = geofenceData[2].toDouble()

            Toast.makeText(context, "Received a $type geofence transition!!", Toast.LENGTH_LONG)
                .show()
            Log.e("Geofence trigger", "$geofenceId $geofenceLat $geofenceLng $type")

            //}
            // Delete any duplicate geofences

            // Dont forget to add the implementation for morning notifications! ("Does this look right")p
            // Launch a notification based on the information received
            // Make sure to track "last geofence ID". HOME-HOME trips should be ignored
            Log.d("Geofnce", "Going in")

            if (geofenceId == GeoFence().GEOFENCE_TYPE_HOME)
            {
                if (type == "ENTER")
                {
                    //Send notification for final odometer tally
                    //Give option for end of the day statistics
                }
                else if (type == "EXIT")
                {
                    // 2 choices here:
                    // 1. Wait ~5 minutes. Open notification asking them to enter new work
                    //                     Or when entering work hide the notification
                    // 2. Periodically track speed with GPS, set up quick "dwell" geofence to determine if they have arrived
                }
                else if (type == "DWELL")
                {
                    Toast.makeText(context, "Triggered dwell!", Toast.LENGTH_LONG).show()
                    // No action?
                }
            }
            else if (geofenceId == GeoFence().GEOFENCE_TYPE_WORK)
            {
                if (type == "ENTER")
                {
                    // Open notification asking for updated odometer (Use maps to estimate in absence of history data)
                    // "Does this look right?"
                }
                else if (type == "EXIT")
                {
                    // If they chose to update later (See Motion_Detector), this will show up:
                    //         "Quickly update your odometer!"
                    // 2 choices here:
                    // 1. Wait ~5 minutes. Open notification asking them to enter new work
                    //                     Or when entering work hide the notification
                    // 2. Wait ~1 minute, then periodically track speed with GPS, set up quick "dwell" geofence to determine if they have arrived
                }
                else if (type == "DWELL")
                {
                    // No action?
                }
            }
            else if (geofenceId == GeoFence().GEOFENCE_TYPE_MOTION_DETECTOR)
            {
                if (type == "EXIT")
                {
                    // Disable and delete this geofence
                    // Set a timer for when the next MD geofence should start up (~1 minute to account for dwell time?)
                }
                else if (type == "DWELL") //Dwell time ~= 3 minutes (long enough to never trigger at a light)
                {
                    // "Have you arrived? (redir to add new work place)" "Not sure what odo says? Ill update it later"
                    // Make sure to update new geofence when done
                    // Maps to estimate distance from starting point
                }
            }
            else //Geofence().GEOFENCE_TYPE_UNKNOWN
            {
                Log.e("Geofencetrigger", "Unknown geofence type: $geofenceId")
            }
        }
        else
        {
            // Log the error.
            Log.e("geofence-broad2", "Invalid transition type")
        }
    }
}