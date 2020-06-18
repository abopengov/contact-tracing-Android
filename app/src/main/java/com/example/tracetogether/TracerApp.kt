package com.example.tracetogether

import android.app.Application
import android.content.Context
import android.os.Build
import com.example.tracetogether.idmanager.TempIDManager
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.services.BluetoothMonitoringService
import com.example.tracetogether.streetpass.CentralDevice
import com.example.tracetogether.streetpass.PeripheralDevice
import com.worklight.common.Logger
import com.worklight.common.WLAnalytics
import com.worklight.wlclient.api.WLClient

/*
    Entry point of the application
 */
class TracerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext = applicationContext

        // Initialize the IBM Mobile Foundation sdk
        WLClient.createInstance(this)
//        To pin certificate to application store certificate file in
//        src/main/assets and uncomment following line
//        WLClient.getInstance().pinTrustedCertificatePublicKey("mfpcertificate.cer")

        // Initialize IBM Analytics
        WLAnalytics.init(this)
        WLAnalytics.addDeviceEventListener(WLAnalytics.DeviceEvent.LIFECYCLE)
        Logger.setCapture(true)
        Logger.setLevel(Logger.LEVEL.ERROR);
        WLAnalytics.send()
        Logger.send()
    }

    companion object {

        private val TAG = "TracerApp"
        const val ORG = BuildConfig.ORG

        lateinit var AppContext: Context

        fun thisDeviceMsg(): String {
            BluetoothMonitoringService.broadcastMessage?.let {
                CentralLog.i(TAG, "Retrieved BM for storage: $it")

                if (!it.isValidForCurrentTime()) {

                    var fetch = TempIDManager.retrieveTemporaryID(AppContext)
                    fetch?.let {
                        CentralLog.i(TAG, "Grab New Temp ID")
                        BluetoothMonitoringService.broadcastMessage = it
                    }

                    if (fetch == null) {
                        CentralLog.e(TAG, "Failed to grab new Temp ID")
                    }

                }
            }
            return BluetoothMonitoringService.broadcastMessage?.tempID ?: "Missing TempID"
        }

        fun asPeripheralDevice(): PeripheralDevice {
            return PeripheralDevice("Android", "SELF")
        }

        fun asCentralDevice(): CentralDevice {
            return CentralDevice("Android", "SELF")
        }
    }
}
