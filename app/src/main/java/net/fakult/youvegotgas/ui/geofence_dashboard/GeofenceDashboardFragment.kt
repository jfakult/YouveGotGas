package net.fakult.youvegotgas.ui.geofence_dashboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import net.fakult.youvegotgas.GeoFence
import net.fakult.youvegotgas.GeofenceBroadcastReceiver
import net.fakult.youvegotgas.R

class GeofenceDashboardFragment : Fragment()
{
    lateinit var geofencingClient: GeofencingClient

    private lateinit var geofence_manager : GeoFence

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this.context, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        geofencingClient = LocationServices.getGeofencingClient(this.activity as Activity)
        geofence_manager = GeoFence()

        /*dashboardViewModel = ViewModelProviders.of(this).get(GeofenceDashboardViewModel::class.java) */
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        //val textView: TextView = root.findViewById(R.id.text_dashboard)

        /*dashboardViewModel.text.observe(this, Observer {
            textView.text = it
        })*/

        val setHomeGeofenceButton : Button = root.findViewById(R.id.set_home_geofence_button)
        setHomeGeofenceButton.setOnClickListener {
            val latLng : Array<Double> = geofence_manager.getCurrentLocation()
            geofence_manager.createGeofence(geofencingClient, geofencePendingIntent, GeoFence().GEOFENCE_TYPE_HOME, latLng[0], latLng[1])
        }

        return root
    }
}