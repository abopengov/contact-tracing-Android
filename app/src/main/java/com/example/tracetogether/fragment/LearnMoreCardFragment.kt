package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_learn_more_card.*

class LearnMoreCardFragment : Fragment() {
    companion object {
        private const val EXTRA_BACKGROUND = "EXTRA_BACKGROUND"
        private const val EXTRA_TEXT = "EXTRA_TEXT"

        fun newInstance(
            @DrawableRes background: Int,
            text: String
        ): LearnMoreCardFragment {
            val args = Bundle()
            args.putInt(EXTRA_BACKGROUND, background)
            args.putString(EXTRA_TEXT, text)
            val fragment = LearnMoreCardFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            iv_card_image.setImageResource(it.getInt(EXTRA_BACKGROUND))
            tv_description?.setLocalizedString(it.getString(EXTRA_TEXT, ""))
        }
    }
}
