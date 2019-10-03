package net.fakult.youvegotgas

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

//Need to optimize geofence management in order to save battery... Dont forget!

class GeofenceBroadcastReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        Log.d("Receiving", "!!!")

        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 600)

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError())
        {
            //val errorMessage = GeofenceErrorMessages.getErrorString(this, geofencingEvent.errorCode)
            Log.e("Geofence-broad", "Error")
            return
        }

        val geofenceManager = GeoFence()
        val geofencingClient = LocationServices.getGeofencingClient(context as Activity)
        val auth = FirebaseAuth.getInstance()
        val firebaseID = auth.uid!!
        val databaseReference = FirebaseDatabase.getInstance()
            .reference
        val latLng = geofenceManager.getCurrentLocation(context)

        val noteMan = NotificationManager(context, geofencingClient, geofenceManager.getGeofencePendingIntent(context), databaseReference, firebaseID)

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) or (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) or (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL))
        {
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            var type = "---"
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) type = "ENTER"
            else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) type = "EXIT"
            else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) type = "DWELL"


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

            if (geofenceId == geofenceManager.GEOFENCE_TYPE_HOME)
            {
                if (type == "ENTER")
                {
                    Log.d("HOME", "Welcome home!")
                    //Send notification for final odometer tally
                    //Give option for end of the day statistics
                    noteMan.showNotification(R.layout.notification_enter_home, "temp", "enter_home", 1)

                    //Clear all geofences?
                }
                else if (type == "EXIT")
                {
                    //Start up all work geofences
                    Log.d("HOME", "Thanks for stopping by!")
                    noteMan.showNotification(R.layout.notification_leaving_home, "temp", "leaving_home", 2)

                    //Launch our dwell geofences to track when we are arriving at work
                    //Wait ~3 minutes to let them get far enough away from home
                    Handler().postDelayed({
                                              val success = geofenceManager.createGeofence(context, geofencingClient, geofenceManager.getGeofencePendingIntent(context), databaseReference, firebaseID, geofenceManager.GEOFENCE_TYPE_MOTION_DETECTOR, latLng[0], latLng[1])
                                          }, 3 * 60 * 1000)
                }
                // Ignore DWELL
            }
            else if (geofenceId == geofenceManager.GEOFENCE_TYPE_WORK)
            {
                if (type == "ENTER")
                {
                    Log.d("WORK", "Make sure to update your odo")

                    noteMan.showNotification(R.layout.notification_enter_work, "temp", "enter_work", 3)
                    // Open notification asking for updated odometer
                    // "Does this look right?"



                    // Clear dwell geofences
                    // Clear work geofences
                }
                else if (type == "EXIT")
                {
                    // Restart all work geofences

                    Log.d("WORK", "Thanks for stopping by. Ill check to see when you reach the next place")

                    //noteMan.showNotification(R.layout.notification_leaving_work, "temp", "exit_home", 1)
                    // If they chose to update later (See Motion_Detector), this will show up:
                    //         "Quickly update your odometer!"
                    // 2 choices here:
                    // 1. Wait ~5 minutes. Open notification asking them to enter new work
                    //                     Or when entering work hide the notification
                    // 2. Wait ~1 minute, then periodically track speed with GPS, set up quick "dwell" geofence to determine if they have arrived
                }
                // Ignore DWELL
            }
            else if (geofenceId == geofenceManager.GEOFENCE_TYPE_MOTION_DETECTOR)
            {
                // Ignore ENTER
                if (type == "EXIT")
                {
                    Log.d("DWELL", "This isnt work, moving on")
                    //noteMan.showNotification(R.layout.notification_leaving_work, "temp", "exit_home", 1)
                    // Disable and delete this geofence
                    // Set a timer for when the next MD geofence should start up (~1 minute to account for dwell time?)

                    val success = geofenceManager.removeGeofence(geofencingClient, context, databaseReference, firebaseID, triggeringGeofences[0].requestId)

                    Handler().postDelayed({
                                              val success = geofenceManager.createGeofence(context, geofencingClient, geofenceManager.getGeofencePendingIntent(context), databaseReference, firebaseID, geofenceManager.GEOFENCE_TYPE_MOTION_DETECTOR, latLng[0], latLng[1])
                                          }, 30 * 1000) // 30 seconds
                }
                else if (type == "DWELL") //Dwell time ~= 3 minutes (long enough to never trigger at a light)
                {
                    //Clear all work geofences

                    // "Have you arrived? (redir to add new work place)" "Not sure what odo says? Ill update it later"
                    // Make sure to update new geofence when done
                    // Maps to estimate distance from starting point
                    Log.d("DWELL", "Have you arrived? (redir to add new work place) Not sure what odo says? Ill update it later")

                    noteMan.showNotification(R.layout.notification_dwell, "temp", "dwell", 1)
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