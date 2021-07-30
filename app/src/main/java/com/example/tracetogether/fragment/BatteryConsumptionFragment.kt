package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tracetogether.R
import com.example.tracetogether.pause.PauseDialogFragment
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.underline
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

        toolbar.title = "battery_consumption_title".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }

        tv_description.setLocalizedString("battery_consumption_details")
        tv_pause_schedule_title.setLocalizedString("pause_title")
        tv_pause_details.setLocalizedString("pause_description")
        tv_go_to_pause_schedule.setLocalizedString("home_pause_set_schedule")
        tv_go_to_pause_schedule.underline()

        tv_go_to_pause_schedule?.setOnClickListener { gotoPauseSchedule() }
    }

    private fun gotoPauseSchedule() {
        PauseDialogFragment().show(childFragmentManager, "pauseDialog")
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}
