package com.example.tracetogether.herald

import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.streetpass.persistence.StreetPassRecord
import com.example.tracetogether.streetpass.persistence.StreetPassRecordStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface BlueTraceDataPersistence {
    fun saveEncounter(record: StreetPassRecord)
}

class StreetPassDataPersistence(private val streetPassRecordStorage: StreetPassRecordStorage) :
    BlueTraceDataPersistence {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun saveEncounter(record: StreetPassRecord) {
        scope.launch {
            CentralLog.d(
                TAG,
                "Coroutine - Saving StreetPassRecord: ${Utils.getDate(record.timestamp)}"
            )
            streetPassRecordStorage.saveRecord(record)
        }
    }

    companion object {
        private const val TAG = "BlueTraceDataPersistence"
    }
}

