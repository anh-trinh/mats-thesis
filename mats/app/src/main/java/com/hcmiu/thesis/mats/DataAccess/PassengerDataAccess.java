package com.hcmiu.thesis.mats.DataAccess;

import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by edunetjsc on 11/7/16.
 */

public class PassengerDataAccess {

    private DataConnection dataConnection;

    public PassengerDataAccess() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        dataConnection = new DataConnection();
    }

    public ResultSet select() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT * FROM passenger";
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

    public void insert(int id, String email, String phone, String password, String name) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a = "INSERT into passenger (id, email, phone, password, name) values ('"+id+"','"+email+"','"+phone+"','"+password+"','"+name+"');";
        dataConnection.executeUpdate(a);
    }

    public ResultSet selectTaxi() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT * FROM driver where isCalled = '0'";
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
    public void update(int id, String name, String email, String phone, String password) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a =  "UPDATE passenger set name='" + name + "', email='" + email + "', phone='" + phone + "', password='" + password + "' where id ='"+ id +"';";
        Log.i("updateeeeeeeewwwwwww",a);
        dataConnection.executeUpdate(a);
        //Connector.ExecuteUpdate(a);
    }

    public void updateLocation(String id, double latitude, double longitude) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a =  "UPDATE passenger set latitude='" + latitude + "', longitude='" + longitude + "' where id ='"+ id +"';";
        Log.i("updateeeeeeee",a);
        dataConnection.executeUpdate(a);
        //Connector.ExecuteUpdate(a);
    }

}
