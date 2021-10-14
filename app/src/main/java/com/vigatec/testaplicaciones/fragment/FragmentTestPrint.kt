package com.vigatec.testaplicaciones.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vigatec.testaplicaciones.databinding.FragmentTestPrintBinding


class FragmentTestPrint : Fragment()
{

    private val TAG = "FragmentTestPrint"
    private var _binding: FragmentTestPrintBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTestPrintBinding.inflate(inflater, container, false)
        return binding.root
    }


}