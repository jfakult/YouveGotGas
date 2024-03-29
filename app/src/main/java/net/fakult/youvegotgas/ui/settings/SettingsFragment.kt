package net.fakult.youvegotgas.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.fakult.youvegotgas.*
import java.util.*

class SettingsFragment : Fragment()
{
    private lateinit var viewAdapter: AlarmAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var adapterInitialized = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        val addStartButton = root.findViewById(R.id.addStartButton) as Button
        val morningAlarmRecycler = root.findViewById(R.id.morningAlarmRecycler) as RecyclerView
        /*
        This is temporary, just for testing for now. To be implemented by buttons on this page later
         */

        addStartButton.setOnClickListener {
            TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val daysActive = booleanArrayOf(true, true, true, true, true, false, false).toTypedArray()
                val alarmTimeStamp = (System.currentTimeMillis() / 1000).toInt()
                val alarm = AlarmObject("Name", hourOfDay, minute, daysActive, alarmTimeStamp)

                if (!adapterInitialized)
                {
                    viewAdapter = AlarmAdapter(mutableListOf(alarm), context!!)
                    morningAlarmRecycler.apply {
                        adapter = viewAdapter
                    }
                    adapterInitialized = true
                }

                (morningAlarmRecycler.adapter as AlarmAdapter).addItem(alarm)

            }, 8, 0, false).show()
        }

        viewManager = LinearLayoutManager(context)
        //viewAdapter = AlarmAdapter(listOf<AlarmObject?>() as MutableList<AlarmObject?>)

        val swipeHandler = object : SwipeToDeleteCallback(context!!)
        {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
            {
                viewAdapter.removeAt(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(morningAlarmRecycler)

        morningAlarmRecycler.apply {
            layoutManager = viewManager
            //adapter = viewAdapter
        }

        return root
    }
}