package com.hcmiu.thesis.mats_driver.Model;

/**
 * Created by edunetjsc on 3/10/17.
 */

public class TaxiDriver {

    String id;
    String email;
    String phone;
    String name;
    double latitude;
    double longitude;

    public TaxiDriver(String id, String email, String phone, String name) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
