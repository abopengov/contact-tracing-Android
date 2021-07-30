package com.example.tracetogether.widgets

import android.appwidget.AppWidgetProvider
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.network.ProvinceDaily
import com.example.tracetogether.network.StatsApi

open class BaseAppWidgetProvider : AppWidgetProvider() {
    protected suspend fun getLastTwoDailyStats(): Pair<ProvinceDaily, ProvinceDaily>? {
        try {
            val dailyStats = StatsApi.service.getProvinceDaily().sortedBy { it.date }
            if (dailyStats.size >= 2) {
                val lastTwoDaysStats = dailyStats.takeLast(2)
                return Pair(lastTwoDaysStats.first(), lastTwoDaysStats.last())
            }
        } catch (ex: Exception) {
            CentralLog.e("NewCasesWidget", "Failed to get province daily: " + ex.message)
        }
        return null
    }
}