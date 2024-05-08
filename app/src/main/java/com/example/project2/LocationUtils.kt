package com.example.project2

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import java.io.IOException
import java.util.*

class LocationUtils {

    companion object {

        fun getLocationInfoFromLatLng(context: Context, latLng: LatLng): String {
            val geocoder = Geocoder(context, Locale.getDefault())

            try {
                val addresses: MutableList<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (addresses!!.isNotEmpty()) {
                    val address: Address = addresses[0]

                    val name = if (address.featureName != null) address.featureName else ""
                    val addressLines = mutableListOf<String>()

                    // Fetch individual address lines
                    for (i in 0..address.maxAddressLineIndex) {
                        addressLines.add(address.getAddressLine(i))
                    }

                    // Concatenate address lines to get the complete address
                    val fullAddress = addressLines.joinToString(separator = "\n")

                    return "Name: $name\nAddress: $fullAddress\nLatitude: ${latLng.latitude}\nLongitude: ${latLng.longitude}"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error fetching location information", Toast.LENGTH_SHORT).show()
            }

            return "Location information not found"
        }
    }
}
