package com.example.weatherapp.model.Weather

import com.google.gson.annotations.SerializedName

//TODO
// Create data class WeatherItem (Refer to API Response)
/*
"weather": [
  {
    "id": 800,
    "main": "Clear",
    "description": "clear sky",
    "icon": "01d"
  }
]
 */

data class WeatherItem(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("main")
    val main: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("icon")
    val icon: String = ""
)