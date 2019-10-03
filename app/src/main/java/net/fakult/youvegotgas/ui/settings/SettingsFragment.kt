package net.fakult.youvegotgas.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.fakult.youvegotgas.MorningNotificationReceiver
import net.fakult.youvegotgas.R
import java.util.*

const val CODE_ENTER_HOME = 1
const val CODE_ENTER_WORK = 2
const val CODE_DWELL = 3

class SettingsFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        /*
        This is temporary, just for testing for now. To be implemented by buttons on this page later
         */

        val intent = Intent(context, MorningNotificationReceiver::class.java)
        //intent.putExtra("notificationId", CODE_ENTER_HOME)
        val alarmManager = context!!.getSystemService(ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(context, CODE_ENTER_HOME, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Issue with getservice above?

        val calendar = Calendar.getInstance()

        calendar.add(Calendar.SECOND, 4)

        Log.d("Alarm", "Should be set")

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        return root
    }
}