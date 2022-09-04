package com.example.pair.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pair.R
import com.example.pair.data.CardRepo
import com.example.pair.data.PrefRepo
import com.example.pair.databinding.SplashFragmentBinding

class SplashFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: SplashFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SplashFragmentBinding.inflate(inflater, container, false)

        Glide.with(this).load(R.drawable.ic_pear).into(binding.splashLogo)

        binding.splashLogo.animate().apply {
            duration = 500
            alphaBy(1f)
        }.withEndAction {
            checkAccountNavigate()
        }
        return binding.root
    }

    private fun checkAccountNavigate() {
        val accountID = PrefRepo.getAccountID()
        when (accountID) {
            "" -> toCreateAccount()
            else -> {
                //viewModel.account = CardRepo.getCard(accountID)
                toPair()
            }
        }
    }

    // NAV METHODS //
    private fun toPair() {
        viewModel.showNavigation.value = true
        val action = SplashFragmentDirections.actionSplashToSwap()
        this.findNavController().navigate(action)
    }

    private fun toCreateAccount() {
        val action = SplashFragmentDirections.actionSplashToCreateAccountFragment()
        this.findNavController().navigate(action)
    }
}
