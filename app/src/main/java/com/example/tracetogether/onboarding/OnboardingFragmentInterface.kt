package com.example.tracetogether.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_register_number.*

abstract class OnboardingFragmentInterface : Fragment() {
    abstract fun onButtonClick(buttonView: View)
    abstract fun onBackButtonClick()
    abstract fun onUpdatePhoneNumber(num: String)
    abstract fun onError(error: String)

    private var actionButton: View? = null

    abstract fun becomesVisible()

    private fun setupButton() {
        btn_next?.let {
            actionButton = it
            it.setOnClickListener { buttonView ->
                onButtonClick(buttonView)
            }
        }

        toolbar?.setNavigationOnClickListener {
            onBackButtonClick()
        }
    }

    fun enableButton() {
        actionButton?.let {
            it.isEnabled = true
        }
    }

    fun disableButton() {
        actionButton?.let {
            it.isEnabled = false
        }
    }

    fun setButtonText(string: String) {
        btn_next?.let {
            it.text = string
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButton()
    }


}
