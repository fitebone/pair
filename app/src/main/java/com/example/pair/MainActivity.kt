package com.example.pair

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pair.data.PrefRepo
import com.example.pair.databinding.MainActivityBinding
import com.example.pair.ui.main.*

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Preference Repo
        PrefRepo.init(this)

        binding = MainActivityBinding.inflate(layoutInflater)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // TODO determine why nav bar has some lag
        binding.bottomNavBar.setupWithNavController(navController)

        //setSupportActionBar(appbar.root as Toolbar)
        //supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.actionbar.appbarAccount.setOnClickListener {
            //val action = PeersFragmentDirections.actionPeersFragmentToSwapFragment(id)
            findNavController(R.id.nav_host_fragment).navigate(R.id.account)
        }

        // Global visibility to peers
        //appbar.appbarVisibility.setOnClickListener {
        //    if (viewModel.visibleToPeers.value == true) {
        //        viewModel.visibleToPeers.value = false
        //        appbar.appbarVisibility.icon = AppCompatResources.getDrawable(this, R.drawable.ic_invisible)
        //    } else {
        //        viewModel.visibleToPeers.value = true
        //        appbar.appbarVisibility.icon = AppCompatResources.getDrawable(this, R.drawable.ic_visible)
        //    }
        //}

        // Show appbar when viewmodel var is set
        val navObserver = Observer<Boolean> {
            when (it) {
                true -> {
                    binding.bottomNavBar.visibility = View.VISIBLE
                    binding.actionbar.toolbar.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavBar.visibility = View.GONE
                    binding.actionbar.toolbar.visibility = View.GONE
                }
            }
        }
        viewModel.showNavigation.observe(this, navObserver)

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        when (findNavController(R.id.nav_host_fragment).currentDestination?.id) {
            // All navbar destinations
            R.id.swap -> viewModel.showNavigation.value = true
            R.id.cards -> viewModel.showNavigation.value = true
            R.id.drop -> viewModel.showNavigation.value = true
            else -> {
                //viewModel.showNavigation.value = false
            }
        }
    }
}