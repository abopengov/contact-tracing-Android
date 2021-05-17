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
import kotlinx.android.synthetic.main.fragment_permissions.*

class PermissionsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton?.setOnClickListener { goBack() }

        tv_permissions?.setLocalizedString("permissions_title")

        val adapter = PermissionsPagerAdapter(this)
        learnMorePagingView.setAdapter(adapter)
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}

internal class PermissionsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LearnMoreRequestCardFragment(
                R.drawable.ic_location,
                "permissions_page1_will_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_blue_circle_check,
                    "permissions_page1_will_item1"
                ),
                null,
                "permissions_page1_will_not_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_red_circle_x,
                    "permissions_page1_will_not_item1"
                ),
                null
            )
            1 -> LearnMoreRequestCardFragment(
                R.drawable.ic_bluetooth,
                "permissions_page2_will_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_blue_circle_check,
                    "permissions_page2_will_item1"
                ),
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_blue_circle_check, "permissions_page2_will_item2"
                ),
                "permissions_page2_will_not_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_red_circle_x,
                    "permissions_page2_will_not_item1"
                ),
                null
            )
            2 -> LearnMoreRequestCardFragment(
                R.drawable.ic_phone,
                "permissions_page3_will_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_blue_circle_check, "permissions_page3_will_item1"
                ),
                null,
                "permissions_page3_will_not_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_red_circle_x,
                    "permissions_page3_will_not_item1"
                ),
                null
            )
            3 -> LearnMoreRequestCardFragment(
                R.drawable.ic_bell,
                "permissions_page4_will_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_blue_circle_check, "permissions_page4_will_item1"
                ),
                null,
                "permissions_page4_will_not_title",
                LearnMoreRequestCardFragment.Item(
                    R.drawable.ic_red_circle_x, "permissions_page4_will_not_item1"
                ),
                null
            )
            else -> throw RuntimeException("Invalid position $position")
        }
    }
}

