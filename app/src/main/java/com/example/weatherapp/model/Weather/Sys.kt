package com.example.weatherapp.model.Weather

import com.google.gson.annotations.SerializedName


//TODO
// Create data class Sys (Refer to API Response)
/*
{
  "sys": {
    "country": "US",
    "sunrise": 1621234567,
    "sunset": 1621287654
  }
}
 */

data class Sys(

    @SerializedName("country")
    val country: String = "",

    @SerializedName("sunrise")
    val sunrise: Long = 0L,

    @SerializedName("sunset")
    val sunset: Long = 0L
)
