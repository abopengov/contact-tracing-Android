package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.R
import com.example.tracetogether.model.CovidTestData
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.setLocalizedStringHint
import com.example.tracetogether.util.MonthPreviousDateValidator
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_test_date.*
import java.text.SimpleDateFormat
import java.util.*

class TestDateFragment : Fragment() {
    companion object {
        private const val EXTRA_MHR_FLOW = "EXTRA_MHR_FLOW"

        fun newInstance(mhrFlow: Boolean): TestDateFragment {
            val fragment = TestDateFragment()
            val bundle = Bundle(1)
            bundle.putBoolean(EXTRA_MHR_FLOW, mhrFlow)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test_date, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_next?.setLocalizedString("next_button")
        tv_test_date_title?.setLocalizedString("upload_test_date_title")
        et_test_date?.setLocalizedStringHint("upload_enter_date_hint")
        switch_covid_symptoms?.setLocalizedString("upload_symptoms_toggle_text")
        tv_symptoms_date_title?.setLocalizedString("symptoms_date_title")
        et_symtoms_date?.setLocalizedStringHint("upload_enter_date_hint")

        et_test_date.doOnTextChanged { _, _, _, _ -> btn_next.isEnabled = canProceed() }
        et_test_date.setOnClickListener(::showDatePicker)
        et_symtoms_date.doOnTextChanged { _, _, _, _ -> btn_next.isEnabled = canProceed() }
        et_symtoms_date.setOnClickListener(::showDatePicker)

        btn_next.isEnabled = false

        switch_covid_symptoms.setOnCheckedChangeListener { _, isChecked ->
            btn_next.isEnabled = canProceed()

            val visibility = if (isChecked) View.VISIBLE else View.GONE

            tv_symptoms_date_title.visibility = visibility
            et_symtoms_date.visibility = visibility
        }

        val mhrFlow = arguments?.getBoolean(EXTRA_MHR_FLOW) ?: false
        btn_next?.setOnClickListener {
            getCovidTestData()?.let { covidTestData ->
                if (mhrFlow) {
                    navigateToUploadPin(covidTestData)
                } else {
                    navigateToVerifyCaller(covidTestData)
                }
            }
        }

        toolbar.setNavigationOnClickListener {
            val fragManager: FragmentManager? = activity?.supportFragmentManager
            fragManager?.popBackStack()
        }
    }

    private fun canProceed(): Boolean {
        return et_test_date.text.isNotEmpty()
                && (!switch_covid_symptoms.isChecked || et_symtoms_date.text.isNotEmpty())
    }

    private fun showDatePicker(view: View) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.MONTH, -1)
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(MonthPreviousDateValidator())
            .build()

        val editText = view as EditText
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(calendarConstraints)
            .build()
        datePicker.show(childFragmentManager, datePicker.toString())
        datePicker.addOnPositiveButtonClickListener { lDate ->
            val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            c.timeInMillis = lDate

            val simpleFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            editText.setText(simpleFormat.format(c.time))
        }
    }

    private fun getCovidTestData(): CovidTestData? {
        return et_test_date.timeInMillis()?.let { testDate ->
            val symptomsDate: Long? = if (switch_covid_symptoms.isChecked) {
                et_symtoms_date.timeInMillis()
            } else null

            CovidTestData(testDate, symptomsDate)
        }
    }

    private fun navigateToUploadPin(covidTestData: CovidTestData) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()
        val fragB = EnterPinFragment.newInstance(covidTestData)

        fragTrans?.replace(R.id.content, fragB)
        fragTrans?.addToBackStack(EnterPinFragment::class.java.name)
        fragTrans?.commit()
    }

    private fun navigateToVerifyCaller(covidTestData: CovidTestData) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()
        val fragB = VerifyCallerFragment.newInstance(covidTestData)

        fragTrans?.replace(R.id.content, fragB)
        fragTrans?.addToBackStack(VerifyCallerFragment::class.java.name)
        fragTrans?.commit()
    }
}

private fun EditText.timeInMillis(): Long? {
    val simpleFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    return if (text.toString().isNotEmpty()) {
        simpleFormat.parse(text.toString())?.time
    } else null
}
