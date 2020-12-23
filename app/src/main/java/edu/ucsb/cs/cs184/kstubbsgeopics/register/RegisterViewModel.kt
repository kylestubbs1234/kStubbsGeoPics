package edu.ucsb.cs.cs184.kstubbsgeopics.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {
    var email = MutableLiveData<String>().apply {
        value = ""
    }

    var password = MutableLiveData<String>().apply {
        value = ""
    }

    var username = MutableLiveData<String>().apply {
        value = ""
    }
}