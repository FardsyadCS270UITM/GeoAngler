package Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hemantpatel.mpfapp.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class weather extends Fragment {
    TextView cityTextView,cloudTextView,tempTextView,mintempTextView,maxtempTextview,updatedTextView,sunriseTextView,sunsetTextView,windTextView,pressureTextView,humidityTextView;
    EditText nameText;
    Button check;

    public weather() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.weather, container, false);
    }
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        cityTextView= (TextView) view.findViewById(R.id.cityTextView);
        cloudTextView= (TextView) view.findViewById(R.id.cloudTextview);
        tempTextView= (TextView) view.findViewById(R.id.tempTextView);
        mintempTextView= (TextView) view.findViewById(R.id.minTempTextView);
        maxtempTextview= (TextView) view.findViewById(R.id.maxTempTextView);
        updatedTextView= (TextView) view.findViewById(R.id.updatedTextView);
        sunriseTextView= (TextView) view.findViewById(R.id.sunriseTextView);
        sunsetTextView= (TextView) view.findViewById(R.id.sunsetTextVIew);
        windTextView= (TextView) view.findViewById(R.id.windTextView);
        pressureTextView= (TextView) view.findViewById(R.id.pressureTextView);
        humidityTextView= (TextView) view.findViewById(R.id.humidityTextView);
        nameText= (EditText) view.findViewById(R.id.nameText);
        check= (Button) view.findViewById(R.id.check);
            check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeather();
            }
        });
}

    public void getWeather() {
        DownloadTask task=new DownloadTask();
        task.execute("https://openweathermap.org/data/2.5/weather?q="+nameText.getText().toString()+"&appid=439d4b804bc8187953eb36d2a8c26a02");
    }

    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection connection = null;
            try {
                url=new URL(urls[0]);
                connection=(HttpURLConnection) url.openConnection();
                InputStream inputStream=connection.getInputStream();
                InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
                int data=inputStreamReader.read();
                while (data != -1){
                    char current=(char) data;
                    result += current;
                    data=inputStreamReader.read();
                }return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject=new JSONObject(s);
                JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
                JSONObject main=jsonObject.getJSONObject("main");
                JSONObject wind=jsonObject.getJSONObject("wind");
                JSONObject sys=jsonObject.getJSONObject("sys");
                Long updatedAt = jsonObject.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");
                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");
                String address = jsonObject.getString("name") + ", " + sys.getString("country");
                cityTextView.setText(address);
                updatedTextView.setText(updatedAtText);
                cloudTextView.setText(weatherDescription);
                tempTextView.setText(temp);
                mintempTextView.setText(tempMin);
                maxtempTextview.setText(tempMax);
                sunriseTextView.setText(String.valueOf(sunrise));
                sunsetTextView.setText(String.valueOf(sunset));
                windTextView.setText(String.valueOf(windSpeed));
                pressureTextView.setText(pressure);
                humidityTextView.setText(humidity);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    }
