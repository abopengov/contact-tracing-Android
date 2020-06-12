package com.example.tracetogether

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.tracetogether.fragment.ForUseByOTCFragment
import com.example.tracetogether.fragment.HomeFragment
import com.example.tracetogether.fragment.HelpFragment
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.onboarding.PreOnboardingActivity
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/*
    Main activity when the user has been onboarded
 */
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val TAG = "MainActivity"

    // navigation
    private var mNavigationLevel = 0
    var LAYOUT_MAIN_ID = 0
    private var selected = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        Utils.startBluetoothMonitoringService(this)

        LAYOUT_MAIN_ID = R.id.content

        val mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {

                        if (selected != R.id.navigation_home) {
                            openFragment(
                                LAYOUT_MAIN_ID, HomeFragment(),
                                HomeFragment::class.java.name, 0
                            )
                        }
                        selected = R.id.navigation_home
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_upload -> {
                        if (selected != R.id.navigation_upload) {
                            openFragment(
                                LAYOUT_MAIN_ID, ForUseByOTCFragment(),
                                ForUseByOTCFragment::class.java.name, 0
                            )
                        }

                        selected = R.id.navigation_upload
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_help -> {
                        if (selected != R.id.navigation_help) {
                            openFragment(
                                LAYOUT_MAIN_ID, HelpFragment(),
                                HelpFragment::class.java.name, 0
                            )
                        }

                        selected = R.id.navigation_help
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }

        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        goToHome()

    }

    override fun onResume(){
        super.onResume()
        showAppRegistrationStatus()
    }

    private fun showAppRegistrationStatus() = launch {
        if (!Utils.checkIfAppRegistered()) {
            CentralLog.i(TAG,"App version not supported, stopping BluetoothMonitoringService")
            Utils.stopBluetoothMonitoringService(this@MainActivity)
            startActivity(Intent(this@MainActivity, PreOnboardingActivity::class.java))
            finish()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun goToHome() {
        nav_view.selectedItemId = R.id.navigation_home
    }

    fun openFragment(
        containerViewId: Int,
        fragment: Fragment,
        tag: String,
        title: Int
    ) {
        try { // pop all fragments
            supportFragmentManager.popBackStackImmediate(
                LAYOUT_MAIN_ID,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            mNavigationLevel = 0
            val transaction =
                supportFragmentManager.beginTransaction()
            transaction.replace(containerViewId, fragment, tag)
            transaction.commit()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
