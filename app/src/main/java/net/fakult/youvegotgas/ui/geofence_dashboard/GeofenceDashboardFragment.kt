package net.fakult.youvegotgas.ui.geofence_dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.fakult.youvegotgas.R

class GeofenceDashboardFragment : Fragment()
{
    private lateinit var dashboardViewModel: GeofenceDashboardViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        dashboardViewModel = ViewModelProviders.of(this).get(GeofenceDashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        //val textView: TextView = root.findViewById(R.id.text_dashboard)

        /*dashboardViewModel.text.observe(this, Observer {
            textView.text = it
        })*/

        return root
    }
}