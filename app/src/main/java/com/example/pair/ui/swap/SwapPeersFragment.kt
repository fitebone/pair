package com.example.pair.ui.swap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pair.*
import com.example.pair.data.AccountRepo
import com.example.pair.data.CardRepo
import com.example.pair.data.SwapRepo
import com.example.pair.databinding.SwapPeersFragmentBinding
import com.example.pair.ui.card.AccountFragmentDirections
import com.example.pair.ui.card.AddContactInfoDialog
import com.example.pair.ui.main.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlin.Pair as _Pair

class SwapPeersFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: SwapPeersFragmentBinding

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: PeerRecyclerAdapter

    private lateinit var accountRepo: AccountRepo
    private lateinit var cardRepo: CardRepo
    private lateinit var pairRepo: SwapRepo

    private var cardMode = true
    private lateinit var account: Card

    // TODO compress into bool array or something
    private var pairCardSent = false
    private var pairCardReceived = false
    private var pairAvatarSent = false
    private var pairAvatarReceived = false
    private var cardPayloadId = 0.toLong()
    private var avatarPayloadId = 0.toLong()

    // Payload ID -> (Received Time, Peer)
    //private var peerPayloadMap = mutableMapOf<Long, _Pair<Long, Peer>>()

    private var pendingRequestDialog = PostRequestingPeerDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SwapPeersFragmentBinding.inflate(inflater, container, false)

        linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPeers.layoutManager = linearLayoutManager

        // Get reference to adapter from NearbyPair
        adapter = NearbyPair.init(requireActivity(), findNavController())
        binding.recyclerPeers.adapter = adapter
        adapter.onItemClick = { peer ->
            //val pendingReqDialog = PostRequestingPeerDialog()
            pendingRequestDialog.show(parentFragmentManager, "request")
            NearbyPair.requestConnection(peer)
            //showSnackBar("Requesting connection with ${peer.username}")
        }

        binding.peerToolbar.toolbarBack.setOnClickListener {
            NearbyPair.resetEndpoints()
            findNavController().navigateUp()
        }
        binding.peerToolbar.toolbarTitle.text = "Users Nearby"

        // TODO tell nearby to switch advertise etc. live data?
        binding.peerSegmentedButtons.isSingleSelection = true
        binding.peerSegmentedButtons.check(R.id.peer_button_card)
        binding.peerButtonCard.icon = resources.getDrawable(R.drawable.ic_check, null)
        binding.peerButtonCard.setOnClickListener {
            NearbyPair.cardMode = true
            binding.peerButtonCard.icon = resources.getDrawable(R.drawable.ic_check, null)
            binding.peerButtonFull.icon = null
        }
        binding.peerButtonFull.setOnClickListener {
            NearbyPair.cardMode = false
            binding.peerButtonFull.icon = resources.getDrawable(R.drawable.ic_check, null)
            binding.peerButtonCard.icon = null
        }

        NearbyPair.otherDialogUp.observe(viewLifecycleOwner, connectedToPeerObserver)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Automatically start search for peers
        account = AccountRepo.getAccount()
        viewModel.showNavigation.value = false

        NearbyPair.advertiseStart()
        NearbyPair.discoverStart()
    }

    override fun onPause() {
        super.onPause()
        viewModel.showNavigation.value = true
        NearbyPair.advertiseStop()
        NearbyPair.discoverStop()
        //NearbyPair.resetEndpoints()
        Log.i("PAIR", "Stop advertise & discovery")
        //adapter.clear()
    }

    private val connectedToPeerObserver = Observer<Boolean> {
        if (it) {
            if (pendingRequestDialog.isAdded) {
                pendingRequestDialog.dismiss()
            }
        }
    }

    //fun numberToByteArray (data: Number, size: Int = 4) : ByteArray =
    //    ByteArray (size) {i -> (data.toLong() shr (i*8)).toByte()}

    // TODO: Duplicate of SplashFragment func
    private fun showSnackBar(text: String) {
        Snackbar.make(
            binding.root,
            text,
            Snackbar.LENGTH_LONG
        ).show()
    }

    //fun toProgress(endpointID: String) {
    //    val action = SwapPeersFragmentDirections.actionPeersToProgress(endpointID)
     //   this.findNavController().navigate(action)
    //}
}