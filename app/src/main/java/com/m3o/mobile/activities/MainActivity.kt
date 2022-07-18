package com.m3o.mobile.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.m3o.mobile.R
import com.m3o.mobile.api.LoginService
import com.m3o.mobile.api.Networking
import com.m3o.mobile.databinding.ActivityMainBinding
import com.m3o.mobile.utils.REFRESH_TOKEN
import com.m3o.mobile.utils.SKIP_REFRESH
import com.m3o.mobile.utils.Safe
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var skipRefreshAndClickListener = false
        if (Safe.getAndDecryptApiKey(applicationContext).isEmpty()) {
            skipRefreshAndClickListener = true
            finish()
            startActivity(Intent(applicationContext, StartActivity::class.java))
        }

        val navView: BottomNavigationView = binding.navView
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.ServicesFragment, R.id.AccountFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (!skipRefreshAndClickListener) {
            binding.fab.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/vhBuhvYJtG"))
                startActivity(intent)
            }

            if (!intent.getBooleanExtra(SKIP_REFRESH, false)) {
                Networking.initializeAuth(Safe.getAndDecryptAccessToken(applicationContext))
                lifecycleScope.launch {
                    try {
                        LoginService.refresh(Safe.getKey(applicationContext, REFRESH_TOKEN))
                        Log.d("M3O-Mobile", "Access token refreshed")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("M3O-Mobile", "Refreshing access token failed")
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}