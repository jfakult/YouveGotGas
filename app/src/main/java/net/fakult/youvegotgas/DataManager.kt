package net.fakult.youvegotgas

import android.app.Activity
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import java.util.concurrent.CountDownLatch

class DataManager(val context: Context)
{
    val geofenceManager = GeoFence()
    val geofencingClient = LocationServices.getGeofencingClient(context as Activity)
    val auth = FirebaseAuth.getInstance()
    val firebaseID = auth.uid!!
    val databaseReference = FirebaseDatabase.getInstance()
        .reference

    val prefRef = (context as Activity).getPreferences(Context.MODE_PRIVATE)

    fun getData(key: String, defaultValue: Any, childPath: Array<String>?): Any?
    {
        if (prefRef.contains(key)) // We have a value stored locally, use by default
        {
            when (defaultValue)
            {
                is Int -> return prefRef.getInt(key, defaultValue)
                is Boolean -> return prefRef.getBoolean(key, defaultValue)
                is Float -> return prefRef.getFloat(key, defaultValue)
                is Long -> return prefRef.getLong(key, defaultValue)
                is String -> return prefRef.getString(key, defaultValue)
                is MutableList<*> -> return prefRef.getStringSet(key, null)
            }
        }

        var path = childPath
        if (path == null)
        {
            path = listOf<String>().toTypedArray()
        }

        val result = getSyncData(path, key)

        return result ?: defaultValue
    }

    fun setData(key: String, value: Any, childPath: Array<String>?): Boolean
    {
        val edit = prefRef.edit()
        when (value)
        {
            is Int -> edit.putInt(key, value)
            is Boolean -> edit.putBoolean(key, value)
            is Float -> edit.putFloat(key, value)
            is Long -> edit.putLong(key, value)
            is String -> edit.putString(key, value)
            is MutableList<*> -> edit.putStringSet(key, value as MutableSet<String>)
            else -> return false
        }

        edit.apply()

        var path = childPath
        if (path == null)
        {
            path = listOf<String>().toTypedArray()
        }
        setSyncData(path, key, value)

        return true
    }

    var readResult: Any? = null
    private fun getSyncData(childPath: Array<String>, key: String): Any?
    {
        var child = databaseReference.child("users")
            .child(firebaseID)

        for (subPath in childPath)
        {
            child = child.child(subPath)
        }

        val latch = CountDownLatch(1)

        child.child(key)
            .addListenerForSingleValueEvent(object : ValueEventListener
                                            {
                                                override fun onDataChange(p0: DataSnapshot)
                                                {
                                                    readResult = if (p0.value == null) null else p0.value
                                                    latch.countDown()
                                                }

                                                override fun onCancelled(p0: DatabaseError)
                                                {
                                                    // Ignore
                                                }
                                            })

        try
        {
            latch.await()
        }
        catch (e: InterruptedException)
        {
            readResult = null
        }

        return readResult
    }

    private fun setSyncData(childPath: Array<String>, key: String, value: Any)
    {
        var child = databaseReference.child("users")
            .child(firebaseID)

        for (subPath in childPath)
        {
            child = child.child(subPath)
        }

        child.child(key)
            .setValue(value)
    }

    // Only used for history and other statistics, thus only stored online (i.e don't want to set up a local database)
    // In firebase the data is stored in a chronological hierarchical fashion. I.E one log might be stored under 2019 -> March -> 6 -> 8am -> 1256.02s -> log
    fun pushListItem(key: Array<String>, value: Array<Any>, childPath: Array<String>?): Boolean
    {
        var path = childPath
        if (path == null)
        {
            path = listOf<String>().toTypedArray()
        }

        val date = TimeLog()

        var child = databaseReference.child("users")
            .child(firebaseID)

        for (timeValue in date.timeValues)
        {
            child = child.child(timeValue.toString())
        }

        for (i in key.indices)
        {
            if (i < value.size)
            {
                child.child(key[i])
                    .setValue(value[i])
            }
        }

        return true
    }

    private class TimeLog
    {
        private val d = Calendar.getInstance()
        val year = d.get(Calendar.YEAR)
        val month = d.get(Calendar.MONTH)
        val day = d.get(Calendar.DAY_OF_MONTH)
        val hour = d.get(Calendar.HOUR_OF_DAY)
        val minute = d.get(Calendar.MINUTE)
        val second = d.get(Calendar.SECOND)
        val ms = d.get(Calendar.MILLISECOND)

        val timeValues = listOf(year, month, day, hour, minute, second + (ms/1000.0))
    }

    fun logEvent(description : String, location : String, lat : Double, lng : Double, distance : Int)
    {
        val keys = listOf("description", "location", "lat", "lng", "timestamp", "distance_traveled").toTypedArray()
        val values = listOf(description, location, lat, lng, System.currentTimeMillis(), distance).toTypedArray()

        pushListItem(keys, values, null)
    }
}