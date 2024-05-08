package com.example.project2


import PlacesAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import android.location.LocationManager
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_UPDATE_INTERVAL: Long = 10000
    private val LOCATION_UPDATE_FASTEST_INTERVAL: Long = 5000
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private var currentLocation: LatLng? = null
    private lateinit var locationManager: LocationManager

    private lateinit var placesClient: PlacesClient
    private lateinit var placesAdapter: PlacesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (isLocationPermissionGranted()){

            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager



            fetchCurrentLocation()

        }else{
            Toast.makeText(this,"Please allow location permission",Toast.LENGTH_SHORT).show()
            requestLocationPermission()
        }


        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(applicationContext, "AIzaSyBug8Xs-VJKHsUO_bqh7-HGE-160u8trJA")
        // Create a new PlacesClient instance
        placesClient = Places.createClient(this)

        placesAdapter = PlacesAdapter()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = placesAdapter




    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        if (currentLocation != null) {

            mMap.addMarker(MarkerOptions()
                .position(currentLocation!!)
                .title("Your Current Location"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15.0F))

            getCurrentPlace()

        } else {
            showToast("Current location not available.")
        }
    }



    private fun isLocationPermissionGranted(): Boolean {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION


        val permissionCheck = ContextCompat.checkSelfPermission(this, locationPermission)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }


    private fun requestLocationPermission() {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION


        if (!isLocationPermissionGranted()) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(locationPermission),
                LOCATION_PERMISSION_REQUEST_CODE
            )

        } else {

            fetchCurrentLocation()

        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Got last known location. In some rare situations, this can be null.
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        currentLocation = LatLng(latitude, longitude)
                        val mapFragment = supportFragmentManager
                            .findFragmentById(R.id.map) as SupportMapFragment
                        mapFragment.getMapAsync(this@MainActivity) // Use this@MainActivity to refer to the outer class
                        showToast("Latitude: $latitude, Longitude: $longitude")
                    } else {
                        //requestNewLocation()
                        showToast("Location is null. Try again.")
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Error getting location: ${e.message}")
                }
        } else {
            showToast("Location permission is not granted.")
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentPlace() {
        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)

        // Use FindCurrentPlaceRequest to get place information
        val request = FindCurrentPlaceRequest.newInstance(placeFields)



        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response: FindCurrentPlaceResponse ->
                val placesList = mutableListOf<Place>()
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place
                    val name = place.name
                    val address = place.address
                    val latLng = place.latLng

                    placesList.add(place)

                    // Display the result on the map or in a RecyclerView
                    Log.i("Places", "$name, $address, $latLng")
                }

                // Update the RecyclerView with the list of nearby places
                placesAdapter.updatePlacesList(placesList)
            }
            .addOnFailureListener { exception: Exception ->
                Log.e("Places", "Failed to get current place: ${exception.message}")
            }
    }




    private fun showToast(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // Check if the request was granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("Permission Granted")
                    fetchCurrentLocation()
                } else {
                    showToast("Please allow location permission from the app settings")

                }
            }

        }
    }


    fun onMenuButtonClick(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.main_menu, popup.menu)

        // Set a listener for menu item clicks
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuEmail -> {
                    // Handle the "Email" menu item click
                    onSendEmail()
                    true
                }
                R.id.menuAboutApp -> {
                    // Handle the "About App" menu item click
                    startActivity(Intent(this, AboutActivity::class.java))

                    true
                }
                else -> false
            }
        }

        popup.show()
    }


    fun onSendEmail() {

        if (currentLocation==null){
            showToast("Current location is null")
        }else {

            // Inside your activity or fragment
            val emailInputDialog = EmailInputDialog(this) { emailAddress ->
                // Handle the entered email address here
                if (isValidEmail(emailAddress)) {
                    // Do something with the valid email address
                    sendEmailWithLocationInfo(emailAddress)
                } else {
                    // Handle invalid email address
                    showToast("Invalid email address")
                }
            }

// Show the email input dialog
            emailInputDialog.showInputDialog()
        }

    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailRegex.matches(email)
    }


    private fun sendEmailWithLocationInfo(emailAddress: String) {
        val subject = "My Current Location Info"
        val locationInfo = LocationUtils.getLocationInfoFromLatLng(this, currentLocation!!)


        val intent = Intent(Intent.ACTION_SEND, Uri.parse("mailto:$emailAddress"))
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, locationInfo)

        try {
            // Always show the chooser, even if there's only one email app
            startActivity(Intent.createChooser(intent, "Choose Email Client..."))
        } catch (e: Exception) {
            // Handle any exceptions, e.g., no email client application
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SEND)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        // put recipient email in intent
        /* recipient is put as array because you may wanna send email to multiple emails
           so enter comma(,) separated emails, it will be stored in array*/
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, message)


        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        }
        catch (e: Exception){
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }


}
