package com.example.pair.ui.card

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.pair.*
import com.example.pair.databinding.AccountFragmentBinding
import com.example.pair.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class AccountFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: AccountFragmentBinding
    //TODO: use as gateway to show all user cards stored
    //val args: AccountFragmentArgs by navArgs()

    private lateinit var sharedPref: SharedPreferences
    val db = PairApplication.room!!
    val userDao = (db as AppDatabase).userDao()

    private lateinit var adapter: ContactInfoListAdapter
    private lateinit var account: Card

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AccountFragmentBinding.inflate(inflater, container, false)
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)

        binding.toolbar.toolbarBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.toolbarTitle.text = "Account"

        //val infoAdapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, myStringArray)
        adapter = ContactInfoListAdapter(requireContext())
        binding.listInfo.adapter = adapter

        binding.accountEdit.setOnClickListener {
            toEditAccount()
        }

        binding.buttonAddInfo.setOnClickListener {
            val newFragment = AddContactInfoDialog(adapter)
            newFragment.show(parentFragmentManager, "info")
        }

        binding.listInfo.setOnItemLongClickListener { adapterView, view, i, l ->
            val label = view.findViewById<TextView>(R.id.list_info_label).text
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, id ->
                            val selectedInfo = adapter.info[i]
                            runBlocking { launch(Dispatchers.Default) {
                                userDao.deleteCI(selectedInfo)
                            } }
                            adapter.info.removeAt(i)
                            adapter.notifyDataSetChanged()
                        })
                    setNegativeButton("No",
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                    setTitle("Delete $label?")
                }
                // Create the AlertDialog
                builder.show()
            }
            true
        }

        //val uuid = sharedPref.getString("account_id", "")
        runBlocking { launch(Dispatchers.Default) {
            //account = userDao.getCard(uuid!!)
            val storedInfo = userDao.getAllCI()
            adapter.info.addAll(storedInfo)
            adapter.notifyDataSetChanged()
        } }
        //binding.accountName.text = account.username

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // TODO: needed?
        viewModel.showNavigation.value = false

        runBlocking { launch(Dispatchers.Default) {
            val uuid = sharedPref.getString("account_id", "")
            // TODO is this really safe?
            account = userDao.getCard(uuid!!)
        } }

        binding.accountName.text = account.username

        val image_file = File(requireContext().filesDir, "account_image")
        val uri = sharedPref.getString("account_image_uri", "")!!
        if (image_file.exists()) {
            //Picasso.get().load(image_file).transform(CircleTransform()).into(binding.accountImage)
            Glide.with(this)
                .load(image_file)
                .signature(ObjectKey(uri))
                .circleCrop()
                .into(binding.accountImage)
        }

        // update image and name
        //if (args.accountChanged) {
        //    runBlocking { launch(Dispatchers.Default) {
        //        val uuid = sharedPref.getString("account_id", "")
        //        binding.accountName.text = account.username
        //    } }
        //}
    }

    override fun onPause() {
        super.onPause()
        viewModel.showNavigation.value = true
    }

    private fun toEditAccount() {
        val action = AccountFragmentDirections.actionAccountToEditAccountFragment()
        this.findNavController().navigate(action)
    }
}