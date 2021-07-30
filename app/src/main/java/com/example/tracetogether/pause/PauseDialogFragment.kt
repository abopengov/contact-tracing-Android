package com.example.tracetogether.pause

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.formatTime
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_pause.*


class PauseDialogFragment : DialogFragment() {

    private val viewModel: PauseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pause, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "pause_title".getLocalizedText()
        toolbar.setNavigationOnClickListener { dismiss() }
        tv_description?.setLocalizedString("pause_description")
        tv_schedule?.setLocalizedString("pause_schedule")
        tv_start_time?.setLocalizedString("pause_start_time")
        tv_end_time?.setLocalizedString("pause_end_time")
        btn_edit.setLocalizedString("pause_edit")
        btn_edit.setOnClickListener {
            val pauseTimeDialog = PauseTimeDialog {
                viewModel.reload()
            }

            pauseTimeDialog.show(childFragmentManager, "pauseTimeDialog")
        }

        viewModel.pauseScheduled.observe(viewLifecycleOwner, Observer { setTimes ->
            pause_switch.isChecked = setTimes
            group_time_setup.visibility = if (setTimes) View.VISIBLE else View.GONE
        })

        viewModel.pauseStartTime.observe(viewLifecycleOwner, Observer { pauseStartTime ->
            tv_start_time_value.text = pauseStartTime.formatTime()
        })

        viewModel.pauseEndTime.observe(viewLifecycleOwner, Observer { pauseEndTime ->
            tv_end_time_value.text = pauseEndTime.formatTime()
        })

        pause_switch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setScheduled(isChecked)
        }
    }
}
