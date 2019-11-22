package com.hcmiu.thesis.mats.DataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by edunetjsc on 11/7/16.
 */

public class DataConnection {

    //private static String url="jdbc:mysql://10.0.3.2/MATS";
    private static String url="jdbc:mysql://112.78.2.161:3306/nhad7399_MATS?useUnicode=yes&characterEncoding=UTF-8";

    //  Database credentials
    private static final String USER = "nhad7399";
    private static final String PASS = "Pikachu@123456";
    //  Database credentials
    //private static final String USER = "root";
    //private static final String PASS = "root";
    private static Connection conn = null;
    private static Statement state = null;
    /**
     * Get a connection to database
     * @return Connection object
     */
    public DataConnection() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException{
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(url,USER,PASS);
        state = conn.createStatement();
    }

    //select
    public ResultSet executeQuery(String query) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException{
        ResultSet rs = state.executeQuery(query);
        return rs;
    }

    //insert, update, delete
    public void executeUpdate(String query) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException{
        int b = 0;
        b = state.executeUpdate(query);
    }
}

