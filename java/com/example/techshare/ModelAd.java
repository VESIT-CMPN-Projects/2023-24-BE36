package com.example.techshare;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ModelAd {

    private String id;
    private String uid;
    private String brand;
    private String category;
    private String condition;
    private String address;
    private String price;
    private String title;
    private String description;
    private String status;
    private long timestamp;
    private double latitude;
    private double longitude;
    private boolean favorite;


    private boolean cart;

    private int rent_period;

    private long rented_on;

    private String rented_to;

    public ModelAd() {

    }


    public ModelAd(String id, String uid, String brand, String category, String condition, String address, String price, String title, String description, String status, long timestamp, Map<String, Object> map, int rent_period, long rented_on, String rented_to) {
        this.id = id;
        this.uid = uid;
        this.brand = brand;
        this.category = category;
        this.condition = condition;
        this.address = address;
        this.price = price;
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.rent_period = rent_period;
        this.rented_on = rented_on;
        this.rented_to = rented_to;
        this.latitude = Double.parseDouble(map.get("latitude").toString());
        this.longitude = Double.parseDouble(map.get("longitude").toString());
        this.favorite = isFavorite();
        this.cart = isCart();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {

        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getRent_period() {
        return rent_period;
    }

    public void setRent_period(int rent_period) {
        this.rent_period = rent_period;
    }

    public long getRented_on() {
        return rented_on;
    }

    public void setRented_on(long rented_on) {
        this.rented_on = rented_on;
    }

    public String getRented_to() {
        return rented_to;
    }

    public void setRented_to(String rented_to) {
        this.rented_to = rented_to;
    }

    public boolean isCart() {
        return cart;
    }

    public void setCart(boolean cart) {
        this.cart = cart;
    }
}
