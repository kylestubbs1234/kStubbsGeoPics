package edu.ucsb.cs.cs184.kstubbsgeopics.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var email = MutableLiveData<String>().apply {
        value = ""
    }

    var password = MutableLiveData<String>().apply {
        value = ""
    }
}