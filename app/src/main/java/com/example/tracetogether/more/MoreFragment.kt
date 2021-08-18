package com.example.tracetogether.more

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class MoreFragment : Fragment(), CoroutineScope by MainScope() {
    private val moreViewModel: MoreViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_more.setLocalizedString("more_title")

        val adapter = MoreLinksAdapter(::openLink)
        rv_more_links.adapter = adapter

        moreViewModel.networkError.observe(viewLifecycleOwner, Observer { networkError ->
            if (networkError) {
                Snackbar.make(coordinator_layout, "error_cannot_fetch_data".getLocalizedText(), Snackbar.LENGTH_SHORT)
                        .show()
            }
        })

        moreViewModel.moreLinks.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        moreViewModel.fetchMoreLinks(requireContext())
    }

    private fun openLink(moreLink: MoreLink) {
        val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(moreLink.link))
    }
}
