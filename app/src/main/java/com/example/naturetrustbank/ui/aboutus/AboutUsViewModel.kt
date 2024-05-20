package com.example.naturetrustbank.ui.aboutus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutUsViewModel : ViewModel(){

    private val _text = MutableLiveData<String>().apply {
        value = "About Us Fragment"
    }
    val text: LiveData<String> = _text
}