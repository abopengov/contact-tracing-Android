package com.example.tracetogether.herald

import com.example.tracetogether.TracerApp
import com.example.tracetogether.idmanager.TempIDManager

interface TempIdProvider {
    val tempId: String?

    fun fetchNewTempId()
}

class ConcreteTempIdProvider: TempIdProvider {
    override val tempId: String? get() = TracerApp.thisDeviceMsg()

    override fun fetchNewTempId() {
        TempIDManager.getTemporaryIDs(TracerApp.AppContext)
    }
}

