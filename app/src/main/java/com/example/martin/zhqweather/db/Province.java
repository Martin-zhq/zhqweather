package com.example.martin.zhqweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by zhq on 2017/12/7.
 */

public class Province extends DataSupport {

    private int id;

    /** 省的名字 */
    private String provinceName;

    /** 省的代号 */
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
