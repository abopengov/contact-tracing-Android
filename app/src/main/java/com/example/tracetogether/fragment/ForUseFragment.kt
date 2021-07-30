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
import com.example.tracetogether.util.Extensions.hide
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.show
import kotlinx.android.synthetic.main.fragment_upload_foruse.*

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

        btn_ct?.setLocalizedString("proceed_with_a_ct")
        btn_mhr?.setLocalizedString("proceed_through_mhr")
        tv_title?.setLocalizedString("ahs_only")
        tv_desc?.setLocalizedString("tap_next")


        if (Preference.shouldShowFeatureMHR(requireContext())) {
            btn_mhr?.show()
        } else {
            btn_mhr?.hide()
        }

        btn_ct?.setOnClickListener {
            navigateToTestDateFragment(false)
        }

        btn_mhr?.setOnClickListener {
            navigateToTestDateFragment(true)
        }

        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    private fun navigateToTestDateFragment(mhrFlow: Boolean) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()
        val fragB = TestDateFragment.newInstance(mhrFlow)

        fragTrans?.replace(R.id.content, fragB)
        fragTrans?.addToBackStack(TestDateFragment::class.java.name)
        fragTrans?.commit()
    }
}
