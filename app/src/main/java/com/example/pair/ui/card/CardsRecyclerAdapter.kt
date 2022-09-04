package com.example.pair.ui.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.pair.AppDatabase
import com.example.pair.Card
import com.example.pair.PairApplication
import com.example.pair.Peer
import com.example.pair.databinding.RecyclerCardRowBinding
import com.example.pair.databinding.RecyclerPeersRowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class CardsRecyclerAdapter : RecyclerView.Adapter<CardsRecyclerAdapter.CardHolder>() {

    val db = PairApplication.room!!
    val userDao = (db as AppDatabase).userDao()

    var cards = mutableListOf<Card>()
    var onItemClick: ((Card) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CardHolder {
        //val inflatedView = parent.inflate(R.layout.recycler_peers_row, false)
        val binding = RecyclerCardRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardHolder(binding.root, binding)
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        val itemCard = cards[position]
        holder.bindCard(itemCard)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    fun clear() {
        cards.clear()
    }

    inner class CardHolder(v: View, binding: RecyclerCardRowBinding) : RecyclerView.ViewHolder(v) {
        private var card: Card? = null
        private var binding: RecyclerCardRowBinding

        init {
            v.setOnClickListener {
                onItemClick?.invoke(cards[adapterPosition])
            }
            this.binding = binding
        }

        fun bindCard(card: Card) {
            this.card = card
            // TODO: add pic here if user already paired with
            val image_file = File(binding.root.context.filesDir, card.id)
            if (image_file.exists()) {
                // Get last pair for metadata
                var metadata = ""
                runBlocking { launch(Dispatchers.Default) {
                    val latestPair = userDao.getLatestSwap(card.id)
                    metadata = card.id + latestPair.created
                } }
                Glide.with(binding.root)
                    .load(image_file)
                    .signature(ObjectKey(metadata))
                    .circleCrop()
                    .into(binding.cardImage)
            }
            binding.cardUsername.text = card.username
            binding.cardID.text = card.id
        }

        /*
        companion object {
            private val PEER_KEY = "PHOTO"
        }*/
    }
}