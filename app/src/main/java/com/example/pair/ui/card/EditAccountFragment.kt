package com.example.pair.ui.card

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pair.databinding.EditaccountFragmentBinding
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pair.*
import java.io.File
import java.lang.Exception
import java.io.IOException
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.InputStream
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.pair.ui.main.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class EditAccountFragment : Fragment() {

    companion object {
        fun newInstance() = EditAccountFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: EditaccountFragmentBinding

    private lateinit var sharedPref: SharedPreferences
    val db = PairApplication.room!!
    val userDao = (db as AppDatabase).userDao()

    lateinit var account: Card
    var tempUri = ""

    // TODO: Consolidate this with PairFragment sister function
    private fun Context.hasPermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }
    private lateinit var storagePermResultLauncher : ActivityResultLauncher<String>
    private val isReadExternalStoragePermGranted get() = requireContext().hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditaccountFragmentBinding.inflate(inflater, container, false)
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)

        runBlocking { launch(Dispatchers.Default) {
            val uuid = sharedPref.getString("account_id", "")
            account = userDao.getCard(uuid!!)!!
        } }

        binding.toolbar.toolbarBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.toolbarTitle.text = "Edit Account Details"

        binding.editNameEdittext.hint = account.username

        binding.editImageButton.setOnClickListener {
            // open browse menu, return path
            if (!isReadExternalStoragePermGranted) {
                storagePermResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                selectImageFromGallery()
            }
        }

        val image_file = File(requireContext().filesDir, "account_image")
        if (image_file.exists()) {
            val uri = sharedPref.getString("account_image_uri", "")!!
            Glide.with(this)
                .load(image_file)
                .signature(ObjectKey(uri))
                .circleCrop()
                .into(binding.editImageImage)
        }

        binding.editApply.setOnClickListener {
            var new_name = account.username
            // TODO: Better name checking e.g. remove whitespace before and after
            if (binding.editNameEdittext.text.isNotEmpty()) {
                new_name = binding.editNameEdittext.text.toString()
            }

            val new_account = Card(account.id, new_name, account.created, account.card_count)

            var changed = false
            if (new_account != account) {
                Log.i("PAIR", "Updating account")
                runBlocking { launch(Dispatchers.Default) {
                    userDao.updateCard(new_account)
                } }
                changed = true
            }
            val storedUri = sharedPref.getString("account_image_uri", "")
            if (tempUri != "" && tempUri != storedUri) {
                val image_file = File(requireContext().filesDir, "account_image")
                if (!image_file.exists()) {
                    image_file.createNewFile()
                }
                val inputStream = requireActivity().contentResolver.openInputStream(Uri.parse(tempUri))
                copyInputStreamToFile(inputStream!!, image_file)
                sharedPref.edit().putString("account_image_uri", tempUri).commit()
                changed = true
            }
            if (changed) {
                toAccount()
            } else {
                showSnackBar("No changes to apply...")
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storagePermResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted : Boolean ->
            if (!isGranted) {
                // TODO: inform user they have denied enabling location access + consequences
            } else {
                // Granted go to image select
                selectImageFromGallery()
            }
        }
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Log.i("PAIR", "New image uri is: $uri")
            tempUri = uri.toString()
            Glide.with(this)
                .load(uri)
                .signature(ObjectKey(tempUri))
                .circleCrop()
                .into(binding.editImageImage)
        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private fun copyInputStreamToFile(`in`: InputStream, file: File) {
        var out: OutputStream? = null
        try {
            out = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
        } catch (e: Exception) {
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

    override fun onResume() {
        super.onResume()
        viewModel.showNavigation.value = false
    }

    private fun toAccount() {
        val action = EditAccountFragmentDirections.actionEditAccountToAccount()
        this.findNavController().navigate(action)
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(
            binding.root,
            text,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}