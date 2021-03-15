package com.example.tracetogether.idmanager

import android.content.Context
import android.content.Intent
import com.example.tracetogether.Preference
import com.example.tracetogether.RestartActivity
import com.example.tracetogether.Utils
import com.example.tracetogether.api.Request
import com.example.tracetogether.herald.FairEfficacyInstrumentation
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.logging.WFLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


object TempIDManager : CoroutineScope by MainScope() {

    private const val TAG = "TempIDManager"

    fun storeTemporaryIDs(context: Context, packet: String) {
        CentralLog.d(TAG, "[TempID] Storing temporary IDs into internal storage...")
        val file = File(context.filesDir, "tempIDs")
        file.writeText(packet)
    }

    fun retrieveTemporaryID(context: Context): TemporaryID? {
        if (FairEfficacyInstrumentation.enabled) {
            Preference.putExpiryTimeInMillis(
                    context,
                    Long.MAX_VALUE
            )

            return TemporaryID(
                    0,
                    FairEfficacyInstrumentation.fixedTempId,
                    Long.MAX_VALUE / 1000
            )
        }

        val file = File(context.filesDir, "tempIDs")
        if (file.getAbsoluteFile().exists()) {
            val readback = file.readText()
            CentralLog.d(TAG, "[TempID] fetched broadcastmessage from file:  $readback")
            val tempIDArrayList =
                    convertToTemporaryIDs(
                            readback
                    )
            val tempIDQueue =
                    convertToQueue(
                            tempIDArrayList
                    )
            return getValidOrLastTemporaryID(
                    context,
                    tempIDQueue
            )
        }
        return null
    }

    private fun getValidOrLastTemporaryID(
            context: Context,
            tempIDQueue: Queue<TemporaryID>
    ): TemporaryID {

        CentralLog.d(TAG, "[TempID] Retrieving Temporary ID")

        val currentTime = System.currentTimeMillis()
        var pop = 0
        while (tempIDQueue.size > 1) {
            val tempID = tempIDQueue.peek()
            tempID.print()

            if (tempID.isValidForCurrentTime()) {
                CentralLog.d(TAG, "[TempID] Breaking out of the loop")
                break
            }

            tempIDQueue.poll()
            pop++
        }

        val foundTempID = tempIDQueue.peek()
        val foundTempIDStartTime = foundTempID.startTime * 1000
        val foundTempIDExpiryTime = foundTempID.expiryTime * 1000

        CentralLog.d(TAG, "[TempID Total number of items in queue: ${tempIDQueue.size}")
        CentralLog.d(TAG, "[TempID Number of items popped from queue: $pop")
        CentralLog.d(TAG, "[TempID] Current time: $currentTime")
        CentralLog.d(TAG, "[TempID] Start time: $foundTempIDStartTime")
        CentralLog.d(TAG, "[TempID] Expiry time: $foundTempIDExpiryTime")
        CentralLog.d(TAG, "[TempID] Updating expiry time")

        Preference.putExpiryTimeInMillis(
                context,
                foundTempIDExpiryTime
        )
        return foundTempID
    }

    private fun convertToTemporaryIDs(tempIDString: String): Array<TemporaryID> {
        val gson: Gson = GsonBuilder().create()

        val tempIDResult = gson.fromJson(tempIDString, Array<TemporaryID>::class.java)
        CentralLog.d(
                TAG,
                "[TempID] After GSON conversion: ${tempIDResult[0].tempID} ${tempIDResult[0].startTime}"
        )

        return tempIDResult
    }

    private fun convertToQueue(tempIDArray: Array<TemporaryID>): Queue<TemporaryID> {
        CentralLog.d(TAG, "[TempID] Before Sort: ${tempIDArray[0]}")

        //Sort based on start time
        tempIDArray.sortBy {
            return@sortBy it.startTime
        }
        CentralLog.d(TAG, "[TempID] After Sort: ${tempIDArray[0]}")

        //Preserving order of array which was sorted
        var tempIDQueue: Queue<TemporaryID> = LinkedList<TemporaryID>()
        for (tempID in tempIDArray) {
            tempIDQueue.offer(tempID)
        }

        CentralLog.d(TAG, "[TempID] Retrieving from Queue: ${tempIDQueue.peek()}")
        return tempIDQueue
    }

