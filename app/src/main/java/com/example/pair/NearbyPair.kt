package com.example.pair

import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.collection.SimpleArrayMap
import androidx.core.os.bundleOf
import androidx.core.util.Predicate
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.pair.data.AccountRepo
import com.example.pair.data.CardRepo
import com.example.pair.data.SwapRepo
import com.example.pair.ui.swap.PeerRecyclerAdapter
import com.example.pair.ui.swap.PostRequestingPeerDialog
import com.example.pair.ui.swap.ProgressFragment
import com.example.pair.ui.swap.SwapPeersFragment
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.util.*


object NearbyPair {
    private lateinit var client: ConnectionsClient
    private lateinit var activity: FragmentActivity
    private lateinit var navController: NavController
    private lateinit var account: Card
    private val peerAdapter = PeerRecyclerAdapter()
    private val p2pAdvertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
    private val p2pDiscoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()

    private const val CARD_SERVICE = "pair_swap_card"
    //private const val FULL_SERVICE = "pair_swap_full"
    var cardMode = true

    // List of EndpointID -> Payload, that can be queued for processing
    //private var payloadsQueue: MutableLiveData<MutableList<Pair<String, Payload>>> = MutableLiveData(mutableListOf())
    var payloadProgress: MutableLiveData<MutableMap<String, MutableMap<Long, Pair<Long, Long>>>> = MutableLiveData(mutableMapOf())
    var otherDialogUp: MutableLiveData<Boolean> = MutableLiveData(false)
    var byteTargetReceiveConfirm = false

    private var payloadsSent: MutableMap<String, MutableList<Payload>> = mutableMapOf()
    private var payloadsReceived: MutableMap<String, MutableList<Payload>> = mutableMapOf()

    var payloadByteSent: Long = 0
    var payloadByteReceived: Long = 0
    var payloadByteTotal: Double = 0.0

    // Maps to store metadata counts of payloads that are expected to be sent and received
    // TODO Combine count maps
    private var payloadSendCount: MutableMap<String, Int> = mutableMapOf()
    private var payloadReceiveCount: MutableMap<String, Int> = mutableMapOf()

    // Metadata payloads to ignore
    private var ignorePayloads: MutableList<Long> = mutableListOf()

    // Endpoint to Peer map
    var endpointPeerMap: MutableMap<String, Peer> = mutableMapOf()

    fun init(activity: FragmentActivity, navController: NavController) : PeerRecyclerAdapter {
        client = Nearby.getConnectionsClient(activity)
        this.activity = activity
        this.navController = navController
        account = AccountRepo.getAccount()
        //payloadsQueue.observe(activity, cardPayloadObserver)
        return peerAdapter
    }

    // CORE API //
    fun advertiseStart() {
        val account = AccountRepo.getAccount()
        val identifier = account.username+":@:"+account.id
        if (cardMode) {
            // Card Service
            client
                .startAdvertising(identifier, CARD_SERVICE, cardSwapReceiveCallback, p2pAdvertisingOptions)
                .addOnSuccessListener { Log.i("PAIR", "Advertising Card Swap...") }
                .addOnFailureListener { e -> Log.e("PAIR", "Advertise Card Swap failed to start, $e") }
        } else {
            // Full service
        }
    }

    fun advertiseStop() {
        client.stopAdvertising()
    }

    fun discoverStart() {
        if (cardMode) {
            client
                .startDiscovery(CARD_SERVICE, endpointDiscoveryCallback, p2pDiscoveryOptions)
                .addOnSuccessListener { Log.i("PAIR", "Discovering Card Swap...") }
                .addOnFailureListener { e -> Log.e("PAIR", "Discover Card Swap failed to start, $e") }
        } else {

        }
    }

    fun discoverStop() {
        client.stopDiscovery()
    }

    fun requestConnection(peer: Peer) {
        if (cardMode) {
            client.requestConnection(peer.username, peer.endpointID, cardSwapInitiateCallback).addOnSuccessListener {
                Log.i("PAIR", "Requesting connection with $peer")
            }.addOnFailureListener { e: Exception? ->
                Log.e("PAIR", "Failed requesting connection with $peer; Error: $e")
            }
        } else {

        }

    }

