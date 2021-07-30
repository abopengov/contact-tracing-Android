package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tracetogether.R
import com.example.tracetogether.TracerApp
import com.example.tracetogether.util.AppConstants.KEY_CLOSE_CONTACTS_FAQ
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.getUrl
import kotlinx.android.synthetic.main.fragment_permissions.*

class PotentialExposuresFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_potential_exposures, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "potential_exposures_title".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }

        val adapter = PotentialExposuresPagerAdapter(this)
        learnMorePagingView.setAdapter(adapter)
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}

internal class PotentialExposuresPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LearnMoreCardFragment.newInstance(
                R.drawable.potential_exposures_screen_01,
                "potential_exposures_page1_details"
            )
            1 -> LearnMoreCardFragment.newInstance(
                R.drawable.potential_exposures_screen_02,
                "potential_exposures_page2_details"
            )
            2 -> LearnMoreCardFragment.newInstance(
                R.drawable.potential_exposures_screen_03,
                "potential_exposures_page3_details"
            )
            3 -> {
                val url = KEY_CLOSE_CONTACTS_FAQ.getUrl(
                    TracerApp.AppContext,
                    TracerApp.AppContext.getString(R.string.close_contacts_faq_url)
                )
                LearnMoreFourItemCardFragment.newInstance(
                    "potential_exposures_page4_title",
                    "potential_exposures_page4_details1",
                    R.drawable.ic_house,
                    "potential_exposures_page4_details2",
                    R.drawable.ic_positive,
                    "potential_exposures_page4_details3",
                    R.drawable.ic_pill_bottle,
                    "potential_exposures_page4_details4",
                    R.drawable.ic_help,
                    listOf(Hyperlink(4, "potential_exposures_page4_details4_link_text", url))
                )
            }
            else -> throw RuntimeException("Invalid position $position")
        }
    }
}