    fun getTemporaryIDs(context: Context) = launch {

        if (FairEfficacyInstrumentation.enabled) {
            updateFetchTime(context, Long.MAX_VALUE)
            return@launch
        }

        try {
            val userId = Preference.getUUID(context)
            if (userId != "") {
                val queryParams = HashMap<String, String>()
                queryParams["userId"] = userId

                val tempIDsResponse = Request.runRequest(
                        "/adapters/tempidsAdapter/tempIds",
                        Request.GET,
                        queryParams = queryParams
                )
                CentralLog.d(TAG, tempIDsResponse.toString())

                if (tempIDsResponse.isSuccess() && tempIDsResponse.status == 200) {

                    CentralLog.i(TAG, "Retrieved Temporary IDs from Server")

                    var responseText = tempIDsResponse.text?.replace("{\"pin\":", "")
                    responseText = responseText?.substring(0, responseText.length - 1)

                    var result: HashMap<String, Any> = Gson().fromJson(
                            responseText,
                            object : TypeToken<HashMap<String, Any>>() {}.getType()
                    )

                    val status = result["status"]
                    val tempIDs = result["tempIDs"]
                    val refreshTime = result.getValue("refreshTime") as Double

                    when {
                        status != "SUCCESS" -> {
                            CentralLog.d(
                                    TAG,
                                    "[TempID] Error getting Temporary IDs - status not SUCCESS"
                            )
                        }
                        tempIDs == null -> {
                            CentralLog.d(
                                    TAG,
                                    "[TempID] Error getting Temporary IDs - no temp IDs returned"
                            )
                            WFLog.logError("[TempID] Error getting Temporary IDs - no temp IDs returned")
                        }
                        refreshTime == null -> {
                            CentralLog.d(
                                    TAG,
                                    "[TempID] Error getting Temporary IDs - no refreshTime returned"
                            )
                            WFLog.logError("[TempID] Error getting Temporary IDs - no refreshTime returned")
                        }
                        else -> {
                            // Store the temp ids
                            val jsonByteArray =
                                    GsonBuilder().create().toJson(tempIDs).toByteArray(Charsets.UTF_8)
                            storeTemporaryIDs(
                                    context,
                                    jsonByteArray.toString(Charsets.UTF_8)
                            )

                            // Store the refresh time and last fetched time
                            updateFetchTime(context, refreshTime.toLong() * 1000)
                        }
                    }
                } else {
                    throw java.lang.Exception("Failed to get temp ids")
                }
            } else {
                CentralLog.d(TAG, "User is not logged in")
                Utils.restartApp(context, 1,"[TempID] Error getting Temporary IDs, no userId")
            }
        } catch (e: Exception) {
            CentralLog.d(TAG, "[TempID] Error getting Temporary IDs")
        }
    }

    private fun updateFetchTime(context: Context, refreshTime: Long) {
        Preference.putNextFetchTimeInMillis(
                context,
                refreshTime
        )
        Preference.putLastFetchTimeInMillis(
                context,
                System.currentTimeMillis()
        )
    }

    fun needToUpdate(context: Context): Boolean {
        val nextFetchTime =
                Preference.getNextFetchTimeInMillis(context)
        val currentTime = System.currentTimeMillis()

        val update = currentTime >= nextFetchTime
        CentralLog.i(
                TAG,
                "Need to update and fetch TemporaryIDs? $nextFetchTime vs $currentTime: $update"
        )
        return update
    }

    fun needToRollNewTempID(context: Context): Boolean {
        val expiryTime =
                Preference.getExpiryTimeInMillis(context)
        val currentTime = System.currentTimeMillis()
        val update = currentTime >= expiryTime
        CentralLog.d(TAG, "[TempID] Need to get new TempID? $expiryTime vs $currentTime: $update")
        return update
    }

    fun bmValid(context: Context): Boolean {
        val expiryTime = Preference.getExpiryTimeInMillis(context)
        val currentTime = System.currentTimeMillis()
        return currentTime < expiryTime
    }
}
