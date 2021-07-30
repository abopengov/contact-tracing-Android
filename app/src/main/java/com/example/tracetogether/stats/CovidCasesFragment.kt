package com.example.tracetogether.stats

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.fragment_covid_cases.*
import kotlinx.android.synthetic.main.view_stats_cases.*
import kotlinx.android.synthetic.main.view_stats_cases_by_zone.*
import kotlinx.android.synthetic.main.view_stats_charts.*
import java.text.SimpleDateFormat
import java.util.*


/*
    Fragment for the "For use only by" screen in the Upload flow,
 */
class CovidCasesFragment : Fragment() {
    companion object {
        const val THREE_MONTHS_LABEL_FREQUENCY = 15
        const val SIX_MONTHS_LABEL_FREQUENCY = 30
    }

    private val statsViewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_covid_cases, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "stats_covid_cases".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }

        tv_new_cases?.setLocalizedString("stats_new_cases")
        tv_new_variant_cases?.setLocalizedString("stats_new_variant_cases")

        tv_cases_by_zone_title?.setLocalizedString("stats_active_cases_by_zone")

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

        tv_charts_title?.setLocalizedString("stats_active_cases_over_time")

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

        statsViewModel.newCasesToday.observe(viewLifecycleOwner, Observer { newCasesToday ->
            tv_new_cases_count.text = StatsFormatter.formatWithCommas(newCasesToday)
        })

        statsViewModel.newCasesTrend.observe(viewLifecycleOwner, Observer { trend ->
            when (trend) {
                Trend.TRENDING_UP -> {
                    iv_new_cases_trend.visibility = View.VISIBLE
                    iv_new_cases_trend.setImageResource(R.drawable.ic_trending_up)
                }
                Trend.TRENDING_DOWN -> {
                    iv_new_cases_trend.visibility = View.VISIBLE
                    iv_new_cases_trend.setImageResource(R.drawable.ic_trending_down)
                }
                else -> iv_new_cases_trend.visibility = View.GONE
            }
        })

        statsViewModel.newVariantCases.observe(viewLifecycleOwner, Observer { totalCases ->
            tv_new_variant_cases_count.text = StatsFormatter.formatWithCommas(totalCases)
        })

        statsViewModel.newVariantCasesTrend.observe(viewLifecycleOwner, Observer { trend ->
            when (trend) {
                Trend.TRENDING_UP -> {
                    iv_new_variant_trend.visibility = View.VISIBLE
                    iv_new_variant_trend.setImageResource(R.drawable.ic_trending_up_grey)
                }
                Trend.TRENDING_DOWN -> {
                    iv_new_variant_trend.visibility = View.VISIBLE
                    iv_new_variant_trend.setImageResource(R.drawable.ic_trending_down_grey)
                }
                else -> iv_new_variant_trend.visibility = View.GONE
            }
        })

        statsViewModel.latestDate.observe(viewLifecycleOwner, Observer { latestDate ->
            tv_last_updated.text = if (latestDate != null) getFormattedDate(latestDate) else "-"
        })

        statsViewModel.dailyActiveCasesReported.observe(
            viewLifecycleOwner,
            Observer { activeCases ->
                when (tab_layout_timeline.selectedTabPosition) {
                    0 -> setBarChartData(activeCases)
                    1, 2 -> setLineChartData(
                        if (tab_layout_timeline.selectedTabPosition == 1)
                            THREE_MONTHS_LABEL_FREQUENCY
                        else SIX_MONTHS_LABEL_FREQUENCY,
                        activeCases
                    )
                }
            })

        val casesByZoneAdapter = CasesByZoneAdapter()
        rv_cases_by_zone.adapter = casesByZoneAdapter
        rv_cases_by_zone.addItemDecoration(
            DividerItemDecorator(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.divider_grey
                )
            )
        )

        statsViewModel.casesByZone.observe(viewLifecycleOwner, Observer { casesByZone ->
            casesByZoneAdapter.submitList(casesByZone?.toMutableList())
        })

        statsViewModel.getProvinceDaily()
        statsViewModel.getZoneLatest()
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
