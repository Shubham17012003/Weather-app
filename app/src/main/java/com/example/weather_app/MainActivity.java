package com.example.weather_app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONObject;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCity;
    private Button buttonGetWeather;
    private TextView textViewWeatherDescription, textViewTemperature, textViewHumidity, textViewPressure, textViewWindSpeed;
    private ProgressBar progressBar;
    private CardView cardWeatherInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        editTextCity = findViewById(R.id.editTextCity);
        buttonGetWeather = findViewById(R.id.buttonGetWeather);
        textViewWeatherDescription = findViewById(R.id.textViewWeatherDescription);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewHumidity = findViewById(R.id.textViewHumidity);  // New TextView for humidity
        textViewPressure = findViewById(R.id.textViewPressure);  // New TextView for pressure
        textViewWindSpeed = findViewById(R.id.textViewWindSpeed); // New TextView for wind speed
        progressBar = findViewById(R.id.progressBar);
        cardWeatherInfo = findViewById(R.id.cardWeatherInfo);

        buttonGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = editTextCity.getText().toString().trim();
                if (!city.isEmpty()) {
                    new FetchWeatherTask().execute(city);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE); // Show progress bar
            cardWeatherInfo.setVisibility(View.GONE); // Hide weather info card
        }

        @Override
        protected String doInBackground(String... params) {
            String city = params[0];
            String result = "";
            try {
                String apiKey = "80a1f0ee71f36861176569723772874a"; // Replace with your OpenWeatherMap API key
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());

                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE); // Hide progress bar

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject main = jsonObject.getJSONObject("main");
                    double temp = main.getDouble("temp");
                    double humidity = main.getDouble("humidity");
                    double pressure = main.getDouble("pressure");

                    JSONObject wind = jsonObject.getJSONObject("wind");
                    double windSpeed = wind.getDouble("speed");

                    String weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                    // Set the extracted data into the TextViews
                    textViewTemperature.setText(String.format("Temperature: %.2f°C", temp));
                    textViewHumidity.setText(String.format("Humidity: %.0f%%", humidity));
                    textViewPressure.setText(String.format("Pressure: %.0f hPa", pressure));
                    textViewWindSpeed.setText(String.format("Wind Speed: %.2f m/s", windSpeed));
                    textViewWeatherDescription.setText(weatherDescription);

                    cardWeatherInfo.setVisibility(View.VISIBLE); // Show weather info card
                } catch (Exception e) {
                    e.printStackTrace();
                    textViewWeatherDescription.setText("Error fetching weather data.");
                    cardWeatherInfo.setVisibility(View.VISIBLE); // Show error
                }
            } else {
                textViewWeatherDescription.setText("Error fetching weather data.");
                cardWeatherInfo.setVisibility(View.VISIBLE); // Show error
            }
        }
    }
}
