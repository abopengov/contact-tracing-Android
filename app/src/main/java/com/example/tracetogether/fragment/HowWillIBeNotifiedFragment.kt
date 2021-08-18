package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.getLocalizedText
import kotlinx.android.synthetic.main.fragment_permissions.*

class HowWillIBeNotifiedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_how_will_i_be_notified, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "how_will_i_be_notified_title".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }

        val adapter = HowWillIBeNotifiedPagerAdapter(this)
        learnMorePagingView.setAdapter(adapter)
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}

internal class HowWillIBeNotifiedPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LearnMoreCardFragment.newInstance(
                R.drawable.how_will_i_be_notified_screen_01,
                "how_will_i_be_notified_page1_details"
            )
            1 -> LearnMoreCardFragment.newInstance(
                R.drawable.how_will_i_be_notified_screen_02,
                "how_will_i_be_notified_page2_details"
            )
            2 -> LearnMoreCardFragment.newInstance(
                    R.drawable.how_will_i_be_notified_screen_03,
                    "how_will_i_be_notified_page3_details"
            )
            3 -> LearnMoreCardFragment.newInstance(
                    R.drawable.how_will_i_be_notified_screen_04,
                    "how_will_i_be_notified_page4_details"
            )
            else -> throw RuntimeException("Invalid position $position")
        }
    }
}
