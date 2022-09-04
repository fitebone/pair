package com.example.pair.ui.swap

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pair.databinding.RecyclerPeersRowBinding

import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.pair.Peer
import com.example.pair.data.CardRepo
import com.example.pair.data.SwapRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class PeerRecyclerAdapter: RecyclerView.Adapter<PeerRecyclerAdapter.PeerHolder>() {
    var peers = mutableListOf<Peer>()
    var onItemClick: ((Peer) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PeerHolder {
        val binding = RecyclerPeersRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PeerHolder(binding.root, binding)
    }

    override fun onBindViewHolder(holder: PeerHolder, position: Int) {
        val itemPeer = peers[position]
        holder.bindPeer(itemPeer)
    }

    override fun getItemCount(): Int {
        return peers.size
    }

    fun clear() {
        peers.clear()
    }

    inner class PeerHolder(v: View, binding: RecyclerPeersRowBinding) : RecyclerView.ViewHolder(v) {
        private var peer: Peer? = null
        private var binding: RecyclerPeersRowBinding

        init {
            v.setOnClickListener {
                onItemClick?.invoke(peers[adapterPosition])
            }
            this.binding = binding
        }

        fun bindPeer(peer: Peer) {
            this.peer = peer
            val peerImage = CardRepo.getCardAvatar(peer.uuid, binding.root.context)

            if (peerImage != null) {
                if (SwapRepo.doesSwapExistWithPeer(peer.uuid)) {
                    val swap = SwapRepo.getLatestSwapWithPeer(peer.uuid)
                    Glide.with(binding.root)
                        .load(peerImage)
                        .signature(ObjectKey(peer.uuid + swap.created))
                        .circleCrop()
                        .into(binding.peerImage)
                }
            }
            binding.peerName.text = peer.username
        }
    }
}