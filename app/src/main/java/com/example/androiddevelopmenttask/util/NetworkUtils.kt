package com.example.androiddevelopmenttask.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

object NetworkUtils {
    private const val TAG = "NetworkUtils"

    /**
     * Check if the device has an active internet connection
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Log network information for debugging
     */
    fun logNetworkInfo(context: Context) {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val capabilities = if (network != null) {
                    connectivityManager.getNetworkCapabilities(network)
                } else null
                
                Log.d(TAG, "Network available: ${network != null}")
                Log.d(TAG, "Network capabilities: $capabilities")
                
                if (capabilities != null) {
                    Log.d(TAG, "Has internet: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
                    Log.d(TAG, "Has validated: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}")
                    Log.d(TAG, "Is WiFi: ${capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}")
                    Log.d(TAG, "Is Cellular: ${capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
                }
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                Log.d(TAG, "Network info: $networkInfo, Connected: ${networkInfo?.isConnected}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network: ${e.message}")
        }
    }
}
