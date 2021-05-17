package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_battery_consumption.*

class BatteryConsumptionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_battery_consumption, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_battery_consumption.setLocalizedString("battery_consumption_title")

        val learnMoreCardFragment = LearnMoreCardFragment(
            R.drawable.battery_consumption_screen_01,
            "battery_consumption_details"
        )

        val fragTrans: FragmentTransaction = requireActivity()
            .supportFragmentManager
            .beginTransaction()
        fragTrans.add(R.id.batteryCardView, learnMoreCardFragment)
        fragTrans.commit()

        backButton?.setOnClickListener { goBack() }
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}
