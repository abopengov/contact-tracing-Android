package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_app_basics.backButton
import kotlinx.android.synthetic.main.fragment_app_basics.learnMorePagingView
import kotlinx.android.synthetic.main.fragment_info_exchanged.*


class InfoExchangedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info_exchanged, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton?.setOnClickListener { goBack() }

        tv_info_exchanged?.setLocalizedString("info_exchanged_title")

        val adapter = InfoExchangedPagerAdapter(this)
        learnMorePagingView.setAdapter(adapter)
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}

internal class InfoExchangedPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LearnMoreCardFragment(
                R.drawable.info_exchanged_screen_01,
                "info_exchanged_page1_details"
            )
            1 -> LearnMoreRequestCardFragment(
                    null,
                "info_exchanged_page2_title1",
                LearnMoreRequestCardFragment.Item(R.drawable.ic_phone, "info_exchanged_page2_details1"),
                null,
                "info_exchanged_page2_title2",
                LearnMoreRequestCardFragment.Item(R.drawable.ic_anonymous, "info_exchanged_page2_details2"),
                LearnMoreRequestCardFragment.Item(R.drawable.ic_blue_x, "info_exchanged_page2_details3")
            )
            2 -> LearnMorePayloadExampleFragment()
            3 -> LearnMoreCardFragment(
                R.drawable.info_exchanged_screen_04,
                "info_exchanged_page4_details"
            )
            else -> throw RuntimeException("Invalid position $position")
        }
    }
}