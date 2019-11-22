package com.hcmiu.thesis.mats_driver.DataAccess;

import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by edunetjsc on 11/7/16.
 */

public class DriverDataAccess {

    private DataConnection dataConnection;

    public DriverDataAccess() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        dataConnection = new DataConnection();
    }

    public ResultSet selectMyDriver(String id) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT * FROM driver where id = '"+id+"'";
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

    public ResultSet selectPassenger(String id) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT isCalled, passengerIDCalled FROM driver where id = '"+id+"'";
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

    public ResultSet selectPassengerInfo(int passID) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT name,phone,latitude,longitude FROM passenger where id = '"+passID+"'";
        Log.i("select Passenger",a);
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

    public void insert(String id, String email, String phone, String password, String name, String car_type) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a = "INSERT into driver (id, email, phone, password, name, car_type, isCalled) values ('"+id+"','"+email+"','"+phone+"','"+password+"','"+name+"','"+car_type+"','0');";
        Log.i("insert driver",a);
        dataConnection.executeUpdate(a);
    }

    public ResultSet selectTaxi() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT * FROM driver";
        ResultSet r = dataConnection.executeQuery(a);
        Log.i("connect","aaaaaaaa");
        Log.i("connect",""+r);
        return r;
    }
//
//    public static void Delete(String id) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
//        String a = "DELETE from testtable where id='" + id + "';";
//        //Connector.ExecuteUpdate(a);
//    }
//
    public void update(String id, String name, String email, String phone, String password, String car_type) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a =  "UPDATE driver set name='" + name + "', email='" + email + "', phone='" + phone + "', password='" + password + "', car_type='" + car_type + "' where id ='"+ id +"';";
        Log.i("updateeeeeeee",a);
        dataConnection.executeUpdate(a);
        //Connector.ExecuteUpdate(a);
    }

    public void updateIsCalled(String id) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a =  "UPDATE driver set isCalled=0" + " where id ='"+ id +"';";
        Log.i("updateeeeeeee",a);
        dataConnection.executeUpdate(a);
        //Connector.ExecuteUpdate(a);
    }

    public void updateDriverLocation(String id, double latitude, double longitude) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        String a =  "UPDATE driver set latitude='" + latitude + "', longitude='" + longitude + "' where id ='"+ id +"';";
        Log.i("debug",a);
        dataConnection.executeUpdate(a);
    }

    public ResultSet select() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT * FROM driver";
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

}
