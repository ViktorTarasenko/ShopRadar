package com.mygdx.game;

import java.io.Serializable;

public class ClientPos implements Serializable {
    private double longitude;    // 用户当前经度
    private double latitude;    // 用户当前纬度
    private float accuracy;     // 当前位置的置信度
    private String provider;    // 定位方式

    public ClientPos(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public ClientPos(String[] posStr) {
        this.longitude = Double.valueOf(posStr[1]);
        this.latitude = Double.valueOf(posStr[0]);
    }
    /**
     * 获取用户当前纬度
     *
     * @return 当前纬度
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * 获取用户当前经度
     *
     * @return 当前经度
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * 获取当前位置的精度
     *
     * @return 精度
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * 获取当前位置获取方式
     *
     * @return 位置获取方式(GPS or Network)
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置当前位置的精度
     *
     * @param accuracy 精度
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * 设置当前位置的获取方式
     *
     * @param provider 位置获取方式
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }
}
