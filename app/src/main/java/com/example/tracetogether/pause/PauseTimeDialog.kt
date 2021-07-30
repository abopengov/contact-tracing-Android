package com.example.tracetogether.pause

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.formatTime
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.android.synthetic.main.fragment_pause_time.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class PauseTimeDialog(private val savedCallback: () -> Unit) : DialogFragment() {
    private val viewModel: PauseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog_SlideUp)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pause_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.title = "pause_schedule".getLocalizedText()
        toolbar.menu.add("pause_save".getLocalizedText().toUpperCase(Locale.ROOT))
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        toolbar.setOnMenuItemClickListener {
            viewModel.saveTimeRange()
            true
        }

        tv_start_time.setLocalizedString("pause_start_time")
        tv_end_time.setLocalizedString("pause_end_time")
        tv_info.setLocalizedString("pause_info")

        et_start_time_value.setOnClickListener(::showTimePicker)
        et_end_time_value.setOnClickListener(::showTimePicker)

        viewModel.pauseStartTime.observe(viewLifecycleOwner, Observer { pauseStartTime ->
            et_start_time_value.setText(pauseStartTime.formatTime())
        })

        viewModel.pauseEndTime.observe(viewLifecycleOwner, Observer { pauseEndTime ->
            et_end_time_value.setText(pauseEndTime.formatTime())
        })

        viewModel.invalidTimeRange.observe(viewLifecycleOwner, Observer { invalidTimes ->
            if (invalidTimes) {
                iv_info.setImageResource(R.drawable.ic_info_red)
                tv_info.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            } else {
                iv_info.setImageResource(R.drawable.ic_info_grey)
                tv_info.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_2))
            }
        })

        viewModel.timeRangeSaved.observe(viewLifecycleOwner, Observer { timeRangeSaved ->
            if (timeRangeSaved) {
                this.dismiss()
                savedCallback()
            } else {
                AlertDialog.Builder(requireActivity())
                    .setTitle("pause_error_dialog_title".getLocalizedText())
                    .setMessage("pause_error_dialog_message".getLocalizedText())
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .create()
                    .show()
            }
        })
    }

    private fun showTimePicker(view: View) {
        val editText = view as EditText
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .also {
                if (editText.text.isNotBlank()) {
                    val time = timeFromEditText(editText)
                    it.setHour(time.hour)
                    it.setMinute(time.minute)
                }
            }
            .build()
        timePicker.show(childFragmentManager, timePicker.toString())
        timePicker.addOnPositiveButtonClickListener {
            if (view == et_start_time_value) {
                viewModel.setStartTime(LocalTime.of(timePicker.hour, timePicker.minute))
            } else if (view == et_end_time_value) {
                viewModel.setEndTime(LocalTime.of(timePicker.hour, timePicker.minute))
            }
        }
    }

    private fun timeFromEditText(editText: EditText): LocalTime {
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return LocalTime.parse(editText.text.toString(), timeFormatter)
    }
}