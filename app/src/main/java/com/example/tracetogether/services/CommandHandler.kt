package com.example.tracetogether.services

import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

class CommandHandler(val service: WeakReference<BluetoothMonitoringService>) : Handler() {
    override fun handleMessage(msg: Message?) {
        msg?.let {
            //            val cmd = msg.arg1
            val cmd = msg.what
            service.get()?.runService(BluetoothMonitoringService.Command.findByValue(cmd))
        }
    }

    fun sendCommandMsg(cmd: BluetoothMonitoringService.Command, delay: Long) {
//        val msg = obtainMessage(cmd.index)
        val msg = Message.obtain(this, cmd.index)
//        msg.arg1 = cmd.index
        sendMessageDelayed(msg, delay)
    }

    fun sendCommandMsg(cmd: BluetoothMonitoringService.Command) {
        val msg = obtainMessage(cmd.index)
        msg.arg1 = cmd.index
        sendMessage(msg)
    }

    fun startBluetoothMonitoringService() {
        sendCommandMsg(BluetoothMonitoringService.Command.ACTION_START)
    }
}
