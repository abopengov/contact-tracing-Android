package com.example.tracetogether.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.addHyperlink
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_learn_more_four_item_card.*


class LearnMoreFourItemCardFragment(
    val title: String,
    val description1Text: String,
    @DrawableRes val description1Icon: Int,
    val description2Text: String,
    @DrawableRes val description2Icon: Int,
    val description3Text: String,
    @DrawableRes val description3Icon: Int,
    val description4Text: String,
    @DrawableRes val description4Icon: Int
) : Fragment() {

    private val hyperlinks = mutableListOf<Hyperlink>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more_four_item_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString(title)
        tv_description1?.setLocalizedString(description1Text)
        tv_description2?.setLocalizedString(description2Text)
        tv_description3?.setLocalizedString(description3Text)
        tv_description4?.setLocalizedString(description4Text)
        iv_description1?.setImageResource(description1Icon)
        iv_description2?.setImageResource(description2Icon)
        iv_description3?.setImageResource(description3Icon)
        iv_description4?.setImageResource(description4Icon)

        setUpLinks()
    }

    fun addLinkToItem(itemIndex: Int, textToFind: String, linkURL: String) {
        hyperlinks.add(Hyperlink(itemIndex, textToFind, linkURL))
    }

    fun setUpLinks() {
        hyperlinks.forEach { hyperlink ->
            val translatedTextToFind = hyperlink.textToFind.getLocalizedText()
            getItemByIndex(hyperlink.itemIndex)?.let { textView ->
                textView.addHyperlink(translatedTextToFind) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(hyperlink.linkURL)
                    startActivity(intent)
                }
            }
        }
    }

    fun getItemByIndex(itemIndex: Int): TextView? {
        return when (itemIndex) {
            1 -> tv_description1
            2 -> tv_description2
            3 -> tv_description3
            4 -> tv_description4
            else -> null
        }
    }
}

internal data class Hyperlink(val itemIndex: Int, val textToFind: String, val linkURL: String)