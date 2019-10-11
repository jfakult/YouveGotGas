package net.fakult.youvegotgas.ui.geofence_dashboard

import android.app.Activity
import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import net.fakult.youvegotgas.DataManager
import net.fakult.youvegotgas.GeoFence
import net.fakult.youvegotgas.R


const val TAG: String = "GeoDash"

class GeofenceDashboardFragment : Fragment()
{
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceManager: GeoFence
    private lateinit var databaseReference: DatabaseReference
    private lateinit var activity: Activity
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseID: String
    private lateinit var dataManager: DataManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        activity = context as Activity

        auth = FirebaseAuth.getInstance()
        firebaseID = auth.uid!!

        geofencingClient = LocationServices.getGeofencingClient(this.activity as Activity)
        geofenceManager = GeoFence()

        dataManager = DataManager(context!!)

        /*dashboardViewModel = ViewModelProviders.of(this).get(GeofenceDashboardViewModel::class.java) */
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val odometerInput = root.findViewById(R.id.odometerInput) as EditText
        //val textView: TextView = root.findViewById(R.id.text_dashboard)

        /*dashboardViewModel.text.observe(this, Observer {
            textView.text = it
        })*/

        val setHomeGeofenceButton: Button = root.findViewById(R.id.set_home_geofence_button)
        val addWorkGeofenceButton: Button = root.findViewById(R.id.addWorkGeofencebutton)
        setHomeGeofenceButton.setOnClickListener {
            val latLng: Array<Double> = geofenceManager.getCurrentLocation(context!!)

            //DELET THIS :(
            geofenceManager.removeGeofence(geofencingClient, activity, databaseReference, firebaseID, geofenceManager.GEOFENCE_TYPE_HOME.toString() + "+" + latLng[0] + "+" + latLng[1])

            val success = geofenceManager.createGeofence(activity, geofencingClient, geofenceManager.getGeofencePendingIntent(context!!), databaseReference, dataManager, firebaseID, geofenceManager.GEOFENCE_TYPE_HOME, latLng[0], latLng[1], "home")
        }

        addWorkGeofenceButton.setOnClickListener {
            val latLng: Array<Double> = geofenceManager.getCurrentLocation(context!!)

            val name = System.currentTimeMillis().toString()
            val success = geofenceManager.createGeofence(activity, geofencingClient, geofenceManager.getGeofencePendingIntent(context!!), databaseReference, dataManager, firebaseID, geofenceManager.GEOFENCE_TYPE_WORK, latLng[0], latLng[1], name)
        }

        //val inst = FirebaseDatabase.getInstance()
        //val ref = inst.getReference("users").child(auth.uid.toString()).child("test")
        //ref.setValue("value")

        databaseReference = FirebaseDatabase.getInstance()
            .reference

        //geofenceManager.removeAllGeofences(geofencingClient, geofencePendingIntent, activity, databaseReference, firebaseID)

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
        databaseReference.child("users")
            .child(firebaseID)
            .child("odometer")
            .setValue(updatedOdometer)
    }
}