package com.unero.pinar

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unero.pinar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val bottomView: BottomNavigationView by lazy { binding.bottomView }
    private val navController: NavController by lazy {
        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()
    }

    private fun setupBottomNav(){
        bottomView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> bottomView.visibility = View.VISIBLE
                R.id.mapsFragment -> bottomView.visibility = View.VISIBLE
                else -> bottomView.visibility = View.GONE
            }
        }
    }
}