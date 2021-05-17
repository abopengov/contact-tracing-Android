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
import kotlinx.android.synthetic.main.fragment_app_basics.*


class AppBasicsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_basics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton?.setOnClickListener { goBack() }

        tv_app_basics?.setLocalizedString("app_basics_title")

        val adapter = AppBasicsPagerAdapter(this)
        learnMorePagingView.setAdapter(adapter)
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}

internal class AppBasicsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LearnMoreCardFragment(R.drawable.app_basics_screen_01, "app_basics_page1_details")
            1 -> LearnMoreCardFragment(R.drawable.app_basics_screen_02, "app_basics_page2_details")
            2 -> LearnMoreTwoItemCardFragment(
                R.drawable.app_basics_screen_03,
                "app_basics_page3_details1",
                R.drawable.ic_anonymous,
                "app_basics_page3_details2",
                R.drawable.ic_blue_upload
            )
            else -> throw RuntimeException("Invalid position $position")
        }
    }
}