package com.example.aeye.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.aeye.ui.components.*
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Date class to hold hospital info
data class HospitalInfo(
    val location: LatLng,
    val name: String,
    val isWomenSpecific: Boolean
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HospitalScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Request configuration for high-accuracy location
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
    }

    // Handles Location permission + stores user and hospital locations
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hospitalList by remember { mutableStateOf(listOf<HospitalInfo>()) }

    // Created call to receive updated location and fetch nearby hospitals
    val locationCallback = rememberUpdatedState(
        newValue = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    userLocation = latLng
                    Log.d("LiveLocation", "Updated: $latLng")

                    fetchNearbyHospitals(latLng) { results ->
                        hospitalList = results
                    }
                }
            }
        }
    )

    // Default location if none is available
    val fallbackLocation = LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState()

    // Request location updates if permission is granted
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback.value,
                    context.mainLooper
                )
            }
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    // Clean up location updates when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback.value)
        }
    }

    // Center the camera on the user's location when updated
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 14f)
        }
    }

    // Scaffolds layout with top and bottom UI bars
    Scaffold(
        topBar = { AEyeTopBar(onSettingsClick = { /* nav later */ }) }
    ) {
        innerPadding ->
        Box(modifier = modifier.padding(innerPadding).fillMaxSize()) {
            // Google Map showing user and hospital markers
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Marker for user location
                Marker(
                    state = MarkerState(position = userLocation ?: fallbackLocation),
                    title = "You are here"
                )
                // Markers for each hospital, with custom color based on type
                hospitalList.forEach { hospital ->
                    Marker(
                        state = MarkerState(position = hospital.location),
                        title = hospital.name,
                        snippet = if (hospital.isWomenSpecific) "Women-Focused Hospital" else "General Hospital",
                        icon = if (hospital.isWomenSpecific)
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        else
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                    )
                }
            }
        }
    }
}

// Function to fetch specific hospitals from Google Places API and parse results
fun fetchNearbyHospitals(
    location: LatLng,
    onResult: (List<HospitalInfo>) -> Unit
) {
    val apiKey = "AIzaSyD18gM2n6Zjjd-lGOu7mOm88WQgOdQDGi8"
    val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
            "?location=${location.latitude},${location.longitude}" +
            "&radius=10000&type=hospital&key=$apiKey"

    // Launched a background coroutine for network operations on the IO dispatcher
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Initialize execution of HTTP requests to Google Places API
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            // Initialize parsing for API response and extract JSON array
            val bodyString = response.body?.string()
            val json = JSONObject(bodyString ?: "")
            val results = json.getJSONArray("results")

            val hospitals = mutableListOf<HospitalInfo>()

            Log.d("HospitalFetch", "Raw response: $bodyString")

            // Uses for loop to iterate through each API result for hospital names + details
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val nameRaw = item.getString("name")
                val name = nameRaw.lowercase()

                // Filter out cosmetic and aesthetic clinics
                val isCosmeticClinic = name.contains("cosmetic") ||
                        name.contains("plastic") ||
                        name.contains("laser") ||
                        name.contains("aesthetic")
                if (isCosmeticClinic) continue

                // Extracts latitude and longitude from hospital's geometry object
                val locationObj = item.getJSONObject("geometry").getJSONObject("location")
                val lat = locationObj.getDouble("lat")
                val lng = locationObj.getDouble("lng")

                // Specifies women-focused clinics/hospitals
                val isWomenSpecific = name.contains("women") ||
                        name.contains("obstetric") ||
                        name.contains("gynecol") ||
                        name.contains("maternity")

                hospitals.add(HospitalInfo(LatLng(lat, lng), nameRaw, isWomenSpecific))
            }

            Log.d("HospitalFetch", "Found ${hospitals.size} hospitals (women-specific highlighted)")

            // Returns results to UI on main thread
            withContext(Dispatchers.Main) {
                onResult(hospitals)
            }
        } catch (e: Exception) {
            // Logs error message for unsuccessful fetching of hospitals
            Log.e("HospitalFetch", "Error: ${e.message}")
        }
    }
}