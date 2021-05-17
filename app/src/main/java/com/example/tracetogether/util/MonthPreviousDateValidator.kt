package com.example.tracetogether.util

import com.google.android.material.datepicker.CalendarConstraints
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class MonthPreviousDateValidator : CalendarConstraints.DateValidator {
    private val minDate: Long
    private val maxDate: Long

    init {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        maxDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, -1)
        minDate = calendar.timeInMillis
    }


    override fun isValid(date: Long): Boolean {
        return date in minDate..maxDate
    }
}