package com.example.weatherapp.model.Weather

import com.google.gson.annotations.SerializedName

//TODO
// Create data class Coord (Refer to API Response)

/*
{
  "coord": {
    "lon": 139.6917,
    "lat": 35.6895
  },
  // other fields...
}
*/

data class Coord(

    @SerializedName("lon")
    val lon: Double = 0.0,

    @SerializedName("lat")
    val lat: Double = 0.0
)


