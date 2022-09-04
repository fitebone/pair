package com.example.pair.ui.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pair.Card
import com.example.pair.data.AccountRepo
import com.example.pair.data.CardRepo
import com.example.pair.databinding.CreateAccountFragmentBinding
import com.example.pair.ui.main.MainViewModel
import java.util.*

class CreateAccountFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: CreateAccountFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateAccountFragmentBinding.inflate(inflater, container, false)

        binding.createAccountButton.setOnClickListener {
            createNewAccount()
        }

        return binding.root
    }

    private fun createNewAccount() {
        // TODO better name checking
        //val delimiter = resources.getString(R.string.delimiter)
        var username = binding.usernameEdittext.text.toString()
        if (username.isEmpty()) {
            username = "Anonymous"
        } //else if (username.contains(delimiter)) {
            // TODO show red text with error under edittext
        //}
        val id = UUID.randomUUID().toString()
        val card = Card(
            id = id,
            username = username,
            created = System.currentTimeMillis() / 1000L,
            card_count = 0
        )

        // Insert data through repositories
        CardRepo.insertCard(card)
        AccountRepo.setID(id)

        // TODO delete if not needed
        //val accountImage = File(requireContext().filesDir, "avatar")
        //if (!accountImage.exists()) {
        //    accountImage.createNewFile()
        //}

        toPair()
    }

    // NAV METHODS //
    private fun toPair() {
        val action = CreateAccountFragmentDirections.actionCreateAccountFragmentToSwap()
        viewModel.showNavigation.value = true
        this.findNavController().navigate(action)
    }
}