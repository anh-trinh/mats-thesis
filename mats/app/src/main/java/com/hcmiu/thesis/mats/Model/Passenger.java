package com.hcmiu.thesis.mats.Model;

import java.io.Serializable;

/**
 * Created by edunetjsc on 11/7/16.
 */

public class Passenger implements Serializable{
    String id;
    String email;
    String phone;
    String pass;
    String name;
    double latitude;
    double longitude;

    public Passenger(String id, String email, String phone, String pass, String name) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.pass = pass;
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

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
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
