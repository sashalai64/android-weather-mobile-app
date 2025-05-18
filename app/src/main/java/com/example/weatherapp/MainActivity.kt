package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.weatherapp.model.Weather.CurrentWeatherResponse
import com.example.weatherapp.BuildConfig
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices



class MainActivity : AppCompatActivity() {

    // Declare necessary variables
    private lateinit var cityEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var progressBar: ProgressBar

    // Main containers
    private lateinit var weatherContainer: ConstraintLayout
    private lateinit var cityNotFoundContainer: ConstraintLayout
    private lateinit var noConnectionContainer: ConstraintLayout
    private lateinit var cityBlankContainer: ConstraintLayout

    // Weather information TextViews
    private lateinit var cityTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var conditionTextView: TextView
    private lateinit var highTempTextView: TextView
    private lateinit var lowTempTextView: TextView
    private lateinit var sunriseTextView: TextView
    private lateinit var sunsetTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var humidityTextView: TextView

    val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
    private val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var weather: CurrentWeatherResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Call the superclass implementation
        enableEdgeToEdge()
        // Set the content view for this activity, specifying the layout resource to be used
        setContentView(R.layout.activity_main)

        // Initialize the fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the view components by finding it by its ID in the layout
        initializeViews()

        // Initially hide all containers except the search bar
        weatherContainer.visibility = View.GONE
        cityNotFoundContainer.visibility = View.GONE
        noConnectionContainer.visibility = View.GONE
        cityBlankContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        // Set an OnClickListener on the search button
        searchButton.setOnClickListener {
            // Retrieve the city name from the EditText
            val cityName = cityEditText.text.toString().trim()

            // Dismiss the keyboard so it doesn't cover the UI
            dismissKeyboard()

            // Check if the city name is not empty
            if (cityName.isNotEmpty()) {
                showLoading()

                if (isNetworkAvailable()) {
                    // Build the URL for fetching weather data using the city name
                    val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$apiKey"

                    // Fetch weather data from the constructed URL in a separate thread
                    fetchWeatherData(url).start()
                } else {
                    // Show error if no internet connection
                    updateErrorUI("Please connect to internet")
                }

            } else {
                // Update the UI to show an error message if no city name is entered
                updateErrorUI("City name cannot be blank")
            }
        }

