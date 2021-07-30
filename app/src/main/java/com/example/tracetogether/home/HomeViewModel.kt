package com.example.tracetogether.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tracetogether.Preference
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.model.GuidanceTile
import com.example.tracetogether.network.HomeStats
import com.example.tracetogether.network.UrlsApi
import com.example.tracetogether.stats.StatsRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "HomeViewModel"
    }

    private val statsRepository = StatsRepository(application)
    private val urlsApiService = UrlsApi.service

    private val mutableHomeStats = MutableLiveData<List<HomeStats>>()
    val homeStats: LiveData<List<HomeStats>> = mutableHomeStats

    private val mutableGuidanceTile = MutableLiveData<GuidanceTile?>()
    val guidanceTile: LiveData<GuidanceTile?> = mutableGuidanceTile

    private val mutableNetworkError = MutableLiveData<Boolean>()
    val networkError = mutableNetworkError

    fun getHomeStats() {
        viewModelScope.launch {
            try {
                mutableHomeStats.value = statsRepository.getHomeStats()
            } catch (ex: Exception) {
                CentralLog.e(TAG, "Failed to get home stats: " + ex.message)
                mutableNetworkError.value = true
                return@launch
            }
        }
    }

    fun getGuidanceTile(context: Context) {
        viewModelScope.launch {
            mutableGuidanceTile.value = if (System.currentTimeMillis() - Preference.getLastFetchedUrlDataTimestamp(context)
                    > Utils.getUrlsCacheDuration()) {
                try {
                    urlsApiService.getGuidanceTile(context)
                } catch (ex: java.lang.Exception) {
                    CentralLog.e(HomeViewModel.TAG, "Failed to get guidance tile: " + ex.message)
                    mutableNetworkError.value = true
                    Preference.getGuidanceTile(context)
                }
            } else {
                Preference.getGuidanceTile(context)
            }
        }
    }
}
