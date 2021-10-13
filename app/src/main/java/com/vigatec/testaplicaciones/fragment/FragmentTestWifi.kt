package com.vigatec.testaplicaciones.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vigatec.testaplicaciones.R
import com.vigatec.testaplicaciones.WifiAdapterList

import com.vigatec.testaplicaciones.databinding.FragmentTestWifiBinding
import java.lang.Exception
import java.security.AccessController.checkPermission

class FragmentTestWifi : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback
{

    private val TAG = "FragmentTestWifi"
    private var _binding: FragmentTestWifiBinding? = null
    private val binding get() = _binding!!


    //Wifi's variables
    private var isRegisterReceiver: Boolean = false
    private var isScanning: Boolean = false
    private var wifiResults: List<ScanResult> = emptyList()
    private val WFManager get() = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val MY_REQUEST_CODE: Int = 123


    private val wifiReceiver = object :BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            try
            {
                Log.d(TAG, "onRecieve")
                Log.d(TAG, "\n Refresh Result")
                wifiResults = WFManager.scanResults
                updateItems(wifiResults)

            }
            catch(ex: Exception)
            {
            Log.d(TAG,ex.message.toString())
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {

        _binding = FragmentTestWifiBinding.inflate(inflater,container,false)
        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()

    }

    private fun startScanning()
    {
        Log.d(TAG, "Start Scanning")
        binding.loadingPanel.visibility = View.VISIBLE
        isScanning = true
        if (!isRegisterReceiver)
        {
            requireContext().registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            isRegisterReceiver = true
        }

        if (wifiResults.isNotEmpty())
        {
            updateItems(wifiResults)
        }
    }

    private fun stopScanning()
    {
        Log.d(TAG, "Stop Scanning")
        binding.loadingPanel.visibility = View.GONE
        if (isRegisterReceiver)
        {
            requireContext().unregisterReceiver(wifiReceiver)
            isRegisterReceiver = false
        }
    }

    fun updateItems(stations: List<ScanResult>? = null)
    {
        val arrayAdapter: WifiAdapterList
        binding.loadingPanel.visibility = View.GONE
        if (stations != null) {
            val wm: WifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ssidConnected = wm.connectionInfo.ssid.toString().replace("\"", "");

            arrayAdapter = WifiAdapterList(requireContext(), R.layout.row_wifi, ssidConnected, stations)
            binding.lvDataWifi.adapter = arrayAdapter

            binding.lvDataWifi.setOnItemClickListener { parent, view, position, id ->
                var wifi: ScanResult = parent.getItemAtPosition(position) as ScanResult
                val securityMode: String = getScanResultSecurity(wifi)
                                                      }
        }
        else
        {
            Toast.makeText(requireContext(), "Redes no encontradas", Toast.LENGTH_SHORT).show()
        }
        checkPermission()
    }

    private fun clearList() {
        Log.d(TAG,"clearList")
        binding.lvDataWifi.adapter = null

    }

    private fun getScanResultSecurity(scanResult: ScanResult): String {
        Log.d(TAG,"getScanResultSecurity")
        val cap = scanResult.capabilities
        val securityModes = arrayOf("WEP", "PSK", "EAP")
        for (i in securityModes.indices.reversed()) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i]
            }
        }
        return "OPEN"
    }

    private fun checkPermission()
    {
        Log.d(TAG,"checkPermission")
        // With Android Level >= 23, you have to ask the user
        // for permission to Call.
        // With Android Level >= 23, you have to ask the user
        // for permission to Call.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        { // 23
            val permission1: Int = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)

            // Check for permissions
            if (permission1 != PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "Requesting Permissions")

                // Request permissions
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_WIFI_STATE,
                    android.Manifest.permission.ACCESS_NETWORK_STATE
                ), MY_REQUEST_CODE)
                return
            }
            Log.d(TAG, "Permissions Already Granted")
        }
        this.startScanning()
    }

    override fun onDestroy()
    {
        Log.d(TAG,"onDestroy")
        super.onDestroy()
        stopScanning()
    }

    override fun onPause()
    {
        Log.d(TAG,"onPause")
        super.onPause()
        stopScanning()
    }

    override fun onResume()
    {
        Log.d(TAG,"onResume")
        super.onResume()
        if (isScanning)
        {
            startScanning()
        }
    }


}



