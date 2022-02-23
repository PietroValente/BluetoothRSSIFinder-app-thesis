package com.example.bluetoothfinder

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Initial : AppCompatActivity() {
    var listView: ListView? = null
    var statusTextView: TextView? = null
    var DeviceMessageTextView: TextView? = null
    var searchButton: Button? = null
    var bluetoothList: ArrayList<String>? = null
    var bluetoothAddressList: ArrayList<String>? = null
    var arrayAdapter: ArrayAdapter<String>? = null
    var deviceList: MutableList<String> = ArrayList()
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            Log.i("Action", action!!)

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                Log.i("Bluetooth", "Search finished!")
                statusTextView?.text = getString(R.string.finished)
                searchButton?.isEnabled = true
                searchButton?.isClickable = true
                searchButton?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.purple_500))
                if (bluetoothList?.isEmpty() == true) {
                    bluetoothList?.add("No bluetooth devices found! Search again?")
                }
            } else if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val name: String? = device?.name
                val address: String? = device?.address
                val rssi: String = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toString()
                Log.i("Device Found", "Name: $name Address: $address RSSI: $rssi")
                val deviceId: String? = name ?: address
                if (bluetoothAddressList?.contains(address) == false) {
                    bluetoothList?.add("$deviceId")
                    bluetoothAddressList?.add("$address")
                    deviceList.add("$name")
                }
            }
            arrayAdapter?.notifyDataSetChanged()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            search()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial)

        listView = findViewById(R.id.listView)
        statusTextView = findViewById(R.id.statusTextView)
        DeviceMessageTextView = findViewById(R.id.DeviceMessageTextView)
        searchButton = findViewById(R.id.searchButton)

        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(broadcastReceiver, intentFilter)

        bluetoothAddressList = ArrayList()
        DeviceMessageTextView?.text = "Selezionare il dispositivo"

        bluetoothList = ArrayList()
        arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, bluetoothList!!)
        listView?.adapter = arrayAdapter
        listView?.setOnItemClickListener{ parent, view, position, id ->
            MainActivity.deviceName = deviceList[position]
            MainActivity.deviceNameTextView?.text = deviceList[position]
            bluetoothAdapter.cancelDiscovery()
            finish()
        }
    }

    fun onSearchClicked(view: View) {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            search()
        }
    }

    private fun search() {
        Log.i("Bluetooth", "Searching...")
        bluetoothList?.clear()
        bluetoothAddressList?.clear()
        arrayAdapter?.notifyDataSetChanged()
        statusTextView?.text = getString(R.string.searching)
        searchButton?.isEnabled = false
        searchButton?.isClickable = false
        searchButton?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.greyish))

        bluetoothAdapter.startDiscovery()
    }
}
