package com.vaidoos.wificonnectapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.vaidoos.wificonnectapp.ui.theme.WifiConnectAppTheme


class MainActivity : ComponentActivity() {


    private val TAG = "MainActivity"


    private val ssid = "Sweet Home"   // Replace with your SSID
    private val password = "kichu#ekta@daw"  // Replace with your WiFi password

//    private val ssid = "Audra"   // Replace with your SSID
//    private val password = "1234567890"  // Replace with your WiFi password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary permissions at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
                ), 0
            )
        }

        setContent {
            WifiConnectAppTheme {
                // Main Content
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val clipboardManager = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val wifiInstaller = WifiInstaller(wifiManager, clipboardManager)
                MainScreen(onConnectClick = {
//                    connectToWifi(ssid, password)
                    connectToWifiGoogleIO(wifiInstaller)
//                    addWifiNetworkSuggestions()
                })
            }
        }

        // Optional: Register the BroadcastReceiver to listen for post connection broadcasts
        registerPostConnectionReceiver()
    }

    private fun connectToWifiGoogleIO(wifiInstaller: WifiInstaller?) {
        Log.d(TAG, "connectToWifiGoogleIO: ")
        wifiInstaller?.installConferenceWifi(
            WifiConfiguration().apply {
                SSID = ssid
                preSharedKey = password
            }
        )
    }

    private fun registerPostConnectionReceiver() {
        val intentFilter = IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION) {
                    // Handle post connection processing here
                }
            }
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if location permissions are granted
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if not granted
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return false
            }
        }
        return true
    }


    private fun addWifiNetworkSuggestions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val suggestion1 = WifiNetworkSuggestion.Builder()
                .setSsid("Hello")
                .setWpa2Passphrase("World")
                .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                .build()

            val suggestionsList = listOf(suggestion1)

            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val status = wifiManager.addNetworkSuggestions(suggestionsList)

            if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                // Handle error here
                Log.e(TAG, "Failed to add network suggestions")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, continue with the Wi-Fi connection process
                connectToWifi(ssid, password)
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to access Wi-Fi networks",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    // Function to connect to Wi-Fi using SSID and password
    @Suppress("DEPRECATION")
    private fun connectToWifi(ssid: String, password: String) {
        // Check if permissions are granted
        if (!checkPermissions()) return

        // Check if location services are enabled
        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Please enable location services to scan Wi-Fi networks",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // For Android Q and above, use WifiNetworkSpecifier
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "connectToWifi: over Q ")
//            WifiUtils.withContext(applicationContext)
//                .connectWith(ssid, password)
//                .setTimeout(40000)
//                .onConnectionResult(object : ConnectionSuccessListener {
//                    override fun success() {
//                        Toast.makeText(this@MainActivity, "SUCCESS!", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun failed(errorCode: ConnectionErrorCode) {
//                        Toast.makeText(
//                            this@MainActivity,
//                            "EPIC FAIL!$errorCode",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                })
//                .start()
            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()

            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                .addCapability(NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()

            val connectivityManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                    Toast.makeText(applicationContext, "Connected to $ssid", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Toast.makeText(
                        applicationContext,
                        "Failed to connect to $ssid",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            connectivityManager.requestNetwork(networkRequest, networkCallback)

            val network: Network? = connectivityManager.getActiveNetwork();
            val capabilities: NetworkCapabilities? = connectivityManager
                .getNetworkCapabilities(network);

            if (capabilities != null) {
                Log.d(TAG, "connectToWifi: capability available")
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )
                ) {

                    Toast.makeText(
                        this@MainActivity,
                        "Network is Available",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d(
                        TAG,
                        "connectToWifi: NET_CAPABILITY_INTERNET or NET_CAPABILITY_VALIDATED not available"
                    )
                }
            } else {
                Log.d(TAG, "connectToWifi: no capability available")
            }
//            connectivityManager.getNetworkCapabilities()
        } else {
            Log.d(TAG, "connectToWifi: below Q ")
            // For below Android Q, use WifiManager
            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
            }

            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"" + ssid + "\""
                preSharedKey = "\"" + password + "\""
            }

            // Check if the network already exists and update it, otherwise add a new network
            val existingConfig = wifiManager.configuredNetworks.find { it.SSID == "\"" + ssid + "\"" }

            val netId = if (existingConfig != null) {
                wifiManager.updateNetwork(existingConfig)
            } else {
                wifiManager.addNetwork(wifiConfig)
            }

            if (netId == -1) {
                Toast.makeText(
                    applicationContext,
                    "Failed to configure the network",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()
                Toast.makeText(applicationContext, "Connected to $ssid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MainScreen(onConnectClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = onConnectClick,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Connect to Wi-Fi")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WifiConnectAppTheme {
        MainScreen(onConnectClick = {})
    }
}
