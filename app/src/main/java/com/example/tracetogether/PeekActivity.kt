package com.example.tracetogether

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.database_peek.*
import com.example.tracetogether.streetpass.persistence.StreetPassRecordStorage
import com.example.tracetogether.streetpass.view.RecordViewModel

/*
    Database peek activity, only used in debug build mode
 */
class PeekActivity : AppCompatActivity() {

    private lateinit var viewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newPeek()
    }

    private fun newPeek() {
        setContentView(R.layout.database_peek)
        val adapter = RecordListAdapter(this)
        recyclerview.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            recyclerview.context,
            layoutManager.orientation
        )
        recyclerview.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        viewModel.allRecords.observe(this, Observer { records ->
            adapter.setSourceData(records)
        })

        expand?.setOnClickListener {
            viewModel.allRecords.value?.let {
                adapter.setMode(RecordListAdapter.MODE.ALL)
            }
        }

        collapse?.setOnClickListener {
            viewModel.allRecords.value?.let {
                adapter.setMode(RecordListAdapter.MODE.COLLAPSE)
            }
        }


        start?.setOnClickListener {
            startService()
        }

        stop?.setOnClickListener {
            stopService()
        }

        delete?.setOnClickListener { view ->
            view.isEnabled = false

            val builder = AlertDialog.Builder(this)
            builder
                .setTitle(R.string.peekactivity_title)
                .setCancelable(false)
                .setMessage(R.string.delete_warn)
                .setPositiveButton(R.string.delete) { dialog, which ->
                    Observable.create<Boolean> {
                        StreetPassRecordStorage(this).nukeDb()
                        it.onNext(true)
                    }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe { result ->
                            Toast.makeText(this, "Database nuked: $result", Toast.LENGTH_SHORT)
                                .show()
                            view.isEnabled = true
                            dialog.cancel()
                        }
                }

                .setNegativeButton(R.string.nodelete) { dialog, which ->
                    view.isEnabled = true
                    dialog.cancel()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()

        }

        plot?.setOnClickListener { view ->
            val intent = Intent(this, PlotActivity::class.java)
            intent.putExtra("time_period", nextTimePeriod())
            startActivity(intent)
        }

        val uid = Preference.getUUID(applicationContext)
        val serviceUUID = BuildConfig.BLE_SSID
        info?.text =
            "UID: ${uid.substring(uid.length - 4)}   SSID: ${serviceUUID.substring(serviceUUID.length - 4)}"

        if (!BuildConfig.DEBUG) {
            start?.visibility = View.GONE
            stop?.visibility = View.GONE
            delete?.visibility = View.GONE
        }
    }

    private var timePeriod: Int = 0

    private fun nextTimePeriod(): Int {
        timePeriod = when (timePeriod) {
            1 -> 3
            3 -> 6
            6 -> 12
            12 -> 24
            else -> 1
        }

        return timePeriod
    }


    private fun startService() {
        Utils.startBluetoothMonitoringService(this)
    }

    private fun stopService() {
        Utils.stopBluetoothMonitoringService(this)
    }

}
