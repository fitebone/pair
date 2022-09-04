package com.example.pair.ui.card

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pair.*

class AddContactInfoDialog(adapter: ContactInfoListAdapter): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.addcontactinfo_dialog, null)

            builder.setTitle("Add New Contact Info")
                .setView(view)
                .setPositiveButton("Add") { _, _ ->
                    // Add contact info to Room
                    //val label = view.findViewById<EditText>(R.id.contact_info_label).text
                    //val data = view.findViewById<EditText>(R.id.contact_info_data).text
                    //val info = ContactInfo(label.toString(), "owner", data.toString())
                    //runBlocking { launch {
                    //    userDao.insertCI(info)
                    //}}
                    //// Notify data set changed idk
                    //adapter.info.add(info)
                    //adapter.notifyDataSetChanged()
                    //// Make toast confirming completion
                    //Toast.makeText(context, "Added $label: $data", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    // User cancelled the dialog
                    //getDialog()?.cancel()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}