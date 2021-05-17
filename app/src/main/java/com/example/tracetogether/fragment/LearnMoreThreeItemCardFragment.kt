package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_learn_more_three_item_card.*

class LearnMoreThreeItemCardFragment(
    val title: String,
    val description1Text: String,
    @DrawableRes val description1Icon: Int,
    val description2Text: String,
    @DrawableRes val description2Icon: Int,
    val description3Text: String,
    @DrawableRes val description3Icon: Int
) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more_three_item_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString(title)
        tv_description1?.setLocalizedString(description1Text)
        tv_description2?.setLocalizedString(description2Text)
        tv_description3?.setLocalizedString(description3Text)
        iv_description1?.setImageResource(description1Icon)
        iv_description2?.setImageResource(description2Icon)
        iv_description3?.setImageResource(description3Icon)
    }
}
