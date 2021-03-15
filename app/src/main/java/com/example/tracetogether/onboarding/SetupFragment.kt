package com.example.tracetogether.onboarding

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.button_and_progress.*
import kotlinx.android.synthetic.main.fragment_setup.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/*
    Fragment for the app permission setup screen
 */
class SetupFragment : OnboardingFragmentInterface() {

    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "SetupFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onButtonClick(view: View) {
        CentralLog.d(TAG, "OnButtonClick 2")
        val activity = context as OnboardingActivity?
        activity?.let {
            it.enableBluetooth()
        } ?: (Utils.restartAppWithNoContext(0, "SetupFragment not attached to OnboardingActivity"))
    }

    override fun onBackButtonClick(view: View) {}

    override fun becomesVisible() {}

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_step?.setLocalizedString("setup_step")
        tv_title?.setLocalizedString("setup_app_permission")
        tv_desc?.text = HtmlCompat.fromHtml(
                "setup_app_permission_title".getLocalizedText(),
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        tv_desc_sub_1?.setLocalizedString("bluetooth")
        tv_desc_sub_2?.setLocalizedString("location_permissions")
        tv_desc_sub_3?.setLocalizedString("battery_optimiser_opt")
        tv_note_1?.setLocalizedString("notes")
        onboardingButtonText?.setLocalizedString("next_button")


    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SetupFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
