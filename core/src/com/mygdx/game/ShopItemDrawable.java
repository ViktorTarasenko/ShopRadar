package com.mygdx.game;

import java.util.Objects;

public class ShopItemDrawable {
    public ShopItemDrawable(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private final String name;
    private final double latitude;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopItemDrawable shopItem = (ShopItemDrawable) o;
        return Double.compare(shopItem.latitude, latitude) == 0 && Double.compare(shopItem.longitude, longitude) == 0 && Objects.equals(name, shopItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }

    private final double longitude;

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
