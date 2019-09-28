package net.fakult.youvegotgas.ui.geofence_dashboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import net.fakult.youvegotgas.GeoFence
import net.fakult.youvegotgas.GeofenceBroadcastReceiver
import net.fakult.youvegotgas.R
import android.media.ToneGenerator
import android.media.AudioManager



const val TAG: String = "GeoDash"

class GeofenceDashboardFragment : Fragment()
{
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceManager: GeoFence
    private lateinit var databaseReference: DatabaseReference
    private lateinit var activity: Activity

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this.context, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        activity = context as Activity

        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)

        geofencingClient = LocationServices.getGeofencingClient(this.activity as Activity)
        geofenceManager = GeoFence()

        /*dashboardViewModel = ViewModelProviders.of(this).get(GeofenceDashboardViewModel::class.java) */
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val odometerInput = root.findViewById(R.id.odometerInput) as EditText
        //val textView: TextView = root.findViewById(R.id.text_dashboard)

        /*dashboardViewModel.text.observe(this, Observer {
            textView.text = it
        })*/

        val setHomeGeofenceButton: Button = root.findViewById(R.id.set_home_geofence_button)
        setHomeGeofenceButton.setOnClickListener {
            val latLng: Array<Double> = geofenceManager.getCurrentLocation(this.context!!)
            Log.d("going in", latLng[0].toString())
            //geofenceManager.createGeofence(geofencingClient, geofencePendingIntent, GeoFence().GEOFENCE_TYPE_HOME, latLng[0], latLng[1])
            geofenceManager.createGeofence(activity, geofencingClient, geofencePendingIntent, geofenceManager.GEOFENCE_TYPE_MOTION_DETECTOR, latLng[0], latLng[1])
        }

        databaseReference = FirebaseDatabase.getInstance()
            .reference

        odometerInput.setText(loadOdometer(activity).toString())
        odometerInput.addTextChangedListener(object : TextWatcher
                                             {
                                                 override fun afterTextChanged(s: Editable?)
                                                 {
                                                     // Do nothing
                                                 }

                                                 override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
                                                 {
                                                     // Do nothing
                                                 }

                                                 override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
                                                 {
                                                     updateOdometer(activity, s.toString().toInt())
                                                 }

                                             })

        return root
    }

    private fun loadOdometer(activity: Activity): Int
    {
        val odo = activity.getPreferences(Context.MODE_PRIVATE)
            .getInt("odometer", -1)

        if (odo >= 0)
        {
            return odo
        }

        Log.d(TAG, "Looking up odo from firebase")
        //Load from firebase
        //Temporarily disabled as async requests are difficult at best, may not be worth the implementation
        /*databaseReference.child("odometer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError)
            {
                // Ignore
            }

            override fun onDataChange(p0: DataSnapshot)
            {

            }
        })*/

        return 10000
    }

    fun updateOdometer(activity: Activity, updatedOdometer: Int)
    {
        activity.getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putInt("odometer", updatedOdometer)
            .apply()

        //Upload result to firebase asynchronously as well
        databaseReference.child("Users")
            .child("test")
            .setValue("odometer", updatedOdometer)
    }
}