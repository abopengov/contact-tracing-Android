package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_learn_more_two_item_card.*

class LearnMoreTwoItemCardFragment : Fragment() {
    companion object {
        private const val EXTRA_BACKGROUND = "EXTRA_BACKGROUND"
        private const val EXTRA_DESCRIPTION_1_TEXT = "EXTRA_DESCRIPTION_1_TEXT"
        private const val EXTRA_DESCRIPTION_1_ICON = "EXTRA_DESCRIPTION_1_ICON"
        private const val EXTRA_DESCRIPTION_2_TEXT = "EXTRA_DESCRIPTION_2_TEXT"
        private const val EXTRA_DESCRIPTION_2_ICON = "EXTRA_DESCRIPTION_2_ICON"

        fun newInstance(
            @DrawableRes background: Int,
            description1Text: String,
            @DrawableRes description1Icon: Int,
            description2Text: String,
            @DrawableRes description2Icon: Int
        ): LearnMoreTwoItemCardFragment {
            val args = Bundle()
            args.putInt(EXTRA_BACKGROUND, background)
            args.putString(EXTRA_DESCRIPTION_1_TEXT, description1Text)
            args.putInt(EXTRA_DESCRIPTION_1_ICON, description1Icon)
            args.putString(EXTRA_DESCRIPTION_2_TEXT, description2Text)
            args.putInt(EXTRA_DESCRIPTION_2_ICON, description2Icon)

            val fragment = LearnMoreTwoItemCardFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more_two_item_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            iv_card_image.setImageResource(it.getInt(EXTRA_BACKGROUND))
            tv_description1?.setLocalizedString(it.getString(EXTRA_DESCRIPTION_1_TEXT, ""))
            tv_description2?.setLocalizedString(it.getString(EXTRA_DESCRIPTION_2_TEXT, ""))
            iv_description1?.setImageResource(it.getInt(EXTRA_DESCRIPTION_1_ICON))
            iv_description2?.setImageResource(it.getInt(EXTRA_DESCRIPTION_2_ICON))
        }

    }
}
