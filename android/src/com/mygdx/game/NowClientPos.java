package com.mygdx.game;

public class NowClientPos {
    private static double nowLongitude;    // 用户当前经度
    private static double nowLatitude; // 用户当前纬度


    /**
     * 获取用户当前纬度
     *
     * @return 当前纬度
     */
    public static double getNowLatitude() {
        return nowLatitude;
    }

    /**
     * 获取用户当前经度
     *
     * @return 当前经度
     */
    public static double getNowLongitude() {
        return nowLongitude;
    }

    /**
     * 设置用户当前经度
     *
     * @param nowLongitude 用户当前经度
     */
    public static void setNowLongitude(double nowLongitude) {
        NowClientPos.nowLongitude = nowLongitude;
    }

    /**
     * 设置用户当前纬度
     *
     * @param nowLatitude 用户当前纬度
     */
    public static void setNowLatitude(double nowLatitude) {
        NowClientPos.nowLatitude = nowLatitude;
    }
    /**
     * 设置位置参数
     *
     * @param ClientPos 用户位置
     */
    public static void setPosPara(ClientPos ClientPos) {
        NowClientPos.nowLongitude = ClientPos.getLongitude();
        NowClientPos.nowLatitude = ClientPos.getLatitude();
    }

    public static void reset() {
        setNowLongitude(0);
        setNowLatitude(0);
    }
}
