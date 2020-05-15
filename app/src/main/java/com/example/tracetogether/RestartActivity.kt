package com.example.tracetogether

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RestartActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Preference.putIsOnBoarded(this,false)
        val currentRetries = Preference.getUUIDRetryAttempts(this)
        Preference.putUUIDRetryAttempts(this,currentRetries+1)

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.restart_msg))
        builder.setCancelable(false)
        builder.setPositiveButton("Ok") { dialog, id -> restartApp() }
        val alert = builder.create()
        alert.show()
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
