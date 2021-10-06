package com.vigatec.testaplicaciones


import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class WifiAdapterList (var ctx: Context, var resources: Int, var ssidConnected:String, var items: List<ScanResult>): ArrayAdapter<ScanResult>(ctx, resources, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val view: View = layoutInflater.inflate(resources, null)

        val tvSSID: TextView = view.findViewById(R.id.tv_ssid)
        val tvConnected: TextView = view.findViewById(R.id.tv_connected)
        val tvNotSupported: TextView = view.findViewById(R.id.tv_not_supported)

        var scanResult = items[position]
        tvSSID.text = scanResult.SSID

        val securityMode: String = getScanResultSecurity(scanResult)
        if (!securityMode.equals("PSK")) {
            tvNotSupported.visibility = View.VISIBLE
        }

        if(ssidConnected == scanResult.SSID){
            tvConnected.visibility = View.VISIBLE
        }

        return view
    }

    private fun getScanResultSecurity(scanResult: ScanResult): String {
        val cap = scanResult.capabilities
        val securityModes = arrayOf("WEP", "PSK", "EAP")
        for (i in securityModes.indices.reversed()) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i]
            }
        }
        return "OPEN"
    }

}