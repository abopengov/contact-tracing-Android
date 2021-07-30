package com.example.tracetogether.more

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracetogether.Preference
import com.example.tracetogether.Utils
import com.example.tracetogether.home.HomeViewModel
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.network.UrlsApi
import kotlinx.coroutines.launch

class MoreViewModel : ViewModel() {
    private val mutableMoreLinks = MutableLiveData<List<MoreLink>>()
    val moreLinks: LiveData<List<MoreLink>> = mutableMoreLinks

    private val mutableNetworkError = MutableLiveData<Boolean>()
    val networkError = mutableNetworkError

    fun fetchMoreLinks(context: Context) {
        viewModelScope.launch {
            mutableMoreLinks.value = if (System.currentTimeMillis() - Preference.getLastFetchedUrlDataTimestamp(context)
                    > Utils.getUrlsCacheDuration()) {
                try {
                    UrlsApi.service.getMoreLinks(context)
                } catch (ex: Exception) {
                    CentralLog.e(HomeViewModel.TAG, "Failed to get more links: " + ex.message)
                    mutableNetworkError.value = true
                    Preference.getMoreLinks(context)
                }
            } else {
                Preference.getMoreLinks(context)
            }
        }
    }
}