    fun resetEndpoints() {
        client.stopAllEndpoints()
        val len = peerAdapter.itemCount
        peerAdapter.clear()
        peerAdapter.notifyItemRangeRemoved(0, len)
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // An endpoint was found.
                val peerInfo = info.endpointName.split(":@:")
                val peer = Peer(endpointId, peerInfo[1], peerInfo[0])
                if (!peerAdapter.peers.contains(peer)) {
                    peerAdapter.peers.add(peer)
                    peerAdapter.notifyItemInserted(peerAdapter.itemCount)
                }
                endpointPeerMap[endpointId] = peer
                Log.i("PAIR", "Endpoint discovered! Current peers: ${peerAdapter.peers}")
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
                // TODO just swap with normal iteration delete so notify is more precise
                val idMatch = Predicate { id: String -> id == endpointId }
                peerAdapter.peers.removeIf { peer: Peer -> idMatch.test(peer.uuid) }
                peerAdapter.notifyDataSetChanged()
                Log.i("PAIR", "Endpoint lost... Current peers: ${peerAdapter.peers}")
            }
        }

    private val cardSwapInitiateCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                otherDialogUp.value = true
                activity.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("Yes") { _, _ ->
                            client.acceptConnection(endpointId, cardPayloadListener)
                        }
                        setNegativeButton("No") { _, _ ->
                            client.rejectConnection(endpointId)
                        }
                        setTitle("Connect to ${endpointPeerMap[endpointId]!!.username}?")
                        setMessage("Code: ${connectionInfo.authenticationDigits}")
                        setCancelable(false)
                    }
                    builder.show()
                }
            }

            // TODO make helper function so this isnt repeated in receiverCallback below
            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        connectionResultSucc(endpointId)
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        //showSnackBar("$endpointId rejected connection")
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {}
                    else -> {}
                }
            }

            override fun onDisconnected(endpointId: String) {
                disconnectProtocol(endpointId)
            }
        }

    private val cardSwapReceiveCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                otherDialogUp.value = true
                activity.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("Yes") { _, _ ->
                            client.acceptConnection(endpointId, cardPayloadListener)
                        }
                        setNegativeButton("No") { _, _ ->
                            client.rejectConnection(endpointId)
                        }
                        // TODO name is wrong....
                        //connectionInfo.
                        setTitle("${endpointPeerMap[endpointId]!!.username} wants to connect! Accept?")
                        setMessage("Code: ${connectionInfo.authenticationDigits}")
                        setCancelable(false)
                    }
                    // Create the AlertDialog
                    builder.show()
                }
            }

            // TODO make helper function so this isnt repeated in receiverCallback below
            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        connectionResultSucc(endpointId)
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        //showSnackBar("$endpointId rejected connection")
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {}
                    else -> {}
                }
            }

            override fun onDisconnected(endpointId: String) {
                disconnectProtocol(endpointId)
            }
        }

    private val cardPayloadListener : PayloadCallback =
        object : PayloadCallback() {
            private val incomingPayloads = SimpleArrayMap<Long, Payload>()
            //private val completedFilePayloads = SimpleArrayMap<Long, Payload>()

            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                // TODO dangerous?
                when (payload.type) {
                    Payload.Type.BYTES -> {
                        // TODO try catch
                        val receivedBytes = payload.asBytes()!!
                        // Payload count payload received
                        if (receivedBytes.size == 4) {
                            ignorePayloads.add(payload.id)
                            val payloadsToReceive = byteArrayToInt(receivedBytes)
                            payloadReceiveCount[endpointId] = payloadsToReceive
                            Log.i("PAIR", "Payloads to receive: $payloadsToReceive")
                        } else if (receivedBytes.size == 8) {
                            // TODO ensure this is received before file, send ACK?
                            ignorePayloads.add(payload.id)
                            payloadByteReceived = byteArrayToLong(receivedBytes)
                            Log.i("PAIR", "Bytes to receive: $payloadByteReceived")
                            byteTargetReceiveConfirm = true
                            payloadByteTotal += payloadByteReceived.toDouble()
                            //var temp = payloadProgress.value!!
                            //temp[endpointId]!![payload.id] = 0
                            //payloadProgress.value = temp
                        } else {
                            incomingPayloads.put(payload.id, payload)
                            Log.i("PAIR", "Receiving card data...")
                        }
                    }
                    Payload.Type.FILE -> {
                        incomingPayloads.put(payload.id, payload)
                        Log.i("PAIR", "Receiving avatar file...")
                    }
                    else -> {}
                }
            }

            // Be wary, works on both incoming and outgoing
            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                when (update.status) {
                    PayloadTransferUpdate.Status.IN_PROGRESS -> {
                        //val payload = incomingPayloads.get(update.payloadId)
                        if (!ignorePayloads.contains(update.payloadId)) {
                            // TODO show file transfer progress
                            //Log.i("PAIR", "Update of ${update.bytesTransferred} bytes")
                            //payloadByteReceivedSoFar += update.bytesTransferred
                            var temp = payloadProgress.value!!
                            temp[endpointId]!![update.payloadId] = Pair(update.bytesTransferred, update.totalBytes)
                            payloadProgress.value = temp
                        }
                    }
                    PayloadTransferUpdate.Status.SUCCESS -> {
                        val payload = incomingPayloads.remove(update.payloadId)
                        if (!ignorePayloads.contains(update.payloadId)) {

                            if (payload != null) {
                                // TODO check that file is valid image
                                if (payload.type == Payload.Type.FILE) {
                                    processAvatarFile(payload, endpointId)
                                    payloadsReceived[endpointId]!!.add(payload)
                                    //payloadByteReceived += payload.asFile()!!.size
                                    Log.i("PAIR", "Avatar file size: ${payload.asFile()!!.size}, Payload ${payload.id}")
                                } else {
                                    processCardData(payload)
                                    payloadsReceived[endpointId]!!.add(payload)
                                    //payloadByteReceived += payload.asBytes()!!.size
                                    Log.i("PAIR", "Card data size: ${payload.asBytes()!!.size}, Payload ${payload.id}")
                                }

                                // TODO no clue where to put this lol
                                //var temp = payloadProgress.value!!
                                //temp[endpointId]!![update.payloadId] = update.bytesTransferred
                               // payloadProgress.value = temp

                                //var temp = payloadsQueue.value!!
                                //temp.add(Pair(endpointId, payload))
                                //payloadsQueue.value = temp

                                // Add payload to received list for endpoint
                                //payloadsReceived[endpointId]!!.add(payload)

                                // If at least something has been sent to this endpoint
                                if (payloadsSent[endpointId] != null) {
                                    // If payloads sent and received match number expected, then disconnect
                                    if (payloadsSent[endpointId]!!.size == payloadSendCount[endpointId]
                                        && payloadsReceived[endpointId]!!.size == payloadReceiveCount[endpointId]) {
                                        // If receiving more bytes than sending
                                        if (payloadByteSent < payloadByteReceived) {
                                            //client.stopAllEndpoints()
                                            // TODO WE KNOW WE DISCONNECTED and SO DOES PEER!! Time to clean up
                                            client.disconnectFromEndpoint(endpointId)
                                            disconnectProtocol(endpointId)
                                            /*val killPayload = Payload.fromBytes("COMPLETE".encodeToByteArray())
                                            val killTask = client.sendPayload(endpointId, killPayload)
                                            killTask.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Log.i("PAIR", "Kill payload sent successfully")
                                                    client.disconnectFromEndpoint(endpointId)
                                                } else {
                                                    Log.e("PAIR", "Error sending kill payload: ${task.exception}")
                                                }
                                            }*/
                                        }
                                    }
                                }
                            }
                        } /*else {
                            // If a META payload
                            if (payload != null) {
                                if (payload.type == Payload.Type.BYTES) {
                                    if (payloadsSent[endpointId]!!.size == payloadSendCount[endpointId]
                                        && payloadsReceived[endpointId]!!.size == payloadReceiveCount[endpointId]) {
                                        // Must be kill payload
                                        val byteDataString = payload.asBytes()!!.decodeToString()
                                        Log.i("PAIR", "Kill payload received! $byteDataString")
                                        if (byteDataString == "COMPLETE") {
                                            //Log.i("PAIR", "Kill payload received!")
                                            //client.disconnectFromEndpoint(endpointId)
                                            client.stopAllEndpoints()
                                        }
                                    }
                                }
                            }
                            // Needed?

                        }*/
                        /*if (payloadId == avatarPayloadId) {
                            pairAvatarReceived = true
                            val avatarPayload = completedFilePayloads.get(payloadId)
                            completedFilePayloads.remove(payloadId)

                            var tempList = viewModel.fileDataReceived.value
                            tempList!!.add(avatarPayload!!)
                            viewModel.fileDataReceived.value = tempList
                        }
                        if (payloadId == cardPayloadId || payloadId == avatarPayloadId) {
                            // If both card and avatar have been sent and received
                            if (pairCardSent && pairCardReceived && pairAvatarSent && pairAvatarReceived) {
                                // TODO just disconnect from one dude?
                                nearbyClient.stopAllEndpoints()
                            }
                        }*/
                    }
                    PayloadTransferUpdate.Status.FAILURE -> {}
                    PayloadTransferUpdate.Status.CANCELED -> {}
                    else -> {}
                }
            }
        }

    // TODO absolute shite name for a function
    private fun disconnectProtocol(endpointId: String) {
        Log.i("PAIR", "Disconnected from $endpointId")
        //showSnackBar("Initiator: Disconnected from $endpointId")
        payloadByteReceived = 0
        //payloadByteReceivedSoFar = 0
        payloadsSent[endpointId]!!.clear()
        payloadsReceived[endpointId]!!.clear()
        payloadSendCount[endpointId] = 0
        payloadReceiveCount[endpointId] = 0
        payloadProgress.value!!.remove(endpointId)
        otherDialogUp.value = false
        byteTargetReceiveConfirm = false
        payloadByteTotal = 0.0
    }

    private fun connectionResultSucc(endpointId: String) {
        // TODO: make pretty graphic here
        //showSnackBar("Swapping pair data with $endpointId!")
        var temp = payloadProgress.value!!
        temp[endpointId] = mutableMapOf()
        payloadProgress.value = temp

        val bundle = bundleOf("endpointID" to endpointId)
        navController.navigate(R.id.progress, bundle)

        // Disable visibility to other peers while pairing
        client.stopAdvertising()
        client.stopDiscovery()
        peerAdapter.clear()

        // Initialize list of payloads sent/received to this endpoint
        payloadsSent[endpointId] = mutableListOf()
        payloadsReceived[endpointId] = mutableListOf()

        // Init payload counter
        var payloadsCount = 0
        var byteCount = 0.toLong()

        // Setup card payload
        val json = Json.encodeToString(account).encodeToByteArray()
        val cardPayload = Payload.fromBytes(json)
        payloadsCount += 1
        byteCount += cardPayload.asBytes()!!.size

        // Setup avatar payload
        val avatarFile = File(activity.baseContext.filesDir, "account_image")
        lateinit var avatarPayload: Payload
        if (avatarFile.exists()) {
            avatarPayload = Payload.fromFile(avatarFile)
            payloadsCount += 1
            byteCount += avatarPayload.asFile()!!.size
        }

        // Send peer the # of payloads they should expect
        payloadSendCount[endpointId] = payloadsCount
        val pCountPayload = Payload.fromBytes(numberToByteArray(payloadsCount))
        val sendPayloadCountTask = client.sendPayload(endpointId, pCountPayload)
        sendPayloadCountTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("PAIR", "Payload count $payloadsCount sent successfully")
            } else {
                Log.e("PAIR", "Error sending payload count: ${task.exception}")
            }
        }

        // Send peer the # of bytes they should expect
        payloadByteSent = byteCount
        payloadByteTotal += byteCount.toDouble()
        val pBytePayload = Payload.fromBytes(numberToByteArray(byteCount, 8))
        val sendPayloadByteCountTask = client.sendPayload(endpointId, pBytePayload)
        sendPayloadByteCountTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("PAIR", "Payload byte count $byteCount sent successfully")
            } else {
                Log.e("PAIR", "Error sending payload byte count: ${task.exception}")
            }
        }

        // Send card data, will always arrive after payload count
        val sendCardTask = client.sendPayload(endpointId, cardPayload)
        sendCardTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("PAIR", "Card data sent successfully")
                payloadsSent[endpointId]!!.add(cardPayload)
            } else {
                Log.e("PAIR", "Error sending card: ${task.exception}")
            }
        }

        // Only send avatar if file exists to send
        if (payloadsCount == 2) {
            val sendAvatarTask = client.sendPayload(endpointId, avatarPayload)
            sendAvatarTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("PAIR", "Avatar data sent successfully")
                    payloadsSent[endpointId]!!.add(cardPayload)
                } else {
                    Log.e("PAIR", "Error sending avatar: ${task.exception}")
                }
            }
        }
    }

    private fun processAvatarFile(payload: Payload, endpointID: String) {
        val uri = payload.asFile()!!.asUri()
        try {
            // Copy the file to a new location.
            //val idMatch = Predicate { id: String -> id == endpointID }
            //val peer = peerAdapter.peers.first { peer: Peer -> idMatch.test(peer.endpointID) }
            val inf: InputStream? = activity.contentResolver.openInputStream(uri!!)
            copyInputStreamToFile(inf!!, File(activity.filesDir, endpointPeerMap[endpointID]!!.uuid))
        } catch (e: java.lang.Exception) {
            // Log the error.
            Log.e("PAIR", "Error: $e")
        } finally {
            // Delete the original file.
            activity.contentResolver.delete(uri!!, null, null)
        }
        Log.i("PAIR", "Avatar file processed!")
    }

    private fun processCardData(payload: Payload) {
        val byteDataString = payload.asBytes()!!.decodeToString()
        val newCard = Json.decodeFromString<Card>(byteDataString)
        if (!CardRepo.doesCardExist(newCard.id)) {
            CardRepo.insertCard(newCard)
        }
        // TODO move this somewhere more logical?
        val pair = Swap(UUID.randomUUID().toString(), account.id, newCard.id, System.currentTimeMillis() / 1000L)
        SwapRepo.insertPair(pair)
        Log.i("PAIR", "Card data processed!")
    }

    /*private val simpleObserver = Observer<MutableList<Pair<String, Int>>> {
        if (it.isNotEmpty()) {

        }
    }*/

    /*private val cardPayloadObserver = Observer<MutableList<Pair<String, Payload>>> {
        if (it.isNotEmpty()) {
            val firstItem = it.removeAt(0)
            val payload = firstItem.second
            val endpointID = firstItem.first
            when (payload.type) {
                Payload.Type.BYTES -> {
                    Log.i("PAIR", "Observing new Byte data")
                    val byteDataString = payload.asBytes()!!.decodeToString()
                    val newCard = Json.decodeFromString<Card>(byteDataString)
                    if (!CardRepo.doesCardExist(newCard.id)) {
                        CardRepo.insertCard(newCard)
                    }
                    // TODO move this somewhere more logical?
                    val pair = Swap(UUID.randomUUID().toString(), account.id, newCard.id, System.currentTimeMillis() / 1000L)
                    SwapRepo.insertPair(pair)
                }
                Payload.Type.FILE -> {
                    Log.i("PAIR", "Observing new File data")
                    val uri = payload.asFile()!!.asUri()
                    try {
                        // Copy the file to a new location.
                        val idMatch = Predicate { id: String -> id == endpointID }
                        val peer = peerAdapter.peers.first { peer: Peer -> idMatch.test(peer.endpointID) }
                        val inf: InputStream? = activity.contentResolver.openInputStream(uri!!)
                        copyInputStreamToFile(inf!!, File(activity.filesDir, peer.uuid))
                    } catch (e: IOException) {
                        // Log the error.
                    } finally {
                        // Delete the original file.
                        activity.contentResolver.delete(uri!!, null, null)
                    }
                }
                else -> {}
            }
        }
    }*/
}

