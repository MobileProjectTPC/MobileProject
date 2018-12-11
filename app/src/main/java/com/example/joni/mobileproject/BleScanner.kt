package com.example.joni.mobileproject

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.os.Handler
import android.util.Log
import java.util.*

class BleScan(mHand: Handler, private val bluetoothAdapter: BluetoothAdapter): Runnable{

    private var mScanCallback: BtleScanCallback? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mScanResults: HashMap<String, ScanResult>? = null
    private val myHandler = mHand

    override fun run() {
        startsScan()
    }

    // Add settings and filter to BLEScanner
    // Had to improvise little, because I could not access all beacons and config them
    // So now I'm using device name as filter rather than id
    private fun startsScan() {
        Log.d("DBG", "Scan start")
        mScanResults = HashMap()
        mScanCallback = BtleScanCallback()
        mBluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        val filters = ArrayList<ScanFilter>()

        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build()

        var filter = ScanFilter.Builder()
                .setDeviceName("iBKS105")
                .build()

        filters.add(filter)

        filter = ScanFilter.Builder()
                .setDeviceName("iBKS105U")
                .build()

        filters.add(filter)

        mBluetoothLeScanner!!.startScan(filters, settings, mScanCallback)
    }

    // If scanner find a beacon, add it to the results
    private inner class BtleScanCallback: ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results){
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d("MainActivity", "BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address
            mScanResults!![deviceAddress] = result

            getLocation()
        }
    }

    // Location detection is not accurate, because could not config the beacons the way I wanted
    // so now they transmit different strenghts
    // so even you would be closer to another beacon it could be that other one is still giving stronger signal
    private fun getLocation(){
        val locationMap = HashMap<String, Int>()

        mScanResults!!.forEach { s, scanResult ->
            locationMap[s] = scanResult.rssi
        }

        val max = locationMap.maxBy { it.value }

        if (max!!.key == "DD:7D:F4:46:F2:B3" && max.value >= -75){
            val msg = myHandler.obtainMessage()
            msg.what = 0
            msg.obj = "3Dprinting"
            myHandler.sendMessage(msg)
        }
        else if (max.key == "CA:8B:EE:10:E2:6F" && max.value >= -75){
            val msg = myHandler.obtainMessage()
            msg.what = 0
            msg.obj = "Workspace2"
            myHandler.sendMessage(msg)
        }
        else {
            val msg = myHandler.obtainMessage()
            msg.what = 0
            msg.obj = "No workspace detected"
            myHandler.sendMessage(msg)
        }
    }

    private fun stopScan() {
        mBluetoothLeScanner!!.stopScan(mScanCallback)
    }
}