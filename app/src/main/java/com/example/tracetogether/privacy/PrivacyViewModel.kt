package com.example.tracetogether.privacy

import android.app.Application
import androidx.lifecycle.*
import com.example.tracetogether.Preference
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.network.PrivacyApi
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class PrivacyViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        const val TAG = "PrivacyPolicyViewModel"
    }

    private val mutablePrivacyPolicy = MutableLiveData<String>()
    private val mutablePrivacyPolicyVersion = MutableLiveData<Int>()

    val privacyPolicy: LiveData<String> = mutablePrivacyPolicy
    val newPrivacyPolicyAvailable: LiveData<Boolean> =
        Transformations.map(mutablePrivacyPolicyVersion) { newVersion ->
            val newVersionAvailable = newVersion > Preference.getPrivacyPolicyAccepted(getApplication())
            if (!newVersionAvailable) {
                Preference.setPrivacyPolicyFetchTime(getApplication(), System.currentTimeMillis())
            }
            newVersionAvailable
        }

    fun getPrivacyPolicy() {
        viewModelScope.launch {
            var privacyPolicy: String
            try {
                privacyPolicy = PrivacyApi.service.getPrivacyPolicy().privacyStatement
            } catch (ex: Exception) {
                CentralLog.e(TAG, "Failed to get privacy policy: " + ex.message)
                privacyPolicy = getDefaultPrivacyPolicy()
            }

            mutablePrivacyPolicy.value = privacyPolicy
            parsePrivacyPolicyVersion(privacyPolicy)?.let { newVersion ->
                mutablePrivacyPolicyVersion.value = newVersion
            }
        }
    }

    fun checkPrivacyPolicy() {
        if (shouldCheckPrivacyPolicy()) {
            getPrivacyPolicy()
        } else {
            mutablePrivacyPolicyVersion.value =
                Preference.getPrivacyPolicyAccepted(getApplication())
        }
    }

    fun acceptPrivacyPolicy() {
        mutablePrivacyPolicyVersion.value?.let { newVersion ->
            Preference.setPrivacyPolicyAccepted(getApplication(), newVersion)
            Preference.setPrivacyPolicyFetchTime(getApplication(), System.currentTimeMillis())
        }
    }

    private fun shouldCheckPrivacyPolicy(): Boolean {
        val lastFetchTime = Preference.getPrivacyPolicyFetchTime(getApplication()) / 1000
        val currentTime = System.currentTimeMillis() / 1000
        val hours = (currentTime - lastFetchTime) / 3600
        return hours > 24
    }

    private fun getDefaultPrivacyPolicy() =
        getApplication<Application>().assets.open("privacypolicy.html").bufferedReader()
            .use { it.readText() }

    private fun parsePrivacyPolicyVersion(privacyPolicy: String): Int? {
        val pattern = Pattern.compile("meta name=\"privacy_policy_version\" content=\"(\\d+)\">")
        val matcher = pattern.matcher(privacyPolicy)
        return if (matcher.find()) {
            matcher.group(1)?.toInt()
        } else {
            null
        }
    }
}
