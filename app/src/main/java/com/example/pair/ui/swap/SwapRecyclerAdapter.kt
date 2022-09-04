package com.example.pair.ui.swap

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.pair.Swap
import com.example.pair.data.CardRepo
import com.example.pair.databinding.RecyclerPairsRowBinding
import java.util.*

class SwapRecyclerAdapter: RecyclerView.Adapter<SwapRecyclerAdapter.SwapHolder>() {

    var pairs = mutableListOf<Swap>()
    var onItemClick: ((Swap) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SwapHolder {
        val binding = RecyclerPairsRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SwapHolder(binding.root, binding)
    }

    override fun onBindViewHolder(holder: SwapHolder, position: Int) {
        val itemSwap = pairs[position]
        holder.bindSwap(itemSwap)
    }

    override fun getItemCount(): Int {
        return pairs.size
    }

    inner class SwapHolder(v: View, binding: RecyclerPairsRowBinding) : RecyclerView.ViewHolder(v) {
        private var swap: Swap? = null
        private var binding: RecyclerPairsRowBinding = binding

        init {
            v.setOnClickListener {
                onItemClick?.invoke(pairs[adapterPosition])
            }
        }

        fun bindSwap(swap: Swap) {
            this.swap = swap
            val peer1 = CardRepo.getCard(swap.peerID_1)
            val peer2 = CardRepo.getCard(swap.peerID_2)
            binding.pairName1.text = peer1.username
            binding.pairName2.text = peer2.username
            binding.pairGestureText.text = Date(swap.created*1000).toString()
            val peer1image = CardRepo.getCardAvatar(peer1.id, binding.root.context)
            val peer2image = CardRepo.getCardAvatar(peer2.id, binding.root.context)
            if (peer1image != null) {
                Glide.with(binding.root)
                    .load(peer1image)
                    .signature(ObjectKey(peer1.id + swap.created))
                    .circleCrop()
                    .into(binding.pairImage1)
            }
            if (peer2image != null) {
                Glide.with(binding.root)
                    .load(peer2image)
                    .signature(ObjectKey(peer2.id + swap.created))
                    .circleCrop()
                    .into(binding.pairImage2)
            }
        }
    }
}