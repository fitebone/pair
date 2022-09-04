package com.example.pair.ui.swap

import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pair.NearbyPair
import com.example.pair.databinding.ProgressFragmentBinding
import com.example.pair.ui.main.MainViewModel


class ProgressFragment : Fragment()  {

    private val viewModel: MainViewModel by activityViewModels()
    lateinit var binding: ProgressFragmentBinding
    private val args: ProgressFragmentArgs by navArgs()

    private lateinit var endpointID: String
    //var bytesToReceive = NearbyPair.payloadByteReceived
    //private var bytesReceived = 0.toLong()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProgressFragmentBinding.inflate(inflater, container, false)

        endpointID = args.endpointID
        val peer = NearbyPair.endpointPeerMap[endpointID]!!

        binding.progressToolbar.toolbarBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.progressToolbar.toolbarTitle.text = "Swapping with ${peer.username}"

        binding.progressButtonPeers.setOnClickListener {
            findNavController().navigateUp()
        }
        // TODO make account frag accept var id and shit
        //binding.progressButtonViewCard.setOnClickListener {}

        // Observe NearbyPair public progress data
        NearbyPair.payloadProgress.observe(viewLifecycleOwner, payloadProgressObserver)

        return binding.root
    }

    private val payloadProgressObserver = Observer<MutableMap<String, MutableMap<Long, Pair<Long, Long>>>> {
        if (it.isNotEmpty() and NearbyPair.byteTargetReceiveConfirm) {
            // TODO check if null?
            val peerBytes = it[endpointID]!!
            var receivedBytes = 0.0
            val expectedBytes = NearbyPair.payloadByteTotal
            for (payload in peerBytes) {
                receivedBytes += payload.value.first
            }

           // if (expectedBytes != 0.0) {
            val percentTransferred = 100.0 * (receivedBytes / expectedBytes)
            val intTransferred = percentTransferred.toInt()
            binding.progressBytes.text = "${receivedBytes.toInt()}B/${expectedBytes.toInt()}B"
            binding.progressPercent.text = "${intTransferred}%"
            binding.progressCircular.progress = intTransferred
            if (intTransferred == 100) {
                binding.progressMessage.text = "Complete!"
                binding.progressButtonPeers.visibility = View.VISIBLE
                binding.progressButtonViewCard.visibility = View.VISIBLE
            }
            //}
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.showNavigation.value = false
    }

    override fun onPause() {
        super.onPause()
        binding.progressMessage.text = "Swapping data..."
        binding.progressButtonPeers.visibility = View.GONE
        binding.progressButtonViewCard.visibility = View.GONE
    }
}