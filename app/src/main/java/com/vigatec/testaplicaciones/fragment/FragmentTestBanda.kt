package com.vigatec.testaplicaciones.fragment




import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.ingenico.acc.UsdkManager
//import com.ingenico.acc.UsdkManager.deviceService
//import com.ingenico.acc.UsdkManager.getBeeper
//import com.ingenico.acc.UsdkManager.getDevice
//import com.ingenico.acc.UsdkManager.getMagReader
//import com.ingenico.ingp.types.iso7813.Track1
//import com.ingenico.lar.apos.DeviceHelper
import com.usdk.apiservice.aidl.UDeviceService
import com.usdk.apiservice.aidl.data.BytesValue
import com.usdk.apiservice.aidl.magreader.OnSwipeListener
import com.usdk.apiservice.aidl.magreader.TrackID
import com.usdk.apiservice.aidl.magreader.TrackType
import com.usdk.apiservice.aidl.magreader.UMagReader
import com.usdk.apiservice.aidl.magreader.IoCtrlCmd
import com.usdk.apiservice.aidl.magreader.MagData
import com.usdk.apiservice.aidl.magreader.MagError
import com.usdk.apiservice.aidl.magreader.industry.UIndustryMagReader
import com.vigatec.testaplicaciones.DeviceHelper

import com.vigatec.testaplicaciones.R
import java.lang.Exception
import java.lang.IllegalStateException

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.usdk.apiservice.aidl.beeper.UBeeper
import com.vigatec.testaplicaciones.databinding.FragmentTestBandaBinding


class FragmentTestBanda : Fragment() {

    private val TAG = "FragmentTestBanda"
    private var _binding: FragmentTestBandaBinding? = null
    private val binding get() = _binding!!

    //DeviceManager's Variable

    private var magReader: UMagReader? = null
    private val trackType = TrackType.BANK_CARD
    private val trk1Enabled = true
    private val trk2Enabled = true
    private val trk3Enabled = true
    private val retainCtlChar = false
    private val trackStateChecked = true
    private val wholeTrkId = 0
    private var beeper: UBeeper? = null

    //Inicialize deviceHelper & Beeper

    protected fun initDeviceInstanceBeeper() {beeper = DeviceHelper.me().beeper }
    protected open fun initDeviceInstance() { magReader = DeviceHelper.me().getMagReader() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,): View?
    {

        _binding = FragmentTestBandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)

    {
        super.onViewCreated(view, savedInstanceState)

        register()
        initDeviceInstanceBeeper()
        initDeviceInstance()
        searchCard(view)

        //Sound
        //getBeeper()?.startBeep(300)
        Log.d(TAG,"Sonido Beep MagStripe")
        beepWhenNormal(view)

    }

    private fun register()
    {
        try {
            DeviceHelper.me().register(true)

        }
        catch (e: IllegalStateException)
        {
            Log.d(TAG, e.message.toString())
        }
    }



    fun beepWhenNormal(v: View?)
    {
        try
        {
            beeper!!.startBeep(500)
        }
        catch (e: Exception)
        {
            println("Era...")
        }
    }



    fun searchCard(v: View?)
    {

        Log.d(TAG,"Buscando Tarjeta")
        val timeout = 30
        try
        {
            if (!setTRKDataType()) {
                // return;
            }
            enableTrack(TrackID.TRK1, trk1Enabled)
            enableTrack(TrackID.TRK2, trk2Enabled)
            enableTrack(TrackID.TRK3, trk3Enabled)
            magReader!!.setTrackType(trackType)
            magReader!!.retainControlChar(retainCtlChar)
            magReader!!.setTrackStatesChecked(trackStateChecked)
            magReader!!.searchCard(timeout, object : OnSwipeListener.Stub() {
                //@Throws(RemoteException::class)
                override fun onSuccess(track: Bundle) {

                    val Pan     = track.getString(MagData.PAN)
                    Log.d(TAG,"PAN: $Pan")
                    val Track1  = track.getString(MagData.TRACK1)
                    Log.d(TAG,"Track1: $Track1")
                    val Track2  = track.getString(MagData.TRACK2)
                    Log.d(TAG,"Track2: $Track2")
                    val Track3  = track.getString(MagData.TRACK3)
                    Log.d(TAG,"Track3: $Track3")

                    if (Track1 == null)
                    {
                        Toast.makeText(requireContext(), "Imposible leer tarjeta", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        findNavController().navigate(R.id.action_fragmentTestBanda_to_fragmentTestEmv)
                    }

                }

                override fun onError(p0: Int)
                {
                    Log.d(TAG,"OnError")

                }

                @Throws(RemoteException::class)
                override fun onTimeout()
                {
                    //outputRedText("=> onTimeout")
                }
            })
        }
        catch (e: Exception)
        {
            // handleException(e)
        }
    }

    @Throws(RemoteException::class)
    private fun enableTrack(trkId: Int, enabled: Boolean)
    {
        if (enabled)
        {
            magReader!!.enableTrack(trkId)
        }
        else
        {
            magReader!!.disableTrack(trkId)
        }
    }

    @Throws(RemoteException::class)
    private fun setTRKDataType(): Boolean
    {
        val result = BytesValue()
        val ret = magReader!!.magIOControl(
            IoCtrlCmd.SET_TRKDATA_TYPE, byteArrayOf(
                wholeTrkId.toByte()
            ), result
        )
        if (ret != MagError
                .SUCCESS)
        {
            //  outputText("=> magIOControl[SET_TRKDATA_TYPE] fail: " + getErrorDetail(ret))

            return false
        }
        else
        {
            Log.d(TAG, "Función setRTRKDataType OK" )
        }
        return true
    }

//End Class */
}



















