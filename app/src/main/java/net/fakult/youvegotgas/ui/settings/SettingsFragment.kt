package net.fakult.youvegotgas.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.fakult.youvegotgas.R
import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import android.app.PendingIntent
import android.content.Intent
import net.fakult.youvegotgas.MorningNotificationService
import java.util.Calendar

class SettingsFragment : Fragment()
{

    private lateinit var notificationsViewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        notificationsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        notificationsViewModel.text.observe(this, Observer {
            textView.text = it
        })

        /*
        This is temporary, just for testing for now. To be implemented by buttons on this page later
         */


        val intent = Intent(context, MorningNotificationService::class.java)
        val alarmManager = context?.getSystemService(ALARM_SERVICE) as? AlarmManager
        val pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Issue with getservice above?

        val calendar = Calendar.getInstance()

        calendar.add(Calendar.SECOND, 10)

        alarmManager?.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        return root
    }
}