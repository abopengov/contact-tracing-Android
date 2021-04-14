package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString("verify")
        tv_desc?.setLocalizedString("code_match")
        btn_next?.setLocalizedString("next_button")

        verifyCallerFragmentVerificationCode?.text = Preference.getUUID(view.context)
        verifyCallerFragmentActionButton?.setOnClickListener {
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.navigateToUploadPin()
        }
        verifyCallerBackButton?.setOnClickListener {
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.navigateToOTCFragment()
        }
    }
}

