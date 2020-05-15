package com.example.tracetogether.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.button_and_progress.*
import kotlinx.android.synthetic.main.fragment_register_number.*

abstract class OnboardingFragmentInterface : Fragment() {
    abstract fun onButtonClick(buttonView: View)
    abstract fun onBackButtonClick(buttonView: View)
    abstract fun onUpdatePhoneNumber(num: String)
    abstract fun onError(error: String)

    private var actionButton: View? = null
    private var backButton: View? = null

    abstract fun becomesVisible()

    private fun setupButton() {
        onboardingButton?.let {
            actionButton = it
            it.setOnClickListener { buttonView ->
                onButtonClick(buttonView)
            }
        }
        onboardingBackButton?.let {
            backButton = it
            it.setOnClickListener { buttonView ->
                onBackButtonClick(buttonView)
            }
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

    fun setButtonText(string: String){
        onboardingButtonText?.let{
            it.text = string
        }
    }

    fun setButtonIcon(id: Int){
        onboardingButtonIcon?.let{
            it.setImageResource(id)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButton()
    }


}
