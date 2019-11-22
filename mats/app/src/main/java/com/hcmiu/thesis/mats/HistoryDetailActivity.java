package com.hcmiu.thesis.mats;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.hcmiu.thesis.mats.DataAccess.DriverDataAccess;
import com.hcmiu.thesis.mats.Model.Trip;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class HistoryDetailActivity extends AppCompatActivity {

    TextView his_detail_date;
    TextView his_detail_price;
    TextView his_detail_drivername;
    TextView his_detail_driverphone;
    TextView his_detail_cartype;
    TextView his_detail_start;
    TextView his_detail_finish;
    Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        trip = (Trip) bundle.getSerializable("one_trip");

        his_detail_date = (TextView) findViewById(R.id.his_detail_date);
        his_detail_price = (TextView) findViewById(R.id.his_detail_price);
        his_detail_drivername = (TextView) findViewById(R.id.his_detail_drivername);
        his_detail_driverphone = (TextView) findViewById(R.id.his_detail_driverphone);
        his_detail_cartype = (TextView) findViewById(R.id.his_detail_cartype);
        his_detail_start = (TextView) findViewById(R.id.his_detail_start);
        his_detail_finish = (TextView) findViewById(R.id.his_detail_finish);

        new DriverDetail().execute();
    }

    private class DriverDetail extends AsyncTask<Object, Object, Boolean> {

        String name = "";
        String phone = "";


        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                DriverDataAccess driver = new DriverDataAccess();
                final ResultSet result = driver.selectDriver();
                while (result.next()) {
                    name = result.getString("name");
                    phone = result.getString("phone");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Boolean result) {
            his_detail_date.setText("Date: "+new SimpleDateFormat("dd-MM-yyyy").format(trip.getDate()).toString());
            his_detail_price.setText("Price: "+trip.getPrice());
            his_detail_drivername.setText("Driver name: "+name);
            his_detail_driverphone.setText("Driver Phone: "+phone);
            his_detail_cartype.setText("Car Type: "+trip.getCarname());
            his_detail_start.setText("Pick-up Place: "+trip.getStart_address());
            his_detail_finish.setText("Destination: "+trip.getDes_address());
        }

    }
}
