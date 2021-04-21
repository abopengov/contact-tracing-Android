package com.example.tracetogether.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.underline
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.learn_more_paging_view.view.*


class LearnMorePagingView(context: Context?, attrs: AttributeSet? = null) :
    MaterialCardView(context, attrs) {

    init {
        context?.let {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(
                R.layout.learn_more_paging_view,
                this,
                true
            )

            tv_next?.setLocalizedString("next")
            tv_next?.underline()
            tv_next?.setOnClickListener { goToNextTab() }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateNextButton(position)
                }
            })
        }
    }

    fun setAdapter(adapter: FragmentStateAdapter) {
        viewPager?.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ ->
        }.attach()
    }

    private fun updateNextButton(position: Int) {
        val lastTab = position == tabLayout.tabCount - 1
        if (lastTab) {
            tv_next?.visibility = View.GONE
        } else {
            tv_next?.visibility = View.VISIBLE
            tv_next?.setLocalizedString("next")

            val rightDrawable = ContextCompat.getDrawable(
                tabLayout.context,
                R.drawable.ic_next_arrow
            )
            tv_next?.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
        }
    }

    private fun goToNextTab() {
        if (viewPager.currentItem < tabLayout.tabCount - 1) {
            tabLayout.getTabAt(viewPager.currentItem + 1)?.select()
        }
    }
}
