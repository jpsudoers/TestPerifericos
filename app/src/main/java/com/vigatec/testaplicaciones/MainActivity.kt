package com.vigatec.testaplicaciones

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.ingenico.acc.UsdkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        try {

            CoroutineScope(Dispatchers.IO).launch {
                //Initialize USDK manager
                UsdkManager.initialize(application)
                UsdkManager.connect()
            }

        } catch (ex: Exception) {
            Log.d("MainActivity", ex.message.toString())
        }


    }


}