package net.fakult.youvegotgas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth

const val CODE_ENTER_HOME = 1
const val CODE_ENTER_WORK = 2
const val CODE_DWELL = 3

class MorningNotificationReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        val geofenceManager = GeoFence()
        val geofencingClient = LocationServices.getGeofencingClient(context!!)
        val dataManager = DataManager(context)

        val noteMan = NotificationManager(context)

        Toast.makeText(context, "Called MorningNotService", Toast.LENGTH_SHORT)
            .show()

        val notificationId = R.layout.notification_leaving_home //intent.getIntExtra("notificationId")

        noteMan.showNotification(notificationId, dataManager, 0, notificationId.toString(), notificationId)

        /*if (startId == CODE_ENTER_HOME)
        {
            notificationId = R.layout.notification_enter_home
        }
        else if (startId == CODE_ENTER_WORK)
        {
            notificationId = R.layout.notification_enter_work
        }
        else if (startId == CODE_DWELL)
        {
            notificationId = R.layout.notification_dwell
        }
        else // (notification_id == -1)
        {
            Toast.makeText(context, "Error passing in layout: $notificationId", Toast.LENGTH_SHORT).show()
            return
        }*/
    }
}