package com.example.tracetogether.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CovidTestData(val testDate: Long, val symptomsDate: Long?) : Parcelable
