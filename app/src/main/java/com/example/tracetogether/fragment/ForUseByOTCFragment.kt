package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.MainActivity
import com.example.tracetogether.R
import com.example.tracetogether.Utils

class ForUseByOTCFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forusebyotc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val childFragMan: FragmentManager = childFragmentManager
        val childFragTrans: FragmentTransaction = childFragMan.beginTransaction()
        val fragB = ForUseFragment()
        childFragTrans.add(R.id.fragment_placeholder, fragB)
        childFragTrans.addToBackStack("VerifyCaller")
        childFragTrans.commit()
    }

    fun goToUploadFragment(isEnterPin: Boolean) {
        val parentActivity: MainActivity? = activity as MainActivity?
        parentActivity?.let{
            it.openFragment(
                    parentActivity.LAYOUT_MAIN_ID,
                    UploadPageFragment(isEnterPin),
                    UploadPageFragment::class.java.name,
                    0
            )
        }?:(Utils.restartAppWithNoContext(0,"ForUseByOTCFragment not attached to MainActivity"))

    }
}
