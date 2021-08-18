package com.example.tracetogether.pause

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tracetogether.Preference
import java.time.LocalTime
import java.time.temporal.ChronoUnit


class PauseViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "PauseViewModel"
        private const val MAX_VALID_TIME_INTERVAL = 8 * 60 * 60
        private const val ONE_DAY_IN_SECONDS = 24 * 60 * 60
    }

    private val mutablePauseStartTime = MutableLiveData<LocalTime>()
    private val mutablePauseEndTime = MutableLiveData<LocalTime>()
    private val mutablePauseScheduled = MutableLiveData<Boolean>()
    private val mutableInvalidTimeRange = MutableLiveData<Boolean>()
    private val mutableTimeRangeSaved = MutableLiveData<Boolean>()

    val pauseStartTime: LiveData<LocalTime> = mutablePauseStartTime
    val pauseEndTime: LiveData<LocalTime> = mutablePauseEndTime
    val pauseScheduled: LiveData<Boolean> = mutablePauseScheduled
    val invalidTimeRange: LiveData<Boolean> = mutableInvalidTimeRange
    val timeRangeSaved: LiveData<Boolean> = mutableTimeRangeSaved

    init {
        reload()
    }

    fun reload() {
        mutablePauseStartTime.value = Preference.getPauseStartTime(getApplication())
        mutablePauseEndTime.value = Preference.getPauseEndTime(getApplication())
        mutablePauseScheduled.value = Preference.getPauseScheduled(getApplication())
    }

    fun setStartTime(startTime: LocalTime) {
        mutablePauseStartTime.value = startTime
        validateSelectedTimes()
    }

    fun setEndTime(endTime: LocalTime) {
        mutablePauseEndTime.value = endTime
        validateSelectedTimes()
    }

    fun saveTimeRange() {
        val startTime = mutablePauseStartTime.value
        val endTime = mutablePauseEndTime.value

        if (startTime != null && endTime != null && validTimeRange(startTime, endTime)) {
            Preference.setPauseStartTime(getApplication(), startTime)
            Preference.setPauseEndTime(getApplication(), endTime)

            PauseScheduler.togglePause(getApplication())
            PauseScheduler.schedule(getApplication(), startTime, endTime)

            mutableTimeRangeSaved.value = true
        } else {
            mutableTimeRangeSaved.value = false
        }
    }

    fun setScheduled(scheduled: Boolean) {
        Preference.setPauseScheduled(getApplication(), scheduled)

        if (scheduled) {
            val startTime = mutablePauseStartTime.value
            val endTime = mutablePauseEndTime.value
            if (startTime != null && endTime != null) {
                PauseScheduler.togglePause(getApplication())
                PauseScheduler.schedule(getApplication(), startTime, endTime)
            }
        } else {
            PauseScheduler.cancel(getApplication())
            PauseScheduler.togglePause(getApplication())
        }

        mutablePauseScheduled.value = scheduled
    }

    private fun validateSelectedTimes() {
        val startTime = mutablePauseStartTime.value
        val endTime = mutablePauseEndTime.value

        val valid = if (startTime != null && endTime != null) {
            validTimeRange(startTime, endTime)
        } else {
            false
        }

        mutableInvalidTimeRange.value = !valid
    }

    private fun validTimeRange(startTime: LocalTime, endTime: LocalTime): Boolean {
        return if (startTime > endTime) {
            val durationInSeconds = startTime.until(LocalTime.MIDNIGHT, ChronoUnit.SECONDS) +
                    LocalTime.MIDNIGHT.until(endTime, ChronoUnit.SECONDS) + ONE_DAY_IN_SECONDS
            durationInSeconds <= MAX_VALID_TIME_INTERVAL
        } else {
            startTime.until(endTime, ChronoUnit.SECONDS) <= MAX_VALID_TIME_INTERVAL
        }
    }
}
