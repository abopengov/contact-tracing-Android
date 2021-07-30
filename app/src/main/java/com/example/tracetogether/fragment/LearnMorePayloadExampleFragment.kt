package com.example.tracetogether.fragment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_learn_payload_example.*

class LearnMorePayloadExampleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_payload_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val html = "<b>Temporary ID</b> = \"IaN9KiGGqA...\"<br><br><b>Phone Model</b> = \"Android\"<br><br><b>Signal Strengh</b> = \"-61\"<br><br><b>Transmit Power</b> = \"12\""

        tv_title?.setLocalizedString("info_exchanged_page3_title")
        tv_example?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            Html.fromHtml(html);
        }
    }
}