fun updateProgress() {

}

private fun numberToByteArray (data: Number, size: Int = 4) : ByteArray =
    ByteArray (size) {i -> (data.toLong() shr (i*8)).toByte()}

private fun byteArrayToInt(bytes: ByteArray): Int {
    var result = 0
    var shift = 0
    for (byte in bytes) {
        result = result or (byte.toInt() shl shift)
        shift += 8
    }
    return result
}

private fun byteArrayToLong(bytes: ByteArray): Long {
    var offset = 0
    return (bytes[offset++].toLong() and 0xffL) or
            (bytes[offset++].toLong() and 0xffL shl 8) or
            (bytes[offset++].toLong() and 0xffL shl 16) or
            (bytes[offset++].toLong() and 0xffL shl 24) or
            (bytes[offset++].toLong() and 0xffL shl 32) or
            (bytes[offset++].toLong() and 0xffL shl 40) or
            (bytes[offset++].toLong() and 0xffL shl 48) or
            (bytes[offset].toLong() and 0xffL shl 56)
}

private fun copyInputStreamToFile(`in`: InputStream, file: File) {
    var out: OutputStream? = null
    try {
        out = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        // Ensure that the InputStreams are closed even if there's an exception.
        try {
            out?.close()
            // If you want to close the "in" InputStream yourself then remove this
            // from here but ensure that you close it yourself eventually.
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

