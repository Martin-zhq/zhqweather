package com.example.martin.zhqweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Martin on 2017/12/7.
 */

public class Basic {

    /** @SerializedName注解用来让JSON字段和java字段之间产生对应关系 */
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }
}
