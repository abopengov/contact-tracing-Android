package com.example.tracetogether.stats

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.R
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_stats.*

class StatsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_stats?.setLocalizedString("stats_title")
        tv_covid_cases?.setLocalizedString("stats_covid_cases")
        tv_vaccinations?.setLocalizedString("stats_vaccinations")
        tv_map?.setLocalizedString("stats_map")
        tv_dashboard?.setLocalizedString("stats_dashboard")

        tv_covid_cases?.setOnClickListener { navigateToFragment(CovidCasesFragment()) }
        tv_vaccinations?.setOnClickListener { navigateToFragment(VaccinationsFragment()) }
        tv_map?.setOnClickListener { navigateToFragment(StatsMapFragment()) }
        tv_dashboard?.setOnClickListener { openDashboard() }
    }

    private fun navigateToFragment(fragment: Fragment) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()

        fragTrans?.replace(R.id.content, fragment)
        fragTrans?.addToBackStack(fragment.javaClass.name)
        fragTrans?.commit()
    }

    private fun openDashboard() {
        val url = AppConstants.KEY_STATS.getUrl(requireContext(), getString(R.string.stats_url))
        val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }
}