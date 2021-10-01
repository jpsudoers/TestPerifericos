package com.vigatec.testaplicaciones

//import com.ingenico.acc.UsdkManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import com.usdk.apiservice.aidl.constants.RFDeviceName

import android.os.Build
import com.usdk.apiservice.aidl.pinpad.DeviceName
import com.vigatec.testaplicaciones.constant.DemoConfig


class MainActivity : AppCompatActivity() {

    private var TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        try
        {

           // CoroutineScope(Dispatchers.IO).launch {
                //Initialize USDK manager
           //     UsdkManager.initialize(application)
           //     UsdkManager.connect()
                initDefaultConfig()
                DeviceHelper.me().init(this)
                DeviceHelper.me().bindService()

        }

         catch (ex: Exception) {
            Log.d("MainActivity", ex.message.toString())
        }


    }

    override fun onDestroy()
    {
        super.onDestroy()
        DeviceHelper.me().unbindService()
    }

    private fun initDefaultConfig() {
        if (Build.MODEL.startsWith("AECR")) {
            DemoConfig.PINPAD_DEVICE_NAME = DeviceName.COM_EPP
            DemoConfig.RF_DEVICE_NAME = RFDeviceName.EXTERNAL
        } else {
            DemoConfig.PINPAD_DEVICE_NAME = DeviceName.IPP
            DemoConfig.RF_DEVICE_NAME = RFDeviceName.INNER
        }
    }


}