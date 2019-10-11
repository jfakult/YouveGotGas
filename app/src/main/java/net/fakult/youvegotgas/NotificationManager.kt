package net.fakult.youvegotgas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.GeofencingClient
import com.google.firebase.database.DatabaseReference
import net.fakult.youvegotgas.ui.geofence_dashboard.GeofenceDashboardFragment

class NotificationManager(val context: Context, val geofencingClient: GeofencingClient?, val geofencePendingIntent: PendingIntent?, val databaseReference: DatabaseReference?, val firebaseID: String?)
{
    fun showNotification(layoutID: Int, dataManager: DataManager, distance: Int?, channelID: String, notificationID: Int)
    {
        val notificationLayout = RemoteViews("net.fakult.youvegotgas", layoutID)

        val customNotification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.logo2)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= 26)
        {
            val channel = NotificationChannel(channelID, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            customNotification.setChannelId("Channel?")
            mNotificationManager.createNotificationChannel(channel)
        }

        if (layoutID == R.layout.notification_leaving_home)
        {
            val odometer = dataManager.getData("odometer", 0, null) as Int
            notificationLayout.setTextViewText(R.id.odometerValueText, odometer.toString())

            val intent = Intent(context, UpdateOdometerScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra("action", "temp")
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            customNotification.setContentIntent(pendingIntent)
            customNotification.setAutoCancel(true)
        }

        if (layoutID == R.layout.notification_dwell) // Dont forget to launch a new work geofence
        {
            //notificationLayout.setTextViewText(R.id.dwellText, "test")

            val intent = Intent(context, GeofenceDashboardFragment::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra("action", "addEnterTrigger")
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            //Need to also add any logic regarding the new work geofence
            // layoutID = R.layout.notification_enter_work?
            //val latLng = geofenceManager.getCurrentLocation(context)
            //val success = geofenceManager.createGeofence(context, geofencingClient, geofenceManager.getGeofencePendingIntent(context), databaseReference, firebaseID, geofenceManager.GEOFENCE_TYPE_MOTION_DETECTOR, latLng[0], latLng[1])

            customNotification.setContentIntent(pendingIntent)
            customNotification.setAutoCancel(true)
        }

        if (layoutID == R.layout.notification_enter_work)
        {
            //TODO
            val geofenceTriggerLocation = "LOCATION"
            notificationLayout.setTextViewText(R.id.notificationEnterWorkLocationTitle, "Arriving at $geofenceTriggerLocation")

            val intent = Intent(context, UpdateOdometerScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            customNotification.setContentIntent(pendingIntent)
            customNotification.setAutoCancel(true)
        }

        if (layoutID == R.layout.notification_leaving_work)
        {
            // Do nothing
        }

        if (layoutID == R.layout.notification_enter_home)
        {
            val intent = Intent(context, UpdateOdometerScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            customNotification.setContentIntent(pendingIntent)
            customNotification.setAutoCancel(true)
        }

        customNotification.setCustomContentView(notificationLayout)
        customNotification.setCustomBigContentView(notificationLayout)

        mNotificationManager.notify(notificationID, customNotification.build())
    }
}