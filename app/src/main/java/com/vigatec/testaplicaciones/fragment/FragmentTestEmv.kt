package com.vigatec.testaplicaciones.fragment


import android.content.Context
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.usdk.apiservice.aidl.beeper.UBeeper
import com.usdk.apiservice.aidl.data.StringValue
import com.usdk.apiservice.aidl.emv.*
import com.usdk.apiservice.aidl.emv.KernelID
import com.usdk.apiservice.aidl.pinpad.*
import com.vigatec.testaplicaciones.DeviceHelper
import com.vigatec.testaplicaciones.constant.DemoConfig
import com.vigatec.testaplicaciones.emv.SearchListenerAdapter
import com.vigatec.testaplicaciones.entity.CardOption
import com.vigatec.testaplicaciones.entity.EMVOption
import com.vigatec.testaplicaciones.util.BytesUtil
import com.vigatec.testaplicaciones.util.TLV
import java.lang.Exception
import java.lang.IllegalStateException
import com.usdk.apiservice.aidl.emv.ActionFlag
import com.vigatec.testaplicaciones.R
import com.vigatec.testaplicaciones.constant.DemoConfig.TAG
import com.vigatec.testaplicaciones.databinding.FragmentTestEmvBinding
import com.vigatec.testaplicaciones.util.EMVInfoUtil
import kotlinx.android.synthetic.main.fragment_test_emv.*
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.util.ArrayList


class FragmentTestEmv : Fragment()
{
    private val TAG = "FragmentTestEmv"
    private var _binding: FragmentTestEmvBinding? = null
    private val binding get() = _binding!!


    //DeviceManager's Variable
    private var beeper: UBeeper? = null
    protected var emv: UEMV? = null
    protected var pinpad: UPinpad? = null
    protected var emvOption = EMVOption.create()
    protected var cardOption = CardOption.create()
    protected var uiHandler = Handler(Looper.getMainLooper())
    private var lastCardRecord: CardRecord? = null

    //Inicialize deviceHelper & Beeper

    protected fun initDeviceInstanceBeeper() {beeper = DeviceHelper.me().beeper }
    protected fun initDeviceInstance() { emv = DeviceHelper.me().emv }

    //Excepcion click on nav
    private val clickTag = "__click__"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        Log.d(TAG,"onCreateView")

        _binding = FragmentTestEmvBinding.inflate(inflater,container,false)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        // Call's Funtions
        register()
        initDeviceInstanceBeeper()
        initDeviceInstance()

        //Sound
        Log.d(TAG, "Sonido Beep EMV")
        beepWhenNormal(view)
        startTrade(view)
        startEMV(emvOption)
        Toast.makeText(requireContext(), "Apertura Periferico EMV OK", Toast.LENGTH_SHORT).show()



