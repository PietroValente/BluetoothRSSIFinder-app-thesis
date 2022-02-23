package com.example.bluetoothfinder

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import android.widget.Button


class MainActivity : AppCompatActivity() {
    var filePath: File? = null
    var hssfSheet: HSSFSheet? = null
    var r:Int = 0
    var temp :HSSFRow? = null
    var misurazioni :HSSFCell? = null

    var listView: ListView? = null
    var statusTextView: TextView? = null
    var countTextView: TextView? = null
    var searchButton: Button? = null
    var stopButton: Button? = null
    companion object{
        var deviceName: String = " "
        var deviceNameTextView: TextView? = null
    }

    var count:Int = -10
    var flag:Boolean? = false
    var bluetoothList: ArrayList<String>? = ArrayList()
    var arrayAdapter: ArrayAdapter<String>? = null
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            Log.i("Action", action!!)
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                if(flag == true) {
                    search()
                }
            } else if (ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val name: String? = device?.name
                val rssi: String = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toString()

                if(name == "$deviceName"){
                    count++
                    if(count == 1){
                        bluetoothList?.add("INIZIO MISURAZIONI")
                    }
                    bluetoothAdapter.cancelDiscovery()
                    bluetoothList?.add("$rssi")
                    countTextView?.text = "Numero misurazioni: $count"
                    if(count>0){
                            temp = hssfSheet?.createRow(r)
                            if(r == 0) {
                                temp?.createCell(2)?.setCellValue("Numero misurazioni:")
                                misurazioni =  temp?.createCell(4)
                            }
                        temp?.createCell(0)?.setCellValue(rssi.toDouble()) //imposta valore nella cella?
                        r++
                    }
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
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)

        val myIntent = Intent(this, Initial::class.java)
        startActivity(myIntent)

        val nomeFile = "Misurazioni" //nome del file
        filePath = File(Environment.getExternalStorageDirectory().toString() + "/" + nomeFile + ".xls") //PATH NAME LASCIA COSì
        val hssfWorkbook =  HSSFWorkbook()
        hssfSheet = hssfWorkbook.createSheet("Misurazione") //NOME DELLA PAGINA

        listView = findViewById(R.id.listView)
        statusTextView = findViewById(R.id.statusTextView)
        countTextView = findViewById(R.id.countTextView)
        deviceNameTextView = findViewById(R.id.deviceNameTextView)
        searchButton = findViewById(R.id.searchButton)
        stopButton = findViewById(R.id.stopButton)

        stopButton?.isEnabled = false
        stopButton?.isClickable = false

        countTextView?.text = "Numero misurazioni: $count"

        stopButton?.setOnClickListener{
            flag = false
            bluetoothAdapter.cancelDiscovery()
            Log.i("Bluetooth", "Search finished!")
            statusTextView?.text = getString(R.string.finished)
            searchButton?.isEnabled = true
            searchButton?.isClickable = true
            stopButton?.isEnabled = false
            stopButton?.isClickable = false
            searchButton?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.purple_500))
            misurazioni?.setCellValue(count.toDouble())
            try {
                if (!filePath!!.exists()) {
                    filePath!!.createNewFile()
                }
                val fileOutputStream = FileOutputStream(filePath)
                hssfWorkbook.write(fileOutputStream)
                if (fileOutputStream != null) {
                    fileOutputStream.flush()
                    fileOutputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        searchButton?.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            } else {
                flag = true
                search()
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(broadcastReceiver, intentFilter)

        arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, bluetoothList!!)
        listView?.adapter = arrayAdapter
    }

    private fun search(){
        Log.i("Bluetooth", "Searching...")
        arrayAdapter?.notifyDataSetChanged()
        statusTextView?.text = getString(R.string.searching)
        searchButton?.isEnabled = false
        searchButton?.isClickable = false
        stopButton?.isEnabled = true
        stopButton?.isClickable = true
        searchButton?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.greyish))

        bluetoothAdapter.startDiscovery()
    }
}