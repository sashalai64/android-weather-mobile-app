package com.example.weatherapp.model.Weather

import com.google.gson.annotations.SerializedName

//TODO
// Create data class Main (Refer to API Response)
/*
{
  "main": {
    "temp": 22.15,
    "feels_like": 21.32,
    "temp_min": 19.44,
    "temp_max": 23.33,
    "pressure": 1016,
    "humidity": 38
  },
  // other fields...
}
 */

data class Main(

    @SerializedName("temp")
    val temp: Double = 0.0,

    @SerializedName("feels_like")
    val feelsLike: Double = 0.0,

    @SerializedName("temp_min")
    val temp_min: Double = 0.0,

    @SerializedName("temp_max")
    val temp_max: Double = 0.0,

    @SerializedName("pressure")
    val pressure: Int = 0,

    @SerializedName("humidity")
    val humidity: Int = 0
)