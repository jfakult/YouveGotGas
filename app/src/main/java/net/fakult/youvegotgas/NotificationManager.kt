package net.fakult.youvegotgas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class NotificationManager(val context: Context)
{
    fun showNotification(layoutID : Int, text: String, channelID : String, notificationID : Int)
    {
        Log.d("Note", "Opening")
        val notificationLayout = RemoteViews("net.fakult.youvegotgas", layoutID)

        val customNotification = NotificationCompat.Builder(context,  channelID)
            .setSmallIcon(R.drawable.logo2)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)

        val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= 26)
        {
            val channel = NotificationChannel(channelID, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            customNotification.setChannelId("Channel?")
            mNotificationManager.createNotificationChannel(channel)
        }

        mNotificationManager.notify(notificationID, customNotification.build())
    }
}