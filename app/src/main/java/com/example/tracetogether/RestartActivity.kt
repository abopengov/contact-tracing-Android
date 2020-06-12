package com.example.tracetogether

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.logging.WFLog

class RestartActivity : AppCompatActivity() {
    private val TAG = "RestartActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Grabs extra intent data to see what type of error caused the restart
        //0 - default
        //1 - no userID
        val type = intent.getIntExtra("error_type",0)
        if(type == 1){
            restartOnboarding()
        }

        //Grabs extra intent data to see the reason of the restart
        val message = intent.getStringExtra("error_msg")
        message?.let{
            CentralLog.e(TAG,"$message, Restarting application")
            WFLog.logError("$message, Restarting application")
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.restart_msg))
        builder.setCancelable(false)
        builder.setPositiveButton("Ok") { dialog, id -> restartApp() }
        val alert = builder.create()
        alert.show()
    }

    private fun restartOnboarding() {
        Preference.putIsOnBoarded(this,false)
        val currentRetries = Preference.getUUIDRetryAttempts(this)
        Preference.putUUIDRetryAttempts(this,currentRetries+1)
    }

    private fun restartApp() {
        val intent = Intent(
            this.applicationContext,
            SplashActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
