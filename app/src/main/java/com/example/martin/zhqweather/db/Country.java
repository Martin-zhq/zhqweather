package com.example.martin.zhqweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by zhq on 2017/12/7.
 */

public class Country extends DataSupport {

    private int id;

    /** 县的名字 */
    private String countryName;

    /** 县对应的天气信息id */
    private String weatherId;

    /** 县所属市的id */
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
