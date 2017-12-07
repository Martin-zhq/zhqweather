package com.example.martin.zhqweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by zhq on 2017/12/7.
 */

public class City extends DataSupport {

    private int id;

    /** 城市名字 */
    private String cityName;

    /** 城市代号 */
    private int cityCode;

    /** 当前城市所属省的id */
    private int provinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
