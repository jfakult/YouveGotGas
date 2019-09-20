package net.fakult.youvegotgas.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel()
{
    private val _text = MutableLiveData<String>().apply {
        value = "You have not logged any drives yet!\nHead to your Geofence Dashboard to get started!"
    }
    val text: LiveData<String> = _text
}