package com.example.tracetogether.stats

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.R
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.LocalizationHandler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_vaccinations.*
import kotlinx.android.synthetic.main.view_stats_charts.*
import kotlinx.android.synthetic.main.view_stats_vaccines.*
import java.text.SimpleDateFormat
import java.util.*

class VaccinationsFragment : Fragment() {
    private val statsViewModel: StatsViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vaccinations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "stats_vaccinations".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }

        tv_vaccines_given_today?.setLocalizedString("stats_vaccines_given")
        tv_vaccines_administered?.setLocalizedString("stats_vaccines_administered")
        tv_fully_vaccinated?.setLocalizedString("stats_fully_vaccinated")

        tab_layout_timeline.getTabAt(0)?.text = "stats_last_30_days".getLocalizedText()
        tab_layout_timeline.getTabAt(1)?.text = "stats_last_three_months".getLocalizedText()
        tab_layout_timeline.getTabAt(2)?.text = "stats_last_six_months".getLocalizedText()
        tab_layout_timeline.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> statsViewModel.setChartPeriod(ChartPeriod.LAST_30_DAYS)
                        1 -> statsViewModel.setChartPeriod(ChartPeriod.THREE_MONTHS)
                        2 -> statsViewModel.setChartPeriod(ChartPeriod.SIX_MONTHS)
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })

        tv_charts_title?.setLocalizedString("stats_doses_over_time")

        btn_goto_dashboard?.setLocalizedString("stats_goto_dashboard")
        btn_goto_dashboard?.setOnClickListener { openDashboard() }

        setupLiveData()
        bar_chart?.setup()
        line_chart?.setup()
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }

    private fun setupLiveData() {
        statsViewModel.networkError.observe(viewLifecycleOwner, Observer { networkError ->
            if (networkError) {
                Snackbar.make(coordinator_layout, "error_cannot_fetch_data".getLocalizedText(), Snackbar.LENGTH_SHORT)
                        .show()
            }
        })

        statsViewModel.latestDate.observe(viewLifecycleOwner, Observer { latestDate ->
            tv_last_updated.text = if (latestDate != null) getFormattedDate(latestDate) else "-"
        })

        statsViewModel.vaccinesGivenToday.observe(viewLifecycleOwner, Observer { vaccinesGiven ->
            tv_vaccines_given_today_count.text = StatsFormatter.formatWithCommas(vaccinesGiven)
        })

        statsViewModel.vaccinesGivenTrend.observe(viewLifecycleOwner, Observer { trend ->
            when (trend) {
                Trend.TRENDING_UP -> {
                    iv_vaccines_trend.visibility = View.VISIBLE
                    iv_vaccines_trend.setImageResource(R.drawable.ic_trending_up)
                }
                Trend.TRENDING_DOWN -> {
                    iv_vaccines_trend.visibility = View.VISIBLE
                    iv_vaccines_trend.setImageResource(R.drawable.ic_trending_down)
                }
                else -> iv_vaccines_trend.visibility = View.GONE
            }
        })

        statsViewModel.vaccinesAdministered.observe(
                viewLifecycleOwner,
                Observer { vaccinesAdministered ->
                    tv_vaccines_administered_count.text = StatsFormatter.formatWithCommas(vaccinesAdministered)
                })

        statsViewModel.fullyVaccinated.observe(viewLifecycleOwner, Observer { fullyVaccinated ->
            tv_fully_vaccinated_count.text = StatsFormatter.formatWithCommas(fullyVaccinated)
        })

        statsViewModel.dailyCumulativeVaccineDoses.observe(
                viewLifecycleOwner,
                Observer { cumulativeVaccineDoses ->
                    when (tab_layout_timeline.selectedTabPosition) {
                        0 -> setBarChartData(cumulativeVaccineDoses)
                        1, 2 -> setLineChartData(
                                if (tab_layout_timeline.selectedTabPosition == 1)
                                    CovidCasesFragment.THREE_MONTHS_LABEL_FREQUENCY
                                else CovidCasesFragment.SIX_MONTHS_LABEL_FREQUENCY,
                                cumulativeVaccineDoses
                        )
                    }
                })

        statsViewModel.getProvinceDaily()
    }

    private fun setBarChartData(data: List<Long?>) {
        bar_chart.visibility = View.VISIBLE
        line_chart.visibility = View.GONE

        bar_chart.displayData(requireContext(), data)
    }

    private fun setLineChartData(labelFrequency: Int, data: List<Long?>) {
        bar_chart.visibility = View.GONE
        line_chart.visibility = View.VISIBLE

        line_chart.displayData(requireContext(), labelFrequency, data)
    }

    private fun getFormattedDate(date: Date): String {
        val simpleFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return LocalizationHandler.getInstance()
                .getString("last_updated_label") + simpleFormat.format(date)
    }

    private fun openDashboard() {
        val url = AppConstants.KEY_STATS.getUrl(requireContext(), getString(R.string.stats_url))
        val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }
}