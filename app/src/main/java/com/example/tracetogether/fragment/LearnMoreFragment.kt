package com.example.tracetogether.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.underline
import kotlinx.android.synthetic.main.fragment_learn_more.*

class LearnMoreFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_learn_more?.setLocalizedString("learn_title")
        tv_app_basics?.setLocalizedString("app_basics_title")
        tv_info_exchanged?.setLocalizedString("info_exchanged_title")
        tv_permissions?.setLocalizedString("permissions_title")
        tv_testing_positive?.setLocalizedString("testing_positive_title")
        tv_potential_exposures?.setLocalizedString("potential_exposures_title")
        tv_how_will_i_be_notified?.setLocalizedString("how_will_i_be_notified_title")
        tv_battery_consumption?.setLocalizedString("battery_consumption_title")
        tv_whats_new?.setLocalizedString("whats_new_title")
        tv_faq?.setLocalizedString("faq_title")

        tv_app_basics?.setOnClickListener { navigateToFragment(AppBasicsFragment()) }
        tv_info_exchanged?.setOnClickListener { navigateToFragment(InfoExchangedFragment()) }
        tv_permissions?.setOnClickListener { navigateToFragment(PermissionsFragment()) }
        tv_testing_positive?.setOnClickListener { navigateToFragment(TestingPositiveFragment()) }
        tv_potential_exposures?.setOnClickListener { navigateToFragment(PotentialExposuresFragment()) }
        tv_how_will_i_be_notified?.setOnClickListener { navigateToFragment(HowWillIBeNotifiedFragment()) }
        tv_battery_consumption?.setOnClickListener { navigateToFragment(BatteryConsumptionFragment()) }
        tv_whats_new?.setOnClickListener { navigateToFragment(WhatsNewFragment()) }
        tv_faq?.setOnClickListener { openFaq() }

        tv_whats_new?.underline()
        iv_whats_new_badge.visibility =
            if (Preference.userHasSeenWhatsNew(requireContext())) View.GONE else View.VISIBLE
    }

    private fun navigateToFragment(fragment: Fragment) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()

        fragTrans?.replace(R.id.content, fragment)
        fragTrans?.addToBackStack(fragment.javaClass.name)
        fragTrans?.commit()
    }

    private fun openFaq() {
        val url = AppConstants.KEY_FAQ.getUrl(requireContext(), getString(R.string.faq_url))

        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }
}
