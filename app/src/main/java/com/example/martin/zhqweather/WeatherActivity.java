package com.example.martin.zhqweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;

/**
 * @author zhq
 */
public class WeatherActivity extends AppCompatActivity {

    private ScrollView scvWeatherLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
    }
}
