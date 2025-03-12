package com.example.pawfectmatchapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.*
import com.google.android.material.button.MaterialButton

class PlacesDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var placesClient: PlacesClient
    private lateinit var tvName: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvWebsite: TextView
    private lateinit var btnBack: MaterialButton
    private lateinit var mapView: MapView
    private var placeLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_details)

        // Get API key from AndroidManifest.xml directly
        val applicationInfo = packageManager.getApplicationInfo(packageName, android.content.pm.PackageManager.GET_META_DATA)
        val apiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY")

        Log.d("PlacesAPI", "Loaded API Key: $apiKey") // Print API Key to log

        // Check if API key is empty or null
        if (apiKey.isNullOrEmpty()) {
            Log.e("PlacesAPI", "Error: API Key is missing or empty!")
        }

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey!!)
        }

        placesClient = Places.createClient(this)

        // Connect XML elements
        tvName = findViewById(R.id.tvName)
        tvAddress = findViewById(R.id.tvAddress)
        tvPhone = findViewById(R.id.tvPhone)
        tvWebsite = findViewById(R.id.tvWebsite)
        btnBack = findViewById(R.id.btnBack)
        mapView = findViewById(R.id.mapView)

        // Retrieve Place ID from intent
        val placeId = intent.getStringExtra("PLACE_ID") ?: "ChIJc9YSFYxc-hQRYIrEUpKSg_Y"

        // Fetch place details
        fetchPlaceDetails(placeId)

        // Initialize map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Back button functionality
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.WEBSITE_URI,
            Place.Field.LAT_LNG // Include location for the map
        )

        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                tvName.text = place.name ?: "No Name Available"
                tvAddress.text = place.address ?: "No Address Available"
                tvPhone.text = place.phoneNumber ?: "No Phone Available"
                tvWebsite.text = place.websiteUri?.toString() ?: "No Website Available"

                // Store the location of the place
                placeLatLng = place.latLng

                // Reload the map with the location
                if (placeLatLng != null) {
                    Log.d("PlacesAPI", "Fetched LatLng: ${placeLatLng!!.latitude}, ${placeLatLng!!.longitude}")
                    mapView.getMapAsync(this)
                } else {
                    Log.e("PlacesAPI", "LatLng is null!")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesAPI", "Error fetching place details: ${exception.message}")
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        placeLatLng?.let { latLng ->
            googleMap.clear()
            val markerOptions = MarkerOptions().position(latLng).title("Shelter Location")
            googleMap.addMarker(markerOptions)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}
