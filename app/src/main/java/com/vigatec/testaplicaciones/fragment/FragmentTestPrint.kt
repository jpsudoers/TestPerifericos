package com.vigatec.testaplicaciones.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.usdk.apiservice.aidl.printer.*
import com.vigatec.testaplicaciones.DeviceHelper
import com.vigatec.testaplicaciones.databinding.FragmentTestPrintBinding
import kotlinx.android.synthetic.main.fragment_test_print.*
import java.lang.Exception


class FragmentTestPrint : Fragment()
{

    private val TAG = "FragmentTestPrint"
    private var _binding: FragmentTestPrintBinding? = null
    private val binding get() = _binding!!

    //Printers variables
    private var printer: UPrinter? = null

    fun initDeviceInstance() { printer = DeviceHelper.me().getPrinter() }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle?): View?
    {
        Log.d(TAG, "onCreateView")
        _binding = FragmentTestPrintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        initDeviceInstance()
        btnEnablePrint.setOnClickListener {
            getStatus(view) }


    }

    fun getStatus(v: View?)
    {
        Log.d(TAG,"getStatus")

            val status = printer!!.status
            if (status != PrinterError.SUCCESS)
            {
                Log.d(TAG, "Impresora con error")
                Toast.makeText(requireContext(), "Impresora con error", Toast.LENGTH_SHORT).show()

                //     return
            }
        else
        {

            Log.d(TAG, "Impresora lista para imprimir")
            Toast.makeText(requireContext(), "Impresora lista", Toast.LENGTH_SHORT).show()
            printer!!.addText(AlignMode.CENTER, "Pruebas de pantalla")
            printer!!.addText(AlignMode.LEFT, "Prueba CYAN OK")
            printer!!.addText(AlignMode.LEFT, "Prueba MAGENTA OK")
            printer!!.addText(AlignMode.LEFT, "Prueba YELLOW OK")
            printer!!.addText(AlignMode.LEFT, "Prueba BLACK OK")
            printer!!.feedLine(1)
            printer!!.addText(AlignMode.CENTER, "Pruebas de lectura")
            printer!!.addText(AlignMode.LEFT, "Prueba BANDA OK")
            printer!!.addText(AlignMode.LEFT, "Prueba CHIP OK")
            printer!!.addText(AlignMode.LEFT, "Prueba SIN CONTACTO OK")
            printer!!.feedLine(1)
            printer!!.addText(AlignMode.CENTER, "Pruebas Wifi")
            printer!!.addText(AlignMode.LEFT, "Prueba WIFI OK")
            printer!!.feedLine(1)
            printer!!.addText(AlignMode.CENTER, "Prueba Impresión")
            printer!!.addText(AlignMode.LEFT, "Prueba IMPRESION OK")

            printer!!.startPrint (object : OnPrintListener.Stub() {
                override fun onFinish() {
                    /*
                        if(externalApp){
                            activity?.setResult(EXT_OK)
                            activity?.finish()
                        }
                        else
                         */
                  //  findNavController().popBackStack(R.id.mainMenuFragment, false)
                }
                override fun onError(i: Int) {
                    /*
                        if(externalApp){
                            activity?.setResult(EXT_ERROR_PRINTER)
                            activity?.finish()
                        }
                        else
                         */
                  //  findNavController().popBackStack(R.id.mainMenuFragment, false)
                }
            })
        }
    }








}

