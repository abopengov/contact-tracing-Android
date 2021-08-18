package com.example.tracetogether.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.example.tracetogether.R
import com.example.tracetogether.stats.StatsFormatter
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.stats.Trend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Implementation of App Widget functionality.
 */
class VaccinesGivenWidget : BaseAppWidgetProvider() {
    override fun onUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateVaccinesGivenAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    fun updateVaccinesGivenAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            val views = RemoteViews(context.packageName, R.layout.vaccines_given_widget)
            views.setTextViewText(
                    R.id.tv_vaccines_given_today_title,
                    "widgets_vaccines_given_today".getLocalizedText()
            )
            appWidgetManager.updateAppWidget(appWidgetId, views)

            getLastTwoDailyStats()?.let { dailyStats ->
                val formattedVaccinesGivenCount =
                        StatsFormatter.formatWithCommas(dailyStats.second.vaccineDosesGivenToday)
                views.setTextViewText(R.id.tv_vaccines_given_today, formattedVaccinesGivenCount)
                WidgetHelper.updateTrendImage(views, R.id.iv_vaccines_trend, Trend.getTrend(dailyStats.first.vaccineDosesGivenToday, dailyStats.second.vaccineDosesGivenToday))

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

}