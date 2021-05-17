package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.MainActivity
import com.example.tracetogether.Preference
import kotlinx.android.synthetic.main.fragment_upload_foruse.*
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.hide
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.show

/*
    Fragment for the "For use only by" screen in the Upload flow,
 */
class ForUseFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_foruse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_proceed?.setLocalizedString("proceed_with_a_ct")
        btn_proceed_mhr?.setLocalizedString("proceed_through_mhr")
        tv_title?.setLocalizedString("ahs_only")
        tv_desc?.setLocalizedString("tap_next")


        if (Preference.shouldShowFeatureMHR(context!!)) {
            forUseFragmentActionButtonMRH?.show()
        } else {
            forUseFragmentActionButtonMRH?.hide()
        }


        forUseFragmentActionButtonCT?.setOnClickListener {
            navigateToTestDateFragment(false)
        }

        forUseFragmentActionButtonMRH?.setOnClickListener {
            navigateToTestDateFragment(true)
        }

        backButton?.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun navigateToTestDateFragment(mhrFlow: Boolean) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()
        val fragB = TestDateFragment(mhrFlow)

        fragTrans?.replace(R.id.content, fragB)
        fragTrans?.addToBackStack(TestDateFragment::class.java.name)
        fragTrans?.commit()
    }
}
