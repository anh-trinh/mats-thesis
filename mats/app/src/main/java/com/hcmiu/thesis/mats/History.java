package com.hcmiu.thesis.mats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hcmiu.thesis.mats.DataAccess.TripAccess;
import com.hcmiu.thesis.mats.Model.Passenger;
import com.hcmiu.thesis.mats.Model.Trip;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class History extends AppCompatActivity {

    ListView history_list;
    ArrayList<Trip> trips;
    Passenger passenger;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        history_list = (ListView) findViewById(R.id.history_list);
        trips = new ArrayList<>();

        passenger = (Passenger) getIntent().getSerializableExtra("Passenger");
        sp = History.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);

        new LoadTrip().execute();

    }

    private class LoadTrip extends AsyncTask<Object, Object, Boolean> {


        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                TripAccess tripAccess = new TripAccess();
                final ResultSet result = tripAccess.selectTrip(sp.getString("id", "0"));
                while (result.next()) {
                    Trip one_trip = new Trip(result.getString("id"),result.getString("passenger_id"),result.getString("driver_id"),result.getString("price"),result.getDate("date"),result.getString("start_address"),result.getString("des_address"),result.getString("car_type"));
                    Log.i("his_onetrips",""+one_trip);
                    trips.add(one_trip);
                    //taxiMarker(name,latitude,longitude);
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

            super.onPostExecute(result);
            Log.i("his_trips",trips.toString());
            history_list.setAdapter(new HistoryAdapter(History.this,trips));
            history_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(History.this,HistoryDetailActivity.class);
                    intent.putExtra("one_trip",trips.get(i));
                    startActivity(intent);
                }
            });
        }

    }
}
