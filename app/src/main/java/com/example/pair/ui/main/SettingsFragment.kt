package com.example.pair.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pair.R
import com.example.pair.databinding.AccountFragmentBinding
import com.example.pair.databinding.SettingsFragmentBinding


class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: SettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)

        binding.toolbar.toolbarBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // TODO: Make settings for each TOP LEVEL nav destination
        binding.toolbar.toolbarTitle.text = "Settings"

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.showNavigation.value = false
    }

    override fun onPause() {
        super.onPause()
        viewModel.showNavigation.value = true
    }

}