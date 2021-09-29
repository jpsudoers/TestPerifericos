package com.vigatec.testaplicaciones.fragment


import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ingenico.acc.UsdkManager
import com.ingenico.acc.UsdkManager.deviceService
import com.ingenico.acc.UsdkManager.getBeeper
import com.ingenico.acc.UsdkManager.getDevice
import com.ingenico.acc.UsdkManager.getMagReader
import com.ingenico.ingp.types.iso7813.Track1
import com.usdk.apiservice.aidl.UDeviceService
import com.usdk.apiservice.aidl.magreader.OnSwipeListener
import com.usdk.apiservice.aidl.magreader.TrackID
import com.usdk.apiservice.aidl.magreader.TrackType
import com.usdk.apiservice.aidl.magreader.UMagReader
import com.usdk.apiservice.aidl.magreader.IoCtrlCmd
import com.usdk.apiservice.aidl.magreader.MagData
import com.usdk.apiservice.aidl.magreader.MagError
import com.usdk.apiservice.aidl.magreader.industry.UIndustryMagReader

import com.vigatec.testaplicaciones.R
import com.vigatec.testaplicaciones.databinding.FragmentTestMagStripeBinding


class FragmentTestMagStripe : Fragment() {

    private val TAG = "FragmentTestMagStripe"
    private var _binding: FragmentTestMagStripeBinding? = null
    private val binding get() =_binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTestMagStripeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)

            {
            super.onViewCreated(view, savedInstanceState)

            //Sound
           getBeeper()?.startBeep(300)
           Log.d(TAG,"Llamado a bip")

            //GetMagStripe
          //  UsdkManager.getMagReader()

               private var UMagReader: 








            }



}







