package com.example.weatherapp.model.Weather

import com.google.gson.annotations.SerializedName

//TODO
// Create data class CurrentWeatherResponse (Refer to API Response)

/*
{
  "coord": {
    "lon": -122.08,
    "lat": 37.39
  },
  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "clear sky",
      "icon": "01d"
    }
  ],
  "base": "stations",
  "main": {
    "temp": 282.55,
    "feels_like": 281.86,
    "temp_min": 280.37,
    "temp_max": 284.26,
    "pressure": 1023,
    "humidity": 100
  },
  "visibility": 16093,
  "wind": {
    "speed": 1.5,
    "deg": 350
  },
  "rain": {
    "1h": 0.51
  },
  "clouds": {
    "all": 1
  },
  "dt": 1560350645,
  "sys": {
    "type": 1,
    "id": 5122,
    "country": "US",
    "sunrise": 1560343627,
    "sunset": 1560396563
  },
  "timezone": -25200,
  "id": 420006353,
  "name": "Mountain View",
  "cod": 200
}
 */

data class CurrentWeatherResponse(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("main")
    val main: Main,

    @SerializedName("weather")
    val weather: List<WeatherItem>,

    @SerializedName("wind")
    val wind: Wind,

    @SerializedName("sys")
    val sys: Sys
)