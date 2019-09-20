package net.fakult.youvegotgas.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.fakult.youvegotgas.R

class HomeFragment : Fragment()
{
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val tutorialComplete = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt("tutorial_complete", 0)

        if (tutorialComplete == 1)
        {
            val textView: TextView = root.findViewById(R.id.text_home)
            homeViewModel.text.observe(this, Observer {
                textView.text = "Stats shown here"
            })
        }
        else
        {
            val textView: TextView = root.findViewById(R.id.text_home)
            homeViewModel.text.observe(this, Observer {
                textView.text = it
            })
        }

        return root
    }
}