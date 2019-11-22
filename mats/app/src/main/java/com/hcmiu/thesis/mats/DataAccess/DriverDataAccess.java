package com.hcmiu.thesis.mats.DataAccess;

import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by edunetjsc on 3/10/17.
 */

public class DriverDataAccess {

    private DataConnection dataConnection;

    public DriverDataAccess() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        dataConnection = new DataConnection();
    }

    public void updateCallTaxi(String id, String passID) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String a =  "UPDATE driver set isCalled='1', passengerIDCalled='" + passID + "' where id ='"+ id +"';";
        Log.i("updateeCallTaxi",a);
        dataConnection.executeUpdate(a);
        //Connector.ExecuteUpdate(a);
    }

    public void updateDriverLocation(int id, double latitude, double longitude) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        String a =  "UPDATE driver set latitude='" + latitude + "', longitude='" + longitude + "' where id ='"+ id +"';";
        dataConnection.executeUpdate(a);
    }

    public ResultSet selectDriver() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String a = "SELECT * FROM driver";
        ResultSet r = dataConnection.executeQuery(a);
        return r;
    }
}
