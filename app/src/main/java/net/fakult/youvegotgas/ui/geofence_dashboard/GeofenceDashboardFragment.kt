package net.fakult.youvegotgas.ui.geofence_dashboard

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import net.fakult.youvegotgas.DataManager
import net.fakult.youvegotgas.GeoFence
import net.fakult.youvegotgas.R


const val TAG: String = "GeoDash"

class GeofenceDashboardFragment : Fragment()
{
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceManager: GeoFence
    private lateinit var dataManager: DataManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
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

            //DELET THIS??? :(
            geofenceManager.removeGeofence(geofencingClient, dataManager, geofenceManager.GEOFENCE_TYPE_HOME.toString() + "+" + latLng[0] + "+" + latLng[1])

            val success = geofenceManager.createGeofence(activity as Activity, geofencingClient, geofenceManager.getGeofencePendingIntent(context!!), dataManager, geofenceManager.GEOFENCE_TYPE_HOME, latLng[0], latLng[1], "home")
            //geofenceManager.startHomeGeofence(activity as Activity, geofencingClient, geofenceManager.getGeofencePendingIntent(context!!), dataManager, geofenceManager.GEOFENCE_TYPE_HOME, latLng[0], latLng[1], "home")
        }

        addWorkGeofenceButton.setOnClickListener {
            val latLng: Array<Double> = geofenceManager.getCurrentLocation(context!!)

            val name = System.currentTimeMillis()
                .toString()
            val success = geofenceManager.createGeofence(activity as Activity, geofencingClient, geofenceManager.getGeofencePendingIntent(context!!), dataManager, geofenceManager.GEOFENCE_TYPE_WORK, latLng[0], latLng[1], name)
        }

        //geofenceManager.removeAllGeofences(geofencingClient, geofencePendingIntent, activity, databaseReference, firebaseID)

        odometerInput.setText(loadOdometer().toString())
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
                                                     updateOdometer(s.toString().toInt())
                                                 }

                                             })

        return root
    }

    private fun loadOdometer(): Int
    {
        return dataManager.getData("odometer", -1, null) as Int
    }

    fun updateOdometer(updatedOdometer: Int)
    {
        dataManager.setData("odometer", updatedOdometer, null)
    }
}