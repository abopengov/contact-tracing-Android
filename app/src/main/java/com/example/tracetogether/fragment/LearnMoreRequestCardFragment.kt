package com.example.tracetogether.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_learn_more_request_card.*

class LearnMoreRequestCardFragment : Fragment() {
    companion object {
        private const val EXTRA_ICON = "EXTRA_ICON"
        private const val EXTRA_WILL_TITLE = "EXTRA_WILL_TITLE"
        private const val EXTRA_WILL_ITEM_1 = "EXTRA_WILL_ITEM_1"
        private const val EXTRA_WILL_ITEM_2 = "EXTRA_WILL_ITEM_2"
        private const val EXTRA_WILL_NOT_TITLE = "EXTRA_WILL_NOT_TITLE"
        private const val EXTRA_WILL_NOT_ITEM_1 = "EXTRA_WILL_NOT_ITEM_1"
        private const val EXTRA_WILL_NOT_ITEM_2 = "EXTRA_WILL_NOT_ITEM_2"

        fun newInstance(
            @DrawableRes icon: Int?,
            willTitle: String,
            willItem1: Item,
            willItem2: Item?,
            willNotTitle: String,
            willNotItem1: Item,
            willNotItem2: Item?
        ): LearnMoreRequestCardFragment {
            val args = Bundle()
            args.putInt(EXTRA_ICON, icon ?: 0)
            args.putString(EXTRA_WILL_TITLE, willTitle)
            args.putParcelable(EXTRA_WILL_ITEM_1, willItem1)
            args.putParcelable(EXTRA_WILL_ITEM_2, willItem2)
            args.putString(EXTRA_WILL_NOT_TITLE, willNotTitle)
            args.putParcelable(EXTRA_WILL_NOT_ITEM_1, willNotItem1)
            args.putParcelable(EXTRA_WILL_NOT_ITEM_2, willNotItem2)
            val fragment = LearnMoreRequestCardFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_more_request_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val icon = it.getInt(EXTRA_ICON)
            if (icon != 0) {
                iv_icon?.setImageResource(icon)
            } else {
                iv_icon?.visibility = View.GONE
            }

            tv_will_title?.setLocalizedString(it.getString(EXTRA_WILL_TITLE, ""))
            updateItem(iv_will_item1, tv_will_item1, it.getParcelable<Item>(EXTRA_WILL_ITEM_1))
            updateItem(iv_will_item2, tv_will_item2, it.getParcelable<Item>(EXTRA_WILL_ITEM_2))
            tv_will_not_title?.setLocalizedString(it.getString(EXTRA_WILL_NOT_TITLE, ""))
            updateItem(
                iv_will_not_item1,
                tv_will_not_item1,
                it.getParcelable<Item>(EXTRA_WILL_NOT_ITEM_1)
            )
            updateItem(
                iv_will_not_item2,
                tv_will_not_item2,
                it.getParcelable<Item>(EXTRA_WILL_NOT_ITEM_2)
            )
        }
    }

    private fun updateItem(
        imageView: ImageView?,
        textView: MaterialTextView?,
        item: Item?
    ) {
        if (item != null) {
            imageView?.setImageResource(item.icon)
            textView?.setLocalizedString(item.text)
        } else {
            imageView?.visibility = View.GONE
            textView?.visibility = View.GONE
        }
    }

    @Parcelize
    data class Item(@DrawableRes val icon: Int, val text: String) : Parcelable
}