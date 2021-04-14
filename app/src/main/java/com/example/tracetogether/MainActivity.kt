package com.example.tracetogether

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tracetogether.fragment.*
import com.example.tracetogether.herald.FairEfficacyInstrumentation
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.onboarding.PreOnboardingActivity
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.google.android.material.bottomnavigation.BottomNavigationView
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
                        R.id.navigation_statistics -> {
                            if (selected != R.id.navigation_statistics) {
                                var fragment = WebViewFragment().newInstance(AppConstants.KEY_STATS, "")
                                openFragment(
                                        LAYOUT_MAIN_ID, fragment,
                                        fragment::class.java.name, 0
                                )
                            }

                            selected = R.id.navigation_statistics
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
                        R.id.navigation_guidance -> {
                            if (selected != R.id.navigation_guidance) {
                                openFragment(
                                        LAYOUT_MAIN_ID, GuidanceFragment(),
                                        GuidanceFragment::class.java.name, 0
                                )
                            }

                            selected = R.id.navigation_guidance
                            return@OnNavigationItemSelectedListener true
                        }
                    }
                    false
                }

        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        nav_view?.menu?.findItem(R.id.navigation_home)?.title = "menu_home".getLocalizedText()
        nav_view?.menu?.findItem(R.id.navigation_statistics)?.title =
                "menu_statistics".getLocalizedText()
        nav_view?.menu?.findItem(R.id.navigation_help)?.title = "menu_help".getLocalizedText()
        nav_view?.menu?.findItem(R.id.navigation_guidance)?.title =
                "menu_guidance".getLocalizedText()

        goToHome()
    }

    override fun onResume() {
        super.onResume()

        if (!FairEfficacyInstrumentation.enabled) {
            showAppRegistrationStatus()
        }
    }

    private fun showAppRegistrationStatus() = launch {
        if (!Utils.checkIfAppRegistered()) {
            CentralLog.i(TAG, "App version not supported, stopping BluetoothMonitoringService")
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
        openFragment(
                LAYOUT_MAIN_ID, HomeFragment(),
                HomeFragment::class.java.name, 0
        )
        nav_view.selectedItemId = R.id.navigation_home
    }

    fun goStatistics() {
        nav_view.selectedItemId = R.id.navigation_statistics
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