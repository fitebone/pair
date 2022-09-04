package com.example.pair.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pair.R
import com.example.pair.databinding.DropFragmentBinding

class DropFragment : Fragment() {

    companion object {
        fun newInstance() = DropFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: DropFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DropFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }
}