package com.example.martin.zhqweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.martin.zhqweather.db.City;
import com.example.martin.zhqweather.db.Country;
import com.example.martin.zhqweather.db.Province;
import com.example.martin.zhqweather.util.HttpUtil;
import com.example.martin.zhqweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zhq on 2017/12/7.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;

    private TextView tvTitleText;

    private Button btnBack;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /** 省列表 */
    private List<Province> provinceList;

    /** 市列表 */
    private List<City> cityList;

    /** 县列表 */
    private List<Country> countryList;

    /** 选中的省份 */
    private Province selectedProvince;

    /** 选中的城市 */
    private City selectedCity;

    /** 当前选中的级别 */
    private int currentLevel;

    /** 标题 */
    private static final String TITLE_TEXT = "中国";

    /** 基础url */
    private static final String ADDRESS = "http://guolin.tech/api/china";

    /** 省份标志位 */
    private static final String PROVINCE = "province";

    /** 市标志位 */
    private static final String CITY = "city";

    /** 县标志位 */
    private static final String COUNTRY = "country";

    /** 加载失败的提示 */
    private static final String LOAD_FAILED_TIP = "加载失败";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        tvTitleText = view.findViewById(R.id.tv_title_text);
        btnBack = view.findViewById(R.id.btn_back);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    //当前列表级别为省列表，选中某一省后查询对应的市展示
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    //当前列表级别为市列表，选中某一市后查询对应的县区展示
                    queryCountries();
                } else if (currentLevel == LEVEL_COUNTRY) {
                    //当前为县级列表，则展示天气情况
                    String weatherId = countryList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        //当前所在activity为mainActivity
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra(WeatherActivity.BUNDLE_WEATHER_ID, weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity){
                        //当前所在activity为WetherActivity
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTRY) {
                    //返回按钮，如果当前为县级列表，点击返回则查询市并展示市级列表
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    //返回按钮，如果当前为市级列表，点击返回则查询省并展示省级列表
                    queryProvinces();
                }
            }
        });
        //默认为查询省级列表并展示
        queryProvinces();
    }

    /**
     * 查询所有省，先走数据库，没有再走网络，然后再走数据库
     */
    private void queryProvinces() {
        //设置当前标题为中国
        tvTitleText.setText(TITLE_TEXT);
        //省级列表则不显示返回按钮
        btnBack.setVisibility(View.GONE);
        //调用litepal的api进行查询 省 的数据
        provinceList = DataSupport.findAll(Province.class);
        //从数据库中查询出了省的数据
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        else {
            //没有数据,走服务器读数据
            String address = ADDRESS;
            queryFromServer(address, PROVINCE);
        }
    }

    /**
     * 查询市
     */
    private void queryCities() {
        //设置标题为当前市所在省份
        tvTitleText.setText(selectedProvince.getProvinceName());
        //市级列表可以返回到省级
        btnBack.setVisibility(View.VISIBLE);
        //利用litepal的api查询数据库中是否有市级数据
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId()))
        .find(City.class);
        //有数据
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            // 没有数据，则从服务器请求
            int provinceCode = selectedProvince.getProvinceCode();
            String address = ADDRESS + "/" + provinceCode;
            queryFromServer(address, CITY);
        }
    }

    /**
     * 查询市区内县的数据
     */
    private void queryCountries() {
        //设置当前县所在市为标题
        tvTitleText.setText(selectedCity.getCityName());
        //县级可以返回到市级 其实可以不用设置
        btnBack.setVisibility(View.VISIBLE);
        //调用litepal的API查询县的数据
        countryList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId()))
                .find(Country.class);
        if (countryList.size() > 0) {
            dataList.clear();
            for (Country country : countryList) {
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = ADDRESS + "/" + provinceCode + "/" + cityCode;
            queryFromServer(address, COUNTRY);
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //请求失败，进行提示
                        closeProgressDialog();
                        Toast.makeText(getActivity(), LOAD_FAILED_TIP, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if (PROVINCE.equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if (CITY.equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if (COUNTRY.equals(type)) {
                    result = Utility.handleCountryResponse(responseText, selectedCity.getId());
                }
                //保存数据库成功
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (PROVINCE.equals(type)) {
                                queryProvinces();
                            } else if (CITY.equals(type)) {
                                queryCities();
                            } else if (COUNTRY.equals(type)) {
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
