package com.example.tracetogether.network

import com.squareup.moshi.Json
import java.util.*

data class ProvinceDaily(
    @Json(name = "date")
    val date: Date?,
    @Json(name = "cases_reported")
    val casesReported: Long?,
    @Json(name = "new_variant_cases")
    val variantReported: Long?,
    @Json(name = "cumulative_cases")
    val cumulativeCases: Long?,
    @Json(name = "vaccine_doses_given_today")
    val vaccineDosesGivenToday: Long?,
    @Json(name = "cumulative_vaccine_doses")
    val cumulativeVaccineDoses: Long?,
    @Json(name = "active_cases_reported")
    val activeCasesReported: Int?,
    @Json(name = "people_fully_vaccinated")
    val peopleFullyVaccinated: Long?
)
