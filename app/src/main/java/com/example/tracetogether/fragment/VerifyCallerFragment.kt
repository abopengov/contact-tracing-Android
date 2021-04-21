package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.model.CovidTestData
import com.example.tracetogether.util.Extensions.setLocalizedString

/*
    Fragment for the Verify caller screen in the Upload flow,
 */
class VerifyCallerFragment(private val covidTestData: CovidTestData) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_verifycaller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString("verify")
        tv_desc?.setLocalizedString("code_match")
        btn_next?.setLocalizedString("next_button")

        verifyCallerFragmentVerificationCode?.text = Preference.getUUID(view.context)
        verifyCallerFragmentActionButton?.setOnClickListener {
            navigateToUploadPin(covidTestData)
        }
        verifyCallerBackButton?.setOnClickListener {
            val fragManager: FragmentManager? = activity?.supportFragmentManager
            fragManager?.popBackStack()
        }
    }

    private fun navigateToUploadPin(covidTestData: CovidTestData) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()
        val fragB = EnterPinFragment(false, covidTestData)

        fragTrans?.replace(R.id.content, fragB)
        fragTrans?.addToBackStack(EnterPinFragment::class.java.name)
        fragTrans?.commit()
    }
}
