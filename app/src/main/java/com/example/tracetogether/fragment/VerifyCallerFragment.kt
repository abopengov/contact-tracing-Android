package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.model.CovidTestData
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*

/*
    Fragment for the Verify caller screen in the Upload flow,
 */
class VerifyCallerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_verifycaller, container, false)
    }

    companion object {
        private const val EXTRA_COVID_TEST_DATA = "EXTRA_COVID_TEST_DATA"

        fun newInstance(covidTestData: CovidTestData): VerifyCallerFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_COVID_TEST_DATA, covidTestData)
            val fragment = VerifyCallerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString("verify")
        tv_desc?.setLocalizedString("code_match")
        btn_next?.setLocalizedString("next_button")

        verifyCallerFragmentVerificationCode?.text = Preference.getUUID(view.context)

        arguments?.getParcelable<CovidTestData>(EXTRA_COVID_TEST_DATA)?.let { covidTestData ->
            btn_next?.setOnClickListener {
                navigateToUploadPin(covidTestData)
            }
        }

        toolbar.setNavigationOnClickListener {
            val fragManager: FragmentManager? = activity?.supportFragmentManager
            fragManager?.popBackStack()
        }
    }

    private fun navigateToUploadPin(covidTestData: CovidTestData) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()
        val enterPinFragment = EnterPinFragment.newInstance(covidTestData)

        fragTrans?.replace(R.id.content, enterPinFragment)
        fragTrans?.addToBackStack(EnterPinFragment::class.java.name)
        fragTrans?.commit()
    }
}
