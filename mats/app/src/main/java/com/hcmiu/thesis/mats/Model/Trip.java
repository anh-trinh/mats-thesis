package com.hcmiu.thesis.mats.Model;

import java.io.Serializable;
import java.sql.Date;

/**
 * Created by edunetjsc on 4/14/17.
 */

public class Trip implements Serializable{
    String id;
    Date date;
    String passenger_id;
    String driver_id;
    String carname;
    String price;
    String start_address;
    String des_address;

    public Trip() {

    }

    public Trip(String id, String passenger_id, String driver_id, String price, Date date, String start_address, String des_address, String carname) {
        this.id = id;
        this.date = date;
        this.passenger_id = passenger_id;
        this.driver_id = driver_id;
        this.price = price;
        this.start_address = start_address;
        this.des_address = des_address;
        this.carname = carname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPassenger_id() {
        return passenger_id;
    }

    public void setPassenger_id(String passenger_id) {
        this.passenger_id = passenger_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStart_address() {
        return start_address;
    }

    public void setStart_address(String start_address) {
        this.start_address = start_address;
    }

    public String getDes_address() {
        return des_address;
    }

    public void setDes_address(String des_address) {
        this.des_address = des_address;
    }

    public String getCarname() {
        return carname;
    }

    public void setCarname(String carname) {
        this.carname = carname;
    }
}
