package com.example.pair.ui.swap

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pair.R

class PostRequestingPeerDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.post_requesting_peer_dialog, null)

            builder.setView(view)
                .setCancelable(false)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}