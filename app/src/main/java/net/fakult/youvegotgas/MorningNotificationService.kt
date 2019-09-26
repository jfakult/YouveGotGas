package net.fakult.youvegotgas

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat

const val CODE_ENTER_HOME = 1
const val CODE_ENTER_WORK = 2
const val CODE_DWELL = 3

class MorningNotificationService : Service()
{
    override fun onBind(intent: Intent?): IBinder?
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onCreate(intent: Intent, startId: Int)
    {
        super.onCreate()
        Toast.makeText(applicationContext, "Called MorningNotService", Toast.LENGTH_SHORT).show()

        var notificationId = -1

        if (startId == CODE_ENTER_HOME)
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
            Toast.makeText(applicationContext, "Error passing in layout: $notificationId", Toast.LENGTH_SHORT).show()
            return
        }

        val notificationLayout = RemoteViews(packageName, notificationId)
        //val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_large)

// Apply the layouts to the notification
        val customNotification = NotificationCompat.Builder(applicationContext, "")
            //.setSmallIcon(R.drawable.notification_icon)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            //.setCustomBigContentView(notificationLayoutExpanded)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .build()
    }
}