        // Get weather for current location as default
        if (isNetworkAvailable()) {
            showLoading()
            checkLocationPermission()
        } else {
            updateErrorUI("Please connect to internet")
        }
    }

    private fun initializeViews() {
        // Search components
        cityEditText = findViewById(R.id.cityEditText)
        searchButton = findViewById(R.id.searchButton)
        progressBar = findViewById(R.id.progressBar)

        // Main containers
        weatherContainer = findViewById(R.id.weatherContainer)
        cityNotFoundContainer = findViewById(R.id.cityNotFoundContainer)
        noConnectionContainer = findViewById(R.id.noConnectionContainer)
        cityBlankContainer = findViewById(R.id.cityBlankContainer)

        // Weather information TextViews
        cityTextView = findViewById(R.id.cityTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        conditionTextView = findViewById(R.id.conditionTextView)
        highTempTextView = findViewById(R.id.highTempTextView)
        lowTempTextView = findViewById(R.id.lowTempTextView)
        sunriseTextView = findViewById(R.id.sunriseTextView)
        sunsetTextView = findViewById(R.id.sunsetTextView)
        windTextView = findViewById(R.id.windTextView)
        pressureTextView = findViewById(R.id.pressureTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
    }

    private fun dismissKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /**
     * Shows the loading indicator and hides all other containers.
     */
    private fun showLoading() {
        weatherContainer.visibility = View.GONE
        cityNotFoundContainer.visibility = View.GONE
        noConnectionContainer.visibility = View.GONE
        cityBlankContainer.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Checks for location permission and fetches weather data if granted
     */
    private fun checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocationAndFetchWeather()
        }
    }

    /**
    * Handles the result of the location permission request
    */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocationAndFetchWeather()
        } else {
            // If permission is denied, fallback to Seattle
            val defaultUrl = "$BASE_URL?q=Seattle&units=metric&appid=$apiKey"
            fetchWeatherData(defaultUrl).start()
        }
    }

    /**
     * Gets the current location and fetches weather data for that location
     */
    @SuppressLint("MissingPermission") // You already check permission before calling this
    private fun getCurrentLocationAndFetchWeather() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val url = "$BASE_URL?lat=$lat&lon=$lon&units=metric&appid=$apiKey"
                    fetchWeatherData(url).start()
                } else {
                    // fallback if location is null
                    val defaultUrl = "$BASE_URL?q=Seattle&units=metric&appid=$apiKey"
                    fetchWeatherData(defaultUrl).start()
                }
            }
            .addOnFailureListener {
                val defaultUrl = "$BASE_URL?q=Seattle&units=metric&appid=$apiKey"
                fetchWeatherData(defaultUrl).start()
            }
    }

    /**
     * Fetches weather data from a specified URL and processes the response.
     *
     * @param urlString The URL string from which the weather data is to be fetched.
     * @return A thread that, when started, performs the network request and data processing.
     */
    fun fetchWeatherData(urlString: String): Thread {
        // Create and return a new Thread object to handle the network operation
        return Thread {
            try {
                // Create a Gson object for JSON parsing
                val gson = Gson()

                // Initialize a URL object from the string URL provided
                val url = URL(urlString)

                // Open a URL connection and cast it to HttpURLConnection
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                // Check if the response from the connection is successful (HTTP 200 OK)
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    // Logging success message with the retrieved data
                    Log.d("Weather App", "Successfully fetched data")

                    // If successful, read the stream from the connection
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))

                    // Read the data
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    // Close the BufferedReader
                    reader.close()

                    // Parse the JSON response into a CurrentWeatherResponse object using Gson
                    val weatherResponse = gson.fromJson(response.toString(), CurrentWeatherResponse::class.java)

                    // Update the global weather object
                    weather = weatherResponse

                    // Update the UI to reflect the fetched data
                    updateWeatherUI(weatherResponse)

                } else {
                    // If connection failed, log the failure and response code
                    Log.d("Weather App", "Failed to fetch data: ${connection.responseCode}")

                    // Update the UI to show an error message
                    when (connection.responseCode) {
                        HttpURLConnection.HTTP_NOT_FOUND -> updateErrorUI("City not found. Please enter a valid city name.")
                        else -> updateErrorUI("Error: ${connection.responseCode}")
                    }
                }
            } catch (e: Exception) {
                Log.e("Weather App", "Error: ${e.message}")
                runOnUiThread {
                    updateErrorUI("Please connect to internet")
                }
            }
        }
    }

    /**
     * Checks if the device is connected to the internet.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    /**
     * Updates the error UI based on the error message.
     */
    private fun updateErrorUI(message: String) {
        runOnUiThread {
            progressBar.visibility = View.GONE
            weatherContainer.visibility = View.GONE

            // Hide all error containers first
            cityNotFoundContainer.visibility = View.GONE
            noConnectionContainer.visibility = View.GONE
            cityBlankContainer.visibility = View.GONE

            // Show the appropriate error container based on the message
            when (message) {
                "Please connect to internet" -> noConnectionContainer.visibility = View.VISIBLE
                "City name cannot be blank" -> cityBlankContainer.visibility = View.VISIBLE
                else -> cityNotFoundContainer.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Updates the UI with the fetched weather data.
     */
    private fun updateWeatherUI(data: CurrentWeatherResponse) {
        runOnUiThread {
            // Hide progress bar and error containers
            progressBar.visibility = View.GONE
            cityNotFoundContainer.visibility = View.GONE
            noConnectionContainer.visibility = View.GONE
            cityBlankContainer.visibility = View.GONE

            // Show weather container
            weatherContainer.visibility = View.VISIBLE

            // Update weather information
            cityTextView.text = data.name

            // Display temperature
            temperatureTextView.text = "${data.main.temp.toInt()}${getString(R.string.temperature_unit)}"

            // Display weather condition
            conditionTextView.text = data.weather[0].main

            // Format high and low temperatures to match design
            val highTemp = data.main.temp_max.toInt()
            val lowTemp = data.main.temp_min.toInt()
            highTempTextView.text = "H:${highTemp}${getString(R.string.temperature_unit)}"
            lowTempTextView.text = "L:${lowTemp}${getString(R.string.temperature_unit)}"


            // Format sunrise and sunset times
            sunriseTextView.text = convertUnixToTime(data.sys.sunrise)
            sunsetTextView.text = convertUnixToTime(data.sys.sunset)

            // Display other weather details
            windTextView.text = "${data.wind.speed} km/h"
            pressureTextView.text = "${data.main.pressure}hPa"
            humidityTextView.text = "${data.main.humidity}%"
        }
    }

    /**
     * Converts a Unix timestamp to a formatted time string.
     */
    private fun convertUnixToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
}