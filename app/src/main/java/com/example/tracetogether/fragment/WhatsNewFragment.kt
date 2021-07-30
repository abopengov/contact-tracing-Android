package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.viewmodels.LearnMoreViewModel
import kotlinx.android.synthetic.main.fragment_whats_new.*


class WhatsNewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_whats_new, container, false)
    }

    private val viewModel: LearnMoreViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "whats_new_in_this_app_title".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }
        tv_whats_new_content?.loadUrl("file:///android_asset/changelog.html")

        viewModel.whatsNewPageSeen(true)
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }
}
