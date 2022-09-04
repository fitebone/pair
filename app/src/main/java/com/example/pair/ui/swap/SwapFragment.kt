package com.example.pair.ui.swap

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pair.data.NukeRepo
import com.example.pair.data.SwapRepo
import com.example.pair.databinding.SwapFragmentBinding
import com.example.pair.ui.main.MainViewModel

class SwapFragment : Fragment() {
    //val viewModel: MainViewModel by viewModels()
    private lateinit var binding: SwapFragmentBinding

    private lateinit var adapter: SwapRecyclerAdapter

    private lateinit var locationPermResultLauncher : ActivityResultLauncher<String>
    private fun Context.hasPermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED }
    private val isLocationPermGranted get() = requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private val isBluetoothGrantedA11 get() = requireContext().hasPermission(Manifest.permission.BLUETOOTH)
    private val isBluetoothAdvertisePermGranted @RequiresApi(Build.VERSION_CODES.S)
    get() = requireContext().hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    private val isBluetoothConnectPermGranted @RequiresApi(Build.VERSION_CODES.S)
    get() = requireContext().hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private val isBluetoothScanPermGranted @RequiresApi(Build.VERSION_CODES.S)
    get() = requireContext().hasPermission(Manifest.permission.BLUETOOTH_SCAN)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SwapFragmentBinding.inflate(inflater, container, false)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPair.layoutManager = linearLayoutManager
        adapter = SwapRecyclerAdapter()
        binding.recyclerPair.adapter = adapter

        val cards = SwapRepo.getAllPairs()
        adapter.pairs.addAll(cards)
        adapter.pairs.sortByDescending { it.created }

        binding.buttonAddPair.setOnClickListener {
            // Permission gauntlet
            if (!isLocationPermGranted) {
                locationPermResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (!isBluetoothAdvertisePermGranted || !isBluetoothConnectPermGranted || !isBluetoothScanPermGranted)) {
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE))
            }
            if (!isBluetoothGrantedA11){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetooth.launch(enableBtIntent)
            }

            // If all permissions granted then go to Peers
            var granted = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isLocationPermGranted && isBluetoothConnectPermGranted && isBluetoothScanPermGranted && isBluetoothAdvertisePermGranted) {
                    granted = true
                }
            } else {
                if (isLocationPermGranted && isBluetoothGrantedA11) {
                    granted = true
                }
            }
            if (granted) {
                toPeers()
            }
        }

        binding.buttonClearRoom.setOnClickListener {
            NukeRepo(requireActivity()).nukeAll()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationPermResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted : Boolean ->
            if (!isGranted) {
                // TODO: inform user they have denied enabling location access + consequences
            }
        }
    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                // TODO prob need to edit this shit
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    // NAV METHODS //
    private fun toPeers() {
        val action = SwapFragmentDirections.actionPairToPeers()
        this.findNavController().navigate(action)
    }
}