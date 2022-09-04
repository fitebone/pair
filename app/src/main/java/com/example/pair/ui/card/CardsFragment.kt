package com.example.pair.ui.card

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pair.AppDatabase
import com.example.pair.PairApplication
import com.example.pair.data.AccountRepo
import com.example.pair.data.CardRepo
import com.example.pair.databinding.CardsFragmentBinding
import com.example.pair.ui.card.CardsRecyclerAdapter
import com.google.android.material.snackbar.Snackbar

class CardsFragment : Fragment() {
    //private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: CardsFragmentBinding

    private lateinit var adapter: CardsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CardsFragmentBinding.inflate(inflater, container, false)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCards.layoutManager = linearLayoutManager
        adapter = CardsRecyclerAdapter()
        binding.recyclerCards.adapter = adapter

        adapter.onItemClick = {
            showSnackBar("Touched user ${it.username}")
        }

        val localId = AccountRepo.getID()
        val cards = CardRepo.getAllCards(localId)
        adapter.cards.addAll(cards)
        //runBlocking { launch(Dispatchers.Default) {
        //    val cards = userDao.getAllCards(local_id)
         //   adapter.cards.addAll(cards)
        //} }

        return binding.root
    }

    // TODO: Duplicate of SplashFragment func
    private fun showSnackBar(text: String) {
        Snackbar.make(
            binding.root,
            text,
            Snackbar.LENGTH_LONG
        ).show()
    }
}