package com.example.tracetogether.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tracetogether.Preference

class LearnMoreViewModel(app: Application) : AndroidViewModel(app) {
    private val mutableSelectedItem = MutableLiveData<Boolean>()
    val userHasSeenWhatsNew: LiveData<Boolean> get() = mutableSelectedItem

    fun whatsNewPageSeen(hasSeen: Boolean) {
        Preference.setUserHasSeenWhatsNew(getApplication(), hasSeen)
        mutableSelectedItem.value = hasSeen
    }

    init {
        mutableSelectedItem.value = Preference.userHasSeenWhatsNew(getApplication())
    }
}