        binding.btnEmv.setOnClickListener{
            val action = FragmentTestBandaDirections.actionFragmentTestBandaToFragmentTestSinContacto()
            findNavController().navigate(action)


        }



    }




    private fun register()
    {
        Log.d(TAG,"Register")
        try
        {
            DeviceHelper.me().register(true)

        }
        catch (e: IllegalStateException)
        {
            Log.d(TAG, e.message.toString())
        }
    }



    fun beepWhenNormal(v: View?)
    {
        Log.d(TAG,"beepWhenNormal")
        try
        {
            beeper!!.startBeep(500)
        }
        catch (e: Exception)
        {
            println("Era...")
        }
    }


    open fun startTrade(v: View?)
    {
        Log.d(TAG, "StartTrade")
        Log.d(TAG,">>>>>>>>>> start trade <<<<<<<<<")
        Log.d(TAG,"******  search card ******")
        try
        {
            emv!!.searchCard(cardOption.toBundle(), DemoConfig.TIMEOUT, object :SearchCardListener.Stub()
            {
                override fun onCardSwiped(p0: Bundle?) {
                    Log.d(TAG,"=> onCardSwiped")

                    TODO("Not yet implemented")
                }

                override fun onCardInsert()
                {
                    Log.d(TAG,"=> onCardInsert")
                    startEMV(emvOption.flagPSE(0x00.toByte()))
                    beepWhenNormal(view)

                    findNavController().navigate(R.id.action_fragmentTestEmv_to_fragmentTestWifi)
                    Toast.makeText(requireContext(), "Lectura Chip OK", Toast.LENGTH_SHORT).show()


                }

                override fun onCardPass(p0: Int)
                {
                    Log.d(TAG,"=> onCardPass")
                    startEMV(emvOption.flagPSE(0x01.toByte()))
                    findNavController().navigate(R.id.action_fragmentTestEmv_to_fragmentTestWifi)


                }

                override fun onTimeout()
                {
                    Log.d(TAG,"=> onTimeout")
                    stopEMV()

                }



                override fun onError(code: Int, message: String)
                {
                    Log.d(TAG,String.format("=> onError | %s[0x%02X]", message, code))
                    stopEMV()
                }
            })
        }
        catch (e: Exception)
        {

        }
    }

    open fun stopTrade(v: View?)
    {
        Log.d(TAG, "StopTrade")
        Log.d(TAG,"\n>>>>>>>>>> stop trade <<<<<<<<<")
        stopEMV()
        stopSearch()
        halt()
    }

    open fun startEMV(option: EMVOption)
    {
        try
        {
            Log.d(TAG,"******  start EMV ******")
            getKernelVersion()
            getCheckSum()

            val ret = emv!!.startEMV(option.toBundle(), emvEventHandler)

            Log.d(TAG, "=> Start EMV + $ret")
            openPinpad()
          //  findNavController().navigate(R.id.action_fragmentTestEmv_to_fragmentTestWifi)

        }
        catch (e: Exception)
        {
            Log.d(TAG,"******  No se pudo iniciar funcion startEMV ******")
            Log.d(TAG, e.message.toString())

        }
    }

    protected open fun getKernelVersion() {
        Log.d(TAG, "getKernelVersion")
        try {
            val version = StringValue()
            val ret = emv!!.getKernelVersion(version)
            if (ret == EMVError.SUCCESS) {
                Log.d(TAG,"EMV kernel version: " + version.data)
            } else {
                Log.d(TAG,"EMV kernel version: fail, ret = $ret")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    protected open fun getCheckSum() {
        Log.d(TAG, "getChecksum")
        try {
            val flag = 0xA2
            val checkSum = StringValue()
            val ret = emv!!.getCheckSum(flag, checkSum)
            if (ret == EMVError.SUCCESS) {
                Log.d(TAG,"EMV kernel[" + flag + "] checkSum: " + checkSum.data)
            } else {
                Log.d(TAG,"EMV kernel[$flag] checkSum: fail, ret = $ret")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    protected open fun stopEMV()
    {
        try
        {
            Log.d(TAG,"******  stop EMV ******")

        }
        catch (e: Exception)
        {
            Log.d(TAG,"No se pudo para EMV")
        }
    }

    protected open fun respondCard()
    {
        Log.d(TAG, "respondCard")
        try
        {
            emv!!.respondCard()
        }
        catch (e: RemoteException)
        {
            Log.d(TAG,"Sin respuesta de tarjeta")
        }
    }

    protected open fun openPinpad()
    {
        Log.d(TAG, "openPinpad")
        try
        {
            pinpad?.open()
        }
        catch (e: RemoteException)
        {
            e.printStackTrace()
        }
    }

    protected open fun stopSearch()
    {
        try
        {
            Log.d(TAG,"******  stop Search ******")
            emv!!.stopSearch()
        }
        catch (e: Exception)
        {
            Log.d(TAG, e.message.toString())
        }
    }


    protected open fun halt()
    {
        Log.d(TAG, "halt")
        try
        {
            Log.d(TAG,"******  close RF device ******")
            emv!!.halt()
        }
        catch (e: Exception)
        {
            Log.d(TAG, e.message.toString())
        }
    }


    protected var emvEventHandler: EMVEventHandler = object : EMVEventHandler.Stub() {
        @Throws(RemoteException::class)
        override fun onInitEMV() {
            Log.d(TAG, "onInitEMV")
            doInitEMV()
        }

        @Throws(RemoteException::class)
        override fun onWaitCard(flag: Int) {
            Log.d(TAG, "onWaitCard")
            doWaitCard(flag)
        }

        @Throws(RemoteException::class)
        override fun onCardChecked(cardType: Int) {
            Log.d(TAG, "onCardChecked")
            // Only happen when use startProcess()
            doCardChecked(cardType)
        }

        @Throws(RemoteException::class)
        override fun onAppSelect(reSelect: Boolean, list: List<CandidateAID>)
        {
            Log.d(TAG, "onAppSelect")
            doAppSelect(reSelect, list)
        }

        @Throws(RemoteException::class)
        override fun onFinalSelect(finalData: FinalData)
        {
            Log.d(TAG, "onFinalSelect")
            doFinalSelect(finalData)
        }

        @Throws(RemoteException::class)
        override fun onReadRecord(cardRecord: CardRecord)
        {
            Log.d(TAG, "onReadRecord")
            lastCardRecord = cardRecord
            doReadRecord(cardRecord)
        }

        @Throws(RemoteException::class)
        override fun onCardHolderVerify(cvmMethod: CVMMethod)
        {
            Log.d(TAG, "onCardHolderVerify")
            doCardHolderVerify(cvmMethod)
        }

        @Throws(RemoteException::class)
        override fun onOnlineProcess(transData: TransData)
        {
            Log.d(TAG,"onOnlineProcess")
            doOnlineProcess(transData)
        }

        @Throws(RemoteException::class)
        override fun onEndProcess(result: Int, transData: TransData)
        {
            Log.d(TAG,"onEndProcess")
            doEndProcess(result, transData)
        }

        @Throws(RemoteException::class)
        override fun onVerifyOfflinePin(flag: Int, random: ByteArray, caPublicKey: CAPublicKey, offlinePinVerifyResult: OfflinePinVerifyResult)
        {
            Log.d(TAG,"onVerifyOfflinePin")
            doVerifyOfflinePin(flag, random, caPublicKey, offlinePinVerifyResult)
        }

        @Throws(RemoteException::class)
        override fun onObtainData(ins: Int, data: ByteArray)
        {
            Log.d(TAG,"=> onObtainData: instruction is 0x" + Integer.toHexString(ins) + ", data is " + BytesUtil.bytes2HexString(
                data))
        }

        @Throws(RemoteException::class)
        override fun onSendOut(ins: Int, data: ByteArray)
        {
            Log.d(TAG,"onSendOut")
            doSendOut(ins, data)
        }
    }

    @Throws(RemoteException::class)
    open fun doInitEMV()
    {
        Log.d(TAG,"=> onInitEMV ")
        manageAID()

        //  init transaction parameters，please refer to transaction parameters
        //  chapter about onInitEMV event in《UEMV develop guide》
        //  For example, if VISA is supported in the current transaction,
        //  the label: DEF_TAG_PSE_FLAG(M) must be set, as follows:
        emv!!.setTLV(KernelID.VISA, EMVTag.DEF_TAG_PSE_FLAG, "03")
        // For example, if AMEX is supported in the current transaction，
        // labels DEF_TAG_PSE_FLAG(M) and DEF_TAG_PPSE_6A82_TURNTO_AIDLIST(M) must be set, as follows：
        // emv.setTLV(KernelID.AMEX, EMVTag.DEF_TAG_PSE_FLAG, "03");
        // emv.setTLV(KernelID.AMEX, EMVTag.DEF_TAG_PPSE_6A82_TURNTO_AIDLIST, "01");
    }

    @Throws(RemoteException::class)
    protected open fun manageAID()
    {
        Log.d(TAG,"****** manage AID ******")
        val aids = arrayOf(
            "A000000333010106",
            "A000000333010103",
            "A000000333010102",
            "A000000333010101",
            "A0000000651010",
            "A0000000043060",
            "A0000000041010",
            "A000000003101002"
        )
        for (aid in aids)
        {
            val ret = emv!!.manageAID(ActionFlag.ADD, aid, true)
            Log.d(TAG,"=> add AID : $aid")
        }
    }

    @Throws(RemoteException::class)
    open fun doWaitCard(flag: Int)
    {
        Log.d(TAG,"doWaitCard")
        when (flag)
        {
            WaitCardFlag.ISS_SCRIPT_UPDATE, WaitCardFlag.SHOW_CARD_AGAIN -> searchRFCard(Runnable { respondCard() })
            WaitCardFlag.EXECUTE_CDCVM ->
            {
                emv!!.halt()
                uiHandler.postDelayed(Runnable { searchRFCard(Runnable { respondCard() }) }, 1200)
            }
            else -> Log.d(TAG,"!!!! unknow flag !!!!")
        }
    }

    protected open fun searchRFCard(next: Runnable)
    {
        Log.d(TAG,"******* search RF card *******")
        val rfCardOption = CardOption.create()
            .supportICCard(false)
            .supportMagCard(false)
            .supportRFCard(true)
            .rfDeviceName(DemoConfig.RF_DEVICE_NAME)
            .toBundle()
        try
        {
            emv!!.searchCard(rfCardOption, DemoConfig.TIMEOUT, object : SearchListenerAdapter()
            {
                override fun onCardPass(cardType: Int)
                {
                    Log.d(TAG,"=> onCardPass | cardType = $cardType")
                    next.run()
                }

                override fun onTimeout()
                {
                    Log.d(TAG,"=> onTimeout")
                    stopEMV()
                    findNavController().navigate(R.id.action_fragmentTestEmv_to_fragmentTestSinContacto)
                    Toast.makeText(requireContext(), "Lectura Chip OK", Toast.LENGTH_SHORT).show()
                }

                override fun onError(code: Int, message: String?)
                {
                    Log.d(TAG,("=> onError | %s[0x%02X], $message, $code"))
                    stopEMV()
                    findNavController().navigate(R.id.action_fragmentTestEmv_to_fragmentTestSinContacto)
                    Toast.makeText(requireContext(), "Lectura Chip OK", Toast.LENGTH_SHORT).show()
                }
            })
            findNavController().navigate(R.id.action_fragmentTestEmv_to_fragmentTestSinContacto)


        }
        catch (e: Exception)
        {
            Log.d(TAG,e.message.toString())
        }
    }

    open fun doCardChecked(cardType: Int)
    {
        Log.d(TAG,"doCardChecked")
        // Only happen when use startProcess()
    }

    open fun doAppSelect(reSelect: Boolean, candList: List<CandidateAID>)
    {
        Log.d(TAG,"=> onAppSelect: cand AID size = " + candList.size)
        if (candList.size > 1)
        {
            selectApp(candList, object : OnSelectListener
            {
                override fun onCancel()
                {
                    try
                    {
                        emv!!.stopEMV()
                    }
                    catch (e: RemoteException)
                    {
                        e.printStackTrace()
                    }
                }

                override fun onSelected(item: Int)
                {
                    respondAID(candList[item].aid)
                    Toast.makeText(requireContext(), "Lectura Chip OK", Toast.LENGTH_SHORT).show()
                }
            })
        } else
        {
            respondAID(candList[0].aid)
        }

    }

    protected open fun selectApp(candList: List<CandidateAID>, listener: OnSelectListener)
    {
        Log.d(TAG,"selectAPP")
        val aidInfoList: MutableList<String> = ArrayList()
        for (candAid in candList)
        {
            aidInfoList.add(String(candAid.apn))
        }
        runOnUiThread(Runnable
        {
        })
    }

    interface OnSelectListener
    {
        fun onCancel()
        fun onSelected(item: Int)
    }

    protected open fun respondAID(aid: ByteArray?)
    {
        Log.d(TAG,"respondAID")
        try
        {
            Log.d(TAG,"Select aid: " + BytesUtil.bytes2HexString(aid))
            val tmAid = TLV.fromData(EMVTag.EMV_TAG_TM_AID, aid)
            Log.d(TAG,"+emv!!.respondEvent(tmAid.toString() ")
        }
        catch (e: Exception)
        {
            Log.d(TAG,e.message.toString())
        }
    }

    private fun runOnUiThread(action: Runnable?)
    {
        Log.d(TAG,"runOnUiThread")
        throw RuntimeException("Stub!")
    }


    @Throws(RemoteException::class)
    open fun doFinalSelect(finalData: FinalData)
    {
        Log.d(TAG,"=> onFinalSelect")
        var tlvList: String? = null
        when (finalData.kernelID.toInt())
        {
            KernelID.EMV ->
                // Parameter settings, see transaction parameters of EMV Contact Level 2 in《UEMV develop guide》
                // For reference only below
                tlvList =
                    "9F02060000000001009F03060000000000009A031710209F21031505129F410400000001" +
                            "9F3501229F3303E0F8C89F40056000F0A0019F1A0201565F2A0201569C0100" +
                            "DF9181040100DF91810C0130DF91810E0190"
            KernelID.PBOC ->                // if suport PBOC Ecash，see transaction parameters of PBOC Ecash in《UEMV develop guide》.
                // If support qPBOC, see transaction parameters of QuickPass in《UEMV develop guide》.
                // For reference only below
                tlvList =
                    "9F02060000000001009F03060000000000009A031710209F21031505129F4104000000019F660427004080"
            KernelID.VISA ->                // Parameter settings, see transaction parameters of PAYWAVE in《UEMV develop guide》.
                tlvList = StringBuilder()
                    .append("9C0100")
                    .append("9F0206000000000100")
                    .append("9A03171020")
                    .append("9F2103150512")
                    .append("9F410400000001")
                    .append("9F350122")
                    .append("9F1A020156")
                    .append("5F2A020156")
                    .append("9F1B0400003A98")
                    .append("9F660436004000")
                    .append("DF06027C00")
                    .append("DF812406000000100000")
                    .append("DF812306000000100000")
                    .append("DF812606000000100000")
                    .append("DF918165050100000000")
                    .append("DF040102")
                    .append("DF810602C000")
                    .append("DF9181040100").toString()
            KernelID.MASTER ->                // Parameter settings, see transaction parameters of PAYPASS in《UEMV develop guide》.
                tlvList = StringBuilder()
                    .append("9F350122")
                    .append("9F3303E0F8C8")
                    .append("9F40056000F0A001")
                    .append("9A03171020")
                    .append("9F2103150512")
                    .append("9F0206000000000100")
                    .append("9F1A020156")
                    .append("5F2A020156")
                    .append("9C0100")
                    .append("DF918111050000000000")
                    .append("DF91811205FFFFFFFFFF")
                    .append("DF91811005FFFFFFFFFF")
                    .append("DF9182010102")
                    .append("DF9182020100")
                    .append("DF9181150100")
                    .append("DF9182040100")
                    .append("DF812406000000010000")
                    .append("DF812506000000010000")
                    .append("DF812606000000010000")
                    .append("DF812306000000010000")
                    .append("DF9182050160")
                    .append("DF9182060160")
                    .append("DF9182070120")
                    .append("DF9182080120").toString()
            KernelID.AMEX -> {
            }
            KernelID.DISCOVER -> {
            }
            KernelID.JCB -> {
            }
            else ->
            {
            }
        }
        Log.d(TAG,"...onFinalSelect: setTLVList")
        Log.d(TAG,"...onFinalSelect: respondEvent")
        findNavController().navigate(R.id.action_fragmentTestEmv_to_fragmentTestWifi)
    }

    @Throws(RemoteException::class)
    open fun doReadRecord(record: CardRecord?)
    {
        Log.d(TAG,"=> onReadRecord ")
        Log.d(TAG,"...onReadRecord: respondEvent")
    }

    @Throws(RemoteException::class)
    open fun doCardHolderVerify(cvm: CVMMethod)
    {
        Log.d(TAG,"=> onCardHolderVerify | " + EMVInfoUtil.getCVMDataDesc(cvm))
        val param = Bundle()
        param.putByteArray(PinpadData.PIN_LIMIT, byteArrayOf(0, 4, 5, 6, 7, 8, 9, 10, 11, 12))
        val listener: OnPinEntryListener = object : OnPinEntryListener.Stub()
        {
            override fun onInput(arg0: Int, arg1: Int)
            {
                Log.d(TAG,"onInput")
            }
            override fun onConfirm(arg0: ByteArray, arg1: Boolean)
            {
                Log.d(TAG,"onConfirm")
                respondCVMResult(1.toByte())
            }

            override fun onCancel()
            {
                Log.d(TAG,"onCancel")
                respondCVMResult(0.toByte())
            }

            override fun onError(error: Int)
            {
                Log.d(TAG,"onError")
                respondCVMResult(2.toByte())
            }
        }
        when (cvm.cvm.toInt())
        {
            CVMFlag.EMV_CVMFLAG_OFFLINEPIN -> pinpad!!.startOfflinePinEntry(param, listener)
            CVMFlag.EMV_CVMFLAG_ONLINEPIN ->
            {
                Log.d(TAG,"=> onCardHolderVerify | onlinpin")
                param.putByteArray(PinpadData.PAN_BLOCK, lastCardRecord!!.pan)
                pinpad!!.startPinEntry(DemoConfig.KEYID_PIN, param, listener)
            }
            else ->
            {
                Log.d(TAG,"=> onCardHolderVerify | default")
                respondCVMResult(1.toByte())
            }
        }
    }

    protected open fun respondCVMResult(result: Byte)
    {
        Log.d(TAG,"respondCVMResult")
        try {
            val chvStatus = TLV.fromData(EMVTag.DEF_TAG_CHV_STATUS, byteArrayOf(result))
            val ret = emv!!.respondEvent(chvStatus.toString())
            Log.d(TAG, "...onCardHolderVerify: respondEvent")
        }
        catch (e: Exception)
        {
            Log.d(TAG, e.message.toString())
        }
    }

    @Throws(RemoteException::class)
    open fun doOnlineProcess(transData: TransData)
    {
        Log.d(TAG,"=> onOnlineProcess | TLVData for online:" + BytesUtil.bytes2HexString(transData.tlvData))
        val onlineResult: String = doOnlineProcess()
        val ret = emv!!.respondEvent(onlineResult)
        Log.d(TAG, "...onOnlineProcess: respondEvent + @ret")
    }

    open fun doOnlineProcess(): String
    {
        Log.d(TAG,"****** doOnlineProcess ******")

        val onlineSuccess = true
        return if (onlineSuccess)
        {
            val onlineResult = StringBuffer()
            onlineResult.append(EMVTag.DEF_TAG_ONLINE_STATUS).append("01").append("00")
            val hostRespCode = "3030"
            onlineResult.append(EMVTag.EMV_TAG_TM_ARC).append("02").append(hostRespCode)
            val onlineApproved = true
            onlineResult.append(EMVTag.DEF_TAG_AUTHORIZE_FLAG).append("01")
                .append(if (onlineApproved) "01" else "00")
            val hostTlvData =
                "9F3501229C01009F3303E0F1C89F02060000000000019F03060000000000009F101307010103A0A802010A010000000052856E2C9B9F2701809F260820F63D6E515BD2CC9505008004E8009F1A0201565F2A0201569F360201C982027C009F34034203009F37045D5F084B9A031710249F1E0835303530343230308408A0000003330101019F090200309F410400000001"
            onlineResult.append(TLV.fromData(EMVTag.DEF_TAG_HOST_TLVDATA,
                BytesUtil.hexString2Bytes(hostTlvData)).toString())
            onlineResult.toString()
        } else {
            Log.d(TAG,"!!! online failed !!!")
            "DF9181090101"
        }
    }

    open fun doEndProcess(result: Int, transData: TransData?)
    {
        Log.d(TAG,"doEndProcess")
        if (result != EMVError.SUCCESS)
        {
            Log.d(TAG,"=> onEndProcess | " + EMVInfoUtil.getErrorMessage(result))
        }
        else
        {
            Log.d(TAG,"=> onEndProcess | EMV_RESULT_NORMAL | " + EMVInfoUtil.getTransDataDesc(transData))
        }

    }

    open fun doVerifyOfflinePin(flag: Int, random: ByteArray?, capKey: CAPublicKey?, result: OfflinePinVerifyResult)
    {
        Log.d(TAG,"=> doVerifyOfflinePin")
        try
        {
            /** 内置插卡- 0；内置挥卡 – 6；外置设备接USB - 7；外置设备接COM口 -8  */
            /** inside insert card - 0；inside swing card – 6；External device is connected to the USB port - 7；External device is connected to the COM port -8  */
            val icToken = 0
            //Specify the type of "PIN check APDU message" that will be sent to the IC card.Currently only support VCF_DEFAULT.
            val cmdFmt = OfflinePinVerify.VCF_DEFAULT
            val offlinePinVerify = OfflinePinVerify(flag.toByte(), icToken, cmdFmt, random)
            val pinVerifyResult = PinVerifyResult()
            val ret = pinpad!!.verifyOfflinePin(offlinePinVerify, getPinPublicKey(capKey), pinVerifyResult)
            if (!ret)
            {
                Log.d(TAG,"verifyOfflinePin fail: " + pinpad!!.lastError)
                stopEMV()
                return
            }
            val apduRet = pinVerifyResult.apduRet
            val sw1 = pinVerifyResult.sW1
            val sw2 = pinVerifyResult.sW2
            result.setSW(sw1.toInt(), sw2.toInt())
            result.result = apduRet.toInt()
        }
        catch (e: Exception)
        {
            Log.d(TAG,e.message.toString())
        }
    }

    open fun getPinPublicKey(from: CAPublicKey?): PinPublicKey?
    {
        Log.d(TAG,"getPinPublicKey")
        if (from == null)
        {
            return null
        }
        val to = PinPublicKey()
        to.mRid = from.rid
        to.mExp = from.exp
        to.mExpiredDate = from.expDate
        to.mHash = from.hash
        to.mHasHash = from.hashFlag
        to.mIndex = from.index
        to.mMod = from.mod
        return to
    }

    open fun doSendOut(ins: Int, data: ByteArray)
    {
        Log.d(TAG,"doSendOut")

    }
//End Class
}

