package com.example.pair.ui.card

import android.widget.ArrayAdapter
import android.widget.TextView
import android.content.Context
import android.view.ViewGroup
import android.view.View
import android.view.LayoutInflater
import com.example.pair.ContactInfo
import com.example.pair.R

class ContactInfoListAdapter(context: Context): ArrayAdapter<Any?>(context, 0) {

    val info = mutableListOf<ContactInfo>()

    override fun getCount(): Int {
        return info.size
    }

    override fun getItem(position: Int): Any? {
        return info[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder // to reference the child views for later actions

        if (v == null) {
            val inflater = LayoutInflater.from(parent.context)
            v = inflater.inflate(R.layout.contact_info_row, null)
            // cache view fields into the holder
            holder = ViewHolder()
            holder.label = v.findViewById(R.id.list_info_label)
            holder.data = v.findViewById(R.id.list_info_data)
            // associate the holder with the view for later lookup
            v!!.setTag(holder)
        } else {
            // view already exists, get the holder instance from the view
            holder = v.tag as ViewHolder
        }
        // no local variables with findViewById here

        holder.label?.text = info[position].label
        holder.data?.text = info[position].data

        // use holder.nameText where you were
        // using the local variable nameText before
        return v
    }

    internal class ViewHolder {
        var label: TextView? = null
        var data: TextView? = null
    }
}