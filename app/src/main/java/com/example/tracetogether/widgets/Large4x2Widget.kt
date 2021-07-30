package com.example.tracetogether.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.example.tracetogether.R
import com.example.tracetogether.stats.StatsFormatter
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.stats.Trend
import com.example.tracetogether.widgets.WidgetHelper.updateTrendImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Implementation of App Widget functionality.
 */
class Large4x2Widget : BaseAppWidgetProvider() {
    override fun onUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            val views = RemoteViews(context.packageName, R.layout.large4x2_widget)
            views.setTextViewText(R.id.tv_new_cases_title, "widgets_cases_today".getLocalizedText())
            views.setTextViewText(R.id.tv_variant_cases_today_title, "widgets_new_variant_cases_today".getLocalizedText())
            views.setTextViewText(R.id.tv_vaccines_given_today_title, "widgets_vaccines_given_today".getLocalizedText())
            views.setTextViewText(R.id.tv_total_vaccines_given_title, "widgets_total_vaccines_doses_given".getLocalizedText())
            views.setTextViewText(R.id.tv_fully_vaccinated_title, "widgets_fully_vaccinated".getLocalizedText())

            appWidgetManager.updateAppWidget(appWidgetId, views)

            getLastTwoDailyStats()?.let { dailyStats ->
                val formattedNewCasesCount = StatsFormatter.formatWithCommas(dailyStats.second.casesReported)
                views.setTextViewText(R.id.tv_new_cases, formattedNewCasesCount)
                updateTrendImage(views, R.id.iv_new_cases_trend, Trend.getTrend(dailyStats.first.casesReported, dailyStats.second.casesReported))

                val formattedVariantCasesTodayCount = StatsFormatter.formatWithCommas(dailyStats.second.variantReported)
                views.setTextViewText(R.id.tv_variant_cases_today, formattedVariantCasesTodayCount)
                updateTrendImage(views, R.id.iv_variant_cases_trend, Trend.getTrend(dailyStats.first.variantReported, dailyStats.second.variantReported), false)

                val formattedVaccinesGivenCount = StatsFormatter.formatWithCommas(dailyStats.second.vaccineDosesGivenToday)
                views.setTextViewText(R.id.tv_vaccines_given_today, formattedVaccinesGivenCount)
                updateTrendImage(views, R.id.iv_vaccines_trend, Trend.getTrend(dailyStats.first.vaccineDosesGivenToday, dailyStats.second.vaccineDosesGivenToday))

                val formattedTotalVaccinesGivenCount = StatsFormatter.formatWithCommas(dailyStats.second.cumulativeVaccineDoses)
                views.setTextViewText(R.id.tv_total_vaccines_given, formattedTotalVaccinesGivenCount)

                val formattedFullyVaccinatedCount = StatsFormatter.formatWithCommas(dailyStats.second.peopleFullyVaccinated)
                views.setTextViewText(R.id.tv_fully_vaccinated, formattedFullyVaccinatedCount)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}