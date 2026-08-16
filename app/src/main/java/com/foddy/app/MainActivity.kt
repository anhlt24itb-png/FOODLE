package com.foddy.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.foddy.app.data.util.NetworkMonitor
import com.foddy.app.data.util.SeedDatabase
import com.foddy.app.presentation.ui.MainScreen
import com.foddy.app.presentation.ui.components.NetworkStatusBar
import com.foddy.app.presentation.ui.theme.FoddyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var seedDatabase: SeedDatabase

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        askNotificationPermission()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedDatabase.seedIfNeeded()
                com.foddy.app.util.SeedData.seedDatabase()
            } catch (e: Exception) {
                com.foddy.app.util.AppLogger.e(e, "Error seeding database in MainActivity")
            }
        }

        enableEdgeToEdge()
        setContent {
            val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
            
            FoddyTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                    NetworkStatusBar(isOnline = isOnline)
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
