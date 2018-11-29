package com.example.joni.mobileproject

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    override fun onStart() {
        super.onStart()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        val tool = rawResult.text
        if (MainActivity.listOfTools.contains(tool)) {
            val toolIntent = Intent(this, ToolsActivity::class.java)
            toolIntent.putExtra(MainActivity.TOOL, tool)
            startActivity(toolIntent)
            onBackPressed()
        }
        else {
            Toast.makeText(this, applicationContext.getText(R.string.unrecognizable_qr_code), Toast.LENGTH_SHORT).show()
            mScannerView!!.resumeCameraPreview(this)
        }
    }
}