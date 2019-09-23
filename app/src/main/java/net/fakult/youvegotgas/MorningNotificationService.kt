package net.fakult.youvegotgas

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

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

        //Open notification here that for morning routine
    }
}