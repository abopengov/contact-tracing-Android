package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.fragment_learn_more_request_card.*

class LearnMoreRequestCardFragment(
    @DrawableRes private val icon: Int?,
    private val willTitle: String,
    private val willItem1: Item,
    private val willItem2: Item?,
    private val willNotTitle: String,
    private val willNotItem1: Item,
    private val willNotItem2: Item?
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more_request_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (icon != null) {
            iv_icon?.setImageResource(icon)
        }
        else {
            iv_icon?.visibility = View.GONE
        }

        tv_will_title?.setLocalizedString(willTitle)

        updateItem(iv_will_item1, tv_will_item1, willItem1)
        updateItem(iv_will_item2, tv_will_item2, willItem2)

        tv_will_not_title?.setLocalizedString(willNotTitle)

        updateItem(iv_will_not_item1, tv_will_not_item1, willNotItem1)
        updateItem(iv_will_not_item2, tv_will_not_item2, willNotItem2)
    }

    private fun updateItem(
        imageView: ImageView?,
        textView: MaterialTextView?,
        item: Item?
    ) {
        if (item != null) {
            imageView?.setImageResource(item.icon)
            textView?.setLocalizedString(item.text)
        }
        else {
            imageView?.visibility = View.GONE
            textView?.visibility = View.GONE
        }
    }

    data class Item(@DrawableRes val icon: Int, val text: String)
}