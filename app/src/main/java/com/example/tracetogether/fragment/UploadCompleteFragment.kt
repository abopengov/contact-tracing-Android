package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracetogether.MainActivity
import kotlinx.android.synthetic.main.fragment_upload_uploadcomplete.*
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString

/*
    Fragment for the Upload Complete screen in the Upload flow,
 */
class UploadCompleteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_uploadcomplete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString("upload_complete_title")
        tv_desc?.setLocalizedString("upload_complete_desc")
        btn_finish?.setLocalizedString("finish_button")

        uploadCompleteFragmentActionButton?.setOnClickListener {
            goBackToHome()
        }
    }

    private fun goBackToHome() {
        val parentActivity = activity as MainActivity?
        parentActivity?.onBackPressed()
    }
}
