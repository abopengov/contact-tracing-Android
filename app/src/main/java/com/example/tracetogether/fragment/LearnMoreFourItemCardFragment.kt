package com.example.tracetogether.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.addHyperlink
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_learn_more_four_item_card.*


class LearnMoreFourItemCardFragment : Fragment() {
    companion object {
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_DESCRIPTION_1_TEXT = "EXTRA_DESCRIPTION_1_TEXT"
        private const val EXTRA_DESCRIPTION_1_ICON = "EXTRA_DESCRIPTION_1_ICON"
        private const val EXTRA_DESCRIPTION_2_TEXT = "EXTRA_DESCRIPTION_2_TEXT"
        private const val EXTRA_DESCRIPTION_2_ICON = "EXTRA_DESCRIPTION_2_ICON"
        private const val EXTRA_DESCRIPTION_3_TEXT = "EXTRA_DESCRIPTION_3_TEXT"
        private const val EXTRA_DESCRIPTION_3_ICON = "EXTRA_DESCRIPTION_3_ICON"
        private const val EXTRA_DESCRIPTION_4_TEXT = "EXTRA_DESCRIPTION_4_TEXT"
        private const val EXTRA_DESCRIPTION_4_ICON = "EXTRA_DESCRIPTION_4_ICON"
        private const val EXTRA_HYPERLINKS = "EXTRA_HYPERLINKS"

        fun newInstance(
            title: String,
            description1Text: String,
            @DrawableRes description1Icon: Int,
            description2Text: String,
            @DrawableRes description2Icon: Int,
            description3Text: String,
            @DrawableRes description3Icon: Int,
            description4Text: String,
            @DrawableRes description4Icon: Int,
            hyperlinks: List<Hyperlink>
        ): LearnMoreFourItemCardFragment {
            val args = Bundle()
            args.putString(EXTRA_TITLE, title)
            args.putString(EXTRA_DESCRIPTION_1_TEXT, description1Text)
            args.putInt(EXTRA_DESCRIPTION_1_ICON, description1Icon)
            args.putString(EXTRA_DESCRIPTION_2_TEXT, description2Text)
            args.putInt(EXTRA_DESCRIPTION_2_ICON, description2Icon)
            args.putString(EXTRA_DESCRIPTION_3_TEXT, description3Text)
            args.putInt(EXTRA_DESCRIPTION_3_ICON, description3Icon)
            args.putString(EXTRA_DESCRIPTION_4_TEXT, description4Text)
            args.putInt(EXTRA_DESCRIPTION_4_ICON, description4Icon)
            args.putParcelableArray(EXTRA_HYPERLINKS, hyperlinks.toTypedArray())
            val fragment = LearnMoreFourItemCardFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more_four_item_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            tv_title?.setLocalizedString(it.getString(EXTRA_TITLE, ""))
            tv_description1?.setLocalizedString(it.getString(EXTRA_DESCRIPTION_1_TEXT, ""))
            tv_description2?.setLocalizedString(it.getString(EXTRA_DESCRIPTION_2_TEXT, ""))
            tv_description3?.setLocalizedString(it.getString(EXTRA_DESCRIPTION_3_TEXT, ""))
            tv_description4?.setLocalizedString(it.getString(EXTRA_DESCRIPTION_4_TEXT, ""))
            iv_description1?.setImageResource(it.getInt(EXTRA_DESCRIPTION_1_ICON))
            iv_description2?.setImageResource(it.getInt(EXTRA_DESCRIPTION_2_ICON))
            iv_description3?.setImageResource(it.getInt(EXTRA_DESCRIPTION_3_ICON))
            iv_description4?.setImageResource(it.getInt(EXTRA_DESCRIPTION_4_ICON))
        }

        setUpLinks()
    }

    private fun setUpLinks() {
        arguments?.getParcelableArray(EXTRA_HYPERLINKS)?.forEach {
            (it as? Hyperlink)?.let { hyperlink ->
                val translatedTextToFind = hyperlink.textToFind.getLocalizedText()
                getItemByIndex(hyperlink.itemIndex)?.let { textView ->
                    textView.addHyperlink(translatedTextToFind) {
                        openUrl(hyperlink.linkURL)
                    }
                }
            }
        }
    }

    private fun openUrl(url: String) {
        val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }

    private fun getItemByIndex(itemIndex: Int): TextView? {
        return when (itemIndex) {
            1 -> tv_description1
            2 -> tv_description2
            3 -> tv_description3
            4 -> tv_description4
            else -> null
        }
    }
}

@Parcelize
data class Hyperlink(val itemIndex: Int, val textToFind: String, val linkURL: String) : Parcelable