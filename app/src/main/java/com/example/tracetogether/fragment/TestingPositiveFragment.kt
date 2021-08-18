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

class TestingPositiveFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_testing_positive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "testing_positive_title".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }

        val adapter = TestingPositivePagerAdapter(this)
        learnMorePagingView.setAdapter(adapter)
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}

internal class TestingPositivePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LearnMoreCardFragment.newInstance(
                R.drawable.testing_positive_screen_01,
                "testing_positive_page1_details"
            )
            1 -> LearnMoreCardFragment.newInstance(
                R.drawable.testing_positive_screen_02,
                "testing_positive_page2_details"
            )
            2 -> LearnMoreCardFragment.newInstance(
                R.drawable.testing_positive_screen_03,
                "testing_positive_page3_details"
            )
            3 -> LearnMoreCardFragment.newInstance(
                R.drawable.testing_positive_screen_04,
                "testing_positive_page4_details"
            )
            else -> throw RuntimeException("Invalid position $position")
        }
    }
}

