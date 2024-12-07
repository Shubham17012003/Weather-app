package com.example.weather_application;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Declare UI elements
    EditText cityName;
    Button Search;
    TextView show;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Link the XML layout

        // Initialize UI elements with correct IDs from the XML layout
        cityName = findViewById(R.id.editTextCity);  // Corrected ID here
        Search = findViewById(R.id.buttonGetWeather);  // Corrected ID here
        show = findViewById(R.id.weather);

        // Set the click listener for the Search button
        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the city name from the EditText
                String city = cityName.getText().toString().trim();

                // Check if the city name is not empty
                if (!city.isEmpty()) {
                    if (city.matches("[a-zA-Z\\s]+")) {  // Check if city name contains only letters and spaces
                        // Construct the URL for the API request
                        url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=9965beda84f13ebea42347aa663f80c7";

                        // Create and execute the AsyncTask to get the weather data
                        getWeather task = new getWeather();
                        task.execute(url);
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a valid city name", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Enter a valid city name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // AsyncTask to fetch weather data
    class getWeather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Open the connection to the API
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Check the response code
                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return "Error: Unable to fetch weather data. Response code: " + responseCode;
                }

                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: Unable to fetch weather data. Please check your internet connection.";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Check if result is empty or contains error message
            if (result == null || result.isEmpty()) {
                show.setText("Error: Unable to fetch weather data.");
                return;
            }

            try {
                // Parse the JSON result to extract weather details
                JSONObject jsonObject = new JSONObject(result);

                // Check if the API returns a valid response
                if (jsonObject.has("main")) {
                    JSONObject main = jsonObject.getJSONObject("main");

                    // Extract temperature in Kelvin and convert to Celsius
                    double tempInKelvin = main.getDouble("temp");
                    double tempInCelsius = tempInKelvin - 273.15;

                    // Extract other weather details
                    double tempMinInCelsius = main.getDouble("temp_min") - 273.15;
                    double tempMaxInCelsius = main.getDouble("temp_max") - 273.15;
                    double feelsLikeInCelsius = main.getDouble("feels_like") - 273.15;

                    // Build the weather information string
                    String weatherInfo = "Temperature: " + String.format("%.2f", tempInCelsius) + "°C\n" +
                            "Feels Like: " + String.format("%.2f", feelsLikeInCelsius) + "°C\n" +
                            "Min Temperature: " + String.format("%.2f", tempMinInCelsius) + "°C\n" +
                            "Max Temperature: " + String.format("%.2f", tempMaxInCelsius) + "°C\n";

                    // Additional weather details
                    weatherInfo += "Pressure: " + main.getInt("pressure") + " hPa\n";
                    weatherInfo += "Humidity: " + main.getInt("humidity") + " %\n";

                    // Set the weather information to the TextView
                    show.setText(weatherInfo);
                } else {
                    show.setText("Error: Invalid city name or no data available.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                show.setText("Error: Failed to parse weather data.");
            }
        }
    }
}
