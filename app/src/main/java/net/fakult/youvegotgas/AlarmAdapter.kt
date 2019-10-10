package net.fakult.youvegotgas

import android.app.TimePickerDialog
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.morning_alarm_view.view.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.time.Duration

private const val BUTTON_ACTIVE = 27
private const val BUTTON_INDEX = 28
private const val ALARM_ID = 29

class AlarmAdapter(private val alarmList: MutableList<AlarmObject>) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>()
{
    class AlarmViewHolder(val alarmView: LinearLayout) : RecyclerView.ViewHolder(alarmView)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder
    {
        // create a new view
        val alarmView = LayoutInflater.from(parent.context).inflate(R.layout.morning_alarm_view, parent, false) as LinearLayout

        val timeTag = System.currentTimeMillis()
        updateTag(alarmView, ALARM_ID, "alarm_id", timeTag)
        updateTag(alarmView.nameText, ALARM_ID, "alarm_id", timeTag)
        updateTag(alarmView.timeText, ALARM_ID, "alarm_id", timeTag)

        alarmView.timeText.setOnClickListener {
            val currentTimeText = (it as TextView).text.split(":")

            TimePickerDialog(it.context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                (it).text = formatTime(hourOfDay, minute)
            }, currentTimeText[0].toInt(), currentTimeText[1].toInt(), false).show()
        }

        var index = 0
        for (view in alarmView.daysActiveList.children)
        {
            if (view !is RelativeLayout) continue

            val button = view.getChildAt(0) as Button?
            if (button != null)
            {
                updateTag(button, ALARM_ID, "alarm_id", timeTag)
                updateTag(button, BUTTON_INDEX, "button_index", index)
                button.setOnClickListener {
                    val buttonState = getTag(button, BUTTON_ACTIVE, "button_active") as Boolean
                    val newButtonState = !buttonState
                    updateTag(button, BUTTON_ACTIVE, "button_active", newButtonState)

                    if (newButtonState)
                    {
                        button.setBackgroundResource(R.drawable.toggle_button_on)
                        button.setTextColor(0x00333333)
                    }
                    else
                    {
                        button.setBackgroundResource(R.drawable.toggle_button_off)
                        button.setTextColor(0x00DDDDDD)
                    }
                }

                index += 1
            }
        }

        return AlarmViewHolder(alarmView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int)
    {
        val formattedTime = formatTime(alarmList[position].hour, alarmList[position].minute)

        holder.alarmView.nameText.setText(alarmList[position].name)
        holder.alarmView.timeText.text = formattedTime
        holder.alarmView.ampmText.text = if (alarmList[position].hour <= 12) "AM" else "PM"

        for (i in alarmList[position].daysActive.indices)
        {
            if (holder.alarmView.daysActiveList.getChildAt(i) is Button)
            {
                val desiredState = alarmList[position].daysActive[i - 1]
                val button = holder.alarmView.daysActiveList.getChildAt(i) as Button
                val buttonState = getTag(button, BUTTON_ACTIVE, "button_active") as Boolean

                if (buttonState != desiredState) button.callOnClick()
            }
        }
    }

    fun addItem(alarm: AlarmObject)
    {
        alarmList.add(alarm)
        notifyItemInserted(alarmList.size)
    }

    fun removeAt(position: Int)
    {
        alarmList.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun formatTime(hour : Int, minute: Int): String
    {
        return String.format("{0}:{1:D2}", hour, minute)
    }

    private fun updateTag(view : View, TAG : Int, key : String, value : Any)
    {
        var tag = view.getTag(TAG).toString()

        if (tag == "")
        {
            tag = "{}"
        }

        val data = JSONObject(tag)
        //if (data.has(key))
        //{
        data.put(key, value)

        view.setTag(TAG, data.toString())
        //}
    }

    private fun getTag(view : View, TAG: Int, key : String) : Any?
    {
        val tag = view.getTag(TAG).toString()

        if (tag == "")
        {
            return false
        }

        return JSONObject(tag).get(key)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = alarmList.size

    //TODO sync state with server when alarms change
}