package com.hcmiu.thesis.mats.DataAccess;

import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by edunetjsc on 4/14/17.
 */

public class TripAccess {

    private DataConnection dataConnection;

    public TripAccess() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        dataConnection = new DataConnection();
    }

    public ResultSet selectTrip(String id) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT * FROM trip where passenger_id = '"+id+"'";
        Log.i("select trip",a);
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

    public ResultSet selectDriverFromID(int id) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT car_type FROM driver where id = '"+id+"'";
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

    public ResultSet selectAllDriverRating(String id) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT rating FROM trip where driver_id = '"+id+"'";
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }

    public void insertNewTrip(String id, String pass_id, String driver_id, String price, java.sql.Date date, String start_address, String des_address, String car_type) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a = "INSERT into trip(id, passenger_id, driver_id, price, date, start_address, des_address, car_type,rating) value('"+id+"','"+pass_id+"','"+driver_id+"','"+price+"','"+date+"','"+start_address+"','"+des_address+"','"+car_type+"',0);";
        Log.i("insert trip",a);
        dataConnection.executeUpdate(a);
    }

    public void deleteTrip(String id) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a = "DELETE from trip where id='" + id + "';";
        dataConnection.executeUpdate(a);
    }

    public void updateDriverRating(String id, int star) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a =  "UPDATE driver set rating='" + star + "' where id ='"+ id +"';";
        Log.i("updateeCallTaxi",a);
        dataConnection.executeUpdate(a);
        //Connector.ExecuteUpdate(a);
    }

    public void updateTripRating(String id, int star) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a =  "UPDATE trip set rating='" + star + "' where id ='"+ id +"';";
        Log.i("updateeCallTaxi",a);
        dataConnection.executeUpdate(a);
        //Connector.ExecuteUpdate(a);
    }
}
