package com.example.martin.zhqweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.martin.zhqweather.gson.Forecast;
import com.example.martin.zhqweather.gson.Weather;
import com.example.martin.zhqweather.service.AutoUpdateService;
import com.example.martin.zhqweather.util.HttpUtil;
import com.example.martin.zhqweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author zhq
 */
public class WeatherActivity extends AppCompatActivity {

    public static final String KEY_WEATHER = "weather";

    public static final String KEY_BING_PIC = "bing_pic";

    public static final String KEY_OK = "ok";

    public static final String BUNDLE_WEATHER_ID = "weather_id";

    private static final String TIP = "获取天气信息失败";

    private static final String UNIT = "°C";

    private static final String COMFORT_TIP = "舒适度：";

    private static final String CAR_WASH_TIP = "洗车指数：";

    private static final String SPORT_TIP = "运动建议：";

    private static final int SDK_INT = 21;

    public DrawerLayout drawerLayout;

    public SwipeRefreshLayout swipeRefresh;

    private ScrollView scvWeatherLayout;

    private TextView tvTitleCity;

    private TextView tvTitleUpdateTime;

    private TextView tvDegree;

    private TextView tvWeatherInfo;

    private LinearLayout llForecast;

    private TextView tvAqi;

    private TextView tvPm25;

    private TextView tvComfort;

    private TextView tvCarWash;

    private TextView tvSport;

    private ImageView ivBingPic;

    private Button btnNav;

    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断是否显示状态栏  sdk大于21，即android系统为5.0之上的系统才会执行
        if (Build.VERSION.SDK_INT >= SDK_INT) {
            View decorView = getWindow().getDecorView();
            //活动的布局会显示在状态栏上面
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //将状态栏设置成透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化控件
        initViews();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString(KEY_WEATHER, null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气数据
            mWeatherId = getIntent().getStringExtra(BUNDLE_WEATHER_ID);
            scvWeatherLayout.setVisibility(View.GONE);
            requestWeather(mWeatherId);
        }
        //从缓存中取出背景图片路径
        String bingPic = prefs.getString(KEY_BING_PIC, null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(ivBingPic);
        } else {
            loadBingPic();
        }

    }

    private void initViews() {
        scvWeatherLayout = findViewById(R.id.scv_weather_layout);
        tvTitleCity = findViewById(R.id.tv_title_city);
        tvTitleUpdateTime = findViewById(R.id.tv_title_update_time);
        tvDegree = findViewById(R.id.tv_degree);
        tvWeatherInfo = findViewById(R.id.tv_weather_info);
        llForecast = findViewById(R.id.ll_forecast);
        tvAqi = findViewById(R.id.tv_aqi);
        tvPm25 = findViewById(R.id.tv_pm25);
        tvComfort = findViewById(R.id.tv_comfort);
        tvCarWash = findViewById(R.id.tv_car_wash);
        tvSport = findViewById(R.id.tv_sport);
        ivBingPic = findViewById(R.id.iv_bing_pic);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        btnNav = findViewById(R.id.btn_nav);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        //刷新监听器
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新就重新请求服务器
                requestWeather(mWeatherId);
            }
        });
        //点击btnNav弹出左侧滑动菜单
        btnNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    /**
     * 根据天气id请求城市信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=ae1f8557e6e64cb982f17360d974dc3c";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            /**
             * 请求失败，直接提示
             * @param call
             * @param e
             */
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, TIP, Toast.LENGTH_SHORT).show();
                        //结束刷新
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            /**
             * 请求成功，则解析数据并保存到缓存中。
             * @param call
             * @param response
             * @throws IOException
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && KEY_OK.equals(weather.status)) {
                            //将weather保存到缓存中
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString(KEY_WEATHER, responseText);
                            editor.apply();
                            //将weather展示到ui上
                            showWeatherInfo(weather);
                            //将当前weatherId保留，因为该界面可能是左滑选择的城市，之前的处理刷新后还是
                            // 会回到上一个城市
                            mWeatherId = weatherId;
                        } else {
                            Toast.makeText(WeatherActivity.this, TIP, Toast.LENGTH_SHORT).show();
                        }
                        //结束刷新
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        //每次请求天气信息的同时刷新背景图片
        loadBingPic();
    }

    /**
     * 处理数据，并展示到ui
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;

        String updateTime = weather.basic.update.updateTime.split(" ")[1];

        String degree = weather.now.temperature + UNIT;

        String weatherInfo = weather.now.more.info;

        tvTitleCity.setText(cityName);
        tvTitleUpdateTime.setText(updateTime);
        tvDegree.setText(degree);
        tvWeatherInfo.setText(weatherInfo);
        llForecast.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, llForecast, false);
            TextView tvDate = view.findViewById(R.id.tv_date);
            TextView tvInfo = view.findViewById(R.id.tv_info);
            TextView tvMax = view.findViewById(R.id.tv_max);
            TextView tvMin = view.findViewById(R.id.tv_min);
            tvDate.setText(forecast.date);
            tvInfo.setText(forecast.more.info);
            tvMax.setText(forecast.temperature.max);
            tvMin.setText(forecast.temperature.min);
            llForecast.addView(view);
        }
        if (weather.aqi != null) {
            tvAqi.setText(weather.aqi.city.aqi);
            tvPm25.setText(weather.aqi.city.pm25);
        }
        String comfort = COMFORT_TIP + weather.suggestion.comfort.info;
        String carWash = CAR_WASH_TIP + weather.suggestion.carWash.info;
        String sport = SPORT_TIP + weather.suggestion.sport.info;
        tvComfort.setText(comfort);
        tvCarWash.setText(carWash);
        tvSport.setText(sport);
        scvWeatherLayout.setVisibility(View.VISIBLE);
        //成功展示数据后，启动后台服务，每八小时更新一次天气信息和背景图片信息并保存到sp中。
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 从服务器读取图片路径
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString(KEY_BING_PIC, bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(ivBingPic);
                    }
                });
            }
        });
    }

}
