package edu.ucsb.cs.cs184.kstubbsgeopics.maps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.ucsb.cs.cs184.kstubbsgeopics.R
import edu.ucsb.cs.cs184.kstubbsgeopics.User
import edu.ucsb.cs.cs184.kstubbsgeopics.post.Post
import java.lang.Boolean.getBoolean

class MapsViewModel : ViewModel() {
    var distances = MutableLiveData<ArrayList<Double>>().apply {
        value = ArrayList()
    }

    var displayedPosts = MutableLiveData<ArrayList<Post>>().apply {
        value = ArrayList()
    }

    var totalPosts = MutableLiveData<ArrayList<Post>>().apply {
        value = ArrayList()
    }

    var users = MutableLiveData<ArrayList<User>>().apply {
        value = ArrayList()
    }

    var currentCaption = MutableLiveData<String>().apply {
        value = ""
    }
    var currentPhotoPath = MutableLiveData<String>().apply {
        value = ""
    }

    var currentLatitude = MutableLiveData<Double>().apply {
        value = null
    }
    var currentLongitude = MutableLiveData<Double>().apply {
        value = null
    }

    var mapLatitude = MutableLiveData<Double>().apply {
        value = 34.412936
    }
    var mapLongitude = MutableLiveData<Double>().apply {
        value = -119.847863
    }
    var mapZoom = MutableLiveData<Float>().apply {
        value = 15.5F
    }

    var editMode = MutableLiveData<Boolean>().apply {
        value = false
    }
    var started = MutableLiveData<Boolean>().apply {
        value = false
    }

    var scrollToIndex = MutableLiveData<Int>().apply {
        value = -1
    }
}