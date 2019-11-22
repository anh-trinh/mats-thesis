package com.hcmiu.thesis.mats_driver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hcmiu.thesis.mats_driver.DataAccess.DriverDataAccess;
import com.hcmiu.thesis.mats_driver.Model.Passenger;
import com.hcmiu.thesis.mats_driver.Model.TaxiDriver;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Location myLocation = new Location("myLocation");
    private Location passLocation = new Location("passLocation");
    private LocationManager locationManager;

    Passenger passengerCalled = new Passenger();

    boolean checkCalled = false;

    TextView address;
    TextView esti_distance;
    TextView esti_time;
    TextView passName;
    TextView passPhone;

    Timer t;

    TaxiDriver myDriver;

    Marker myMarker;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    Button callbutton;
    LinearLayout layout;

    boolean checkClick = true;

    private String username = "";
    private String password = "";

    TaxiDriver saveDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLocationPermission();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle("You should open your location to use this app")
                    .setMessage("Would you like to open your location?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        SharedPreferences sp = MapsActivity.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);
        username = sp.getString("email", null);
        password = sp.getString("password", null);
        Log.i("aaaaaaaaaaa",username+"       "+password);
        if(username == null || password == null || username == "" || password == ""){
            Intent i = new Intent(MapsActivity.this, LoginActivity.class);
            finish();
            startActivity(i);
        }
        else {
            saveDriver = new TaxiDriver(sp.getString("id",null),username,sp.getString("phone",null),sp.getString("name",null));
            new GetInfo(1).execute();

            //Declare the timer
            t = new Timer();
            //Set the schedule function and rate
            t.scheduleAtFixedRate(new TimerTask() {

                                      @Override
                                      public void run() {
                                          //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                          Log.i("test taskkkkkkkkk", "" + myLocation);
                                          if (myLocation.getLatitude() != 0.0 && myLocation.getLongitude() != 0.0) {
                                              if (saveDriver.getId()==null){
                                                  SharedPreferences sp2 = MapsActivity.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);
                                                  saveDriver = new TaxiDriver(sp2.getString("id",null),username,sp2.getString("phone",null),sp2.getString("name",null));
                                              }
                                              //new UpdateDriverPosition(myLocation.getLatitude(),myLocation.getLongitude()).execute();
                                              new GetPassengerCalled().execute();
                                          }
                                      }

                                  },
                    //Set how long before to start calling the TimerTask (in milliseconds)
                    0,
                    //Set the amount of time between each execution (in milliseconds)
                    2000);
        }

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        address = (TextView) findViewById(R.id.address);
        passName = (TextView) findViewById(R.id.passName);
        passPhone = (TextView) findViewById(R.id.passPhone);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private Marker myLocationMarker(Location location) {
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi))
                .position(new LatLng(location.getLatitude(), location.getLongitude()));
        Marker myLocMarker = mMap.addMarker(options);
        myLocMarker.showInfoWindow();

        return myLocMarker;
    }

    private Marker passengerLocationMarker(Location location) {
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_passenger))
                .position(new LatLng(location.getLatitude(), location.getLongitude()));
        Marker myLocMarker = mMap.addMarker(options);

        return myLocMarker;
    }

    private void handleNewLocation(Location location) {

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));

        if(checkClick) {
            myMarker = myLocationMarker(location);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }

        myLocation.setLatitude(currentLatitude);
        myLocation.setLongitude(currentLongitude);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Log.i("hello","eeeeeeeee");

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                checkClick = false;
                mMap.clear();
                new UpdateDriverPosition(latLng.latitude, latLng.longitude).execute();
                Location testLocation = new Location("testLocation");
                testLocation.setLatitude(latLng.latitude);
                testLocation.setLongitude(latLng.longitude);
                myLocationMarker(testLocation);
            }
        });

        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private class UpdateDriverPosition extends AsyncTask<Object, Object, Boolean> {

        private double latitude;
        private double longitude;

        public UpdateDriverPosition(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
            try {
                //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
                DriverDataAccess driver = new DriverDataAccess();
                //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
                SharedPreferences sp = MapsActivity.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);
                driver.updateDriverLocation(sp.getString("id", "0"), latitude, longitude);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }

    }

    private class GetPassengerCalled extends AsyncTask<Object, Object, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
            try {
                //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
                DriverDataAccess driver = new DriverDataAccess();
                //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
                final ResultSet result = driver.selectPassenger(saveDriver.getId());
                while (result.next()) {
                    if (result.getInt("isCalled") == 1) {
                        passengerCalled.setId(result.getInt("passengerIDCalled"));
                        checkCalled = true;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (checkCalled) {
                t.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("A passenger called taxi for a trip!")
                        .setTitle("Passenger Request")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                new GetInfo(2).execute();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

    }

    private class GetInfo extends AsyncTask<Object, Object, Boolean> {

        int key;

        //key 1 = driver, key 2 = passenger;

        private GetInfo(int key) {
            this.key = key;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
            try {
                DriverDataAccess driver = new DriverDataAccess();
                if (key == 1) {
                    //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
                    //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
                    final ResultSet result = driver.selectMyDriver(saveDriver.getId());
                    while (result.next()) {
                        myDriver = new TaxiDriver(saveDriver.getId(), result.getString("email"), result.getString("phone"), result.getString("name"));
                    }
                } else if (key == 2) {
                    final ResultSet result = driver.selectPassengerInfo(passengerCalled.getId());
                    while (result.next()) {
                        passengerCalled = new Passenger(passengerCalled.getId(), result.getString("name"), result.getString("phone"), result.getDouble("latitude"), result.getDouble("longitude"));
                    }
                    driver.updateIsCalled(myDriver.getId());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (key == 2) {
                layout = (LinearLayout) findViewById(R.id.info_layout);
                callbutton = new Button(MapsActivity.this);
                callbutton.setText("Call Passenger");
                callbutton.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                layout.addView(callbutton);
                callbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + passengerCalled.getPhone()));
                        //check permission
                        //If the device is running Android 6.0 (API level 23) and the app's targetSdkVersion is 23 or higher,
                        //the system asks the user to grant approval.
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            //request permission from user if the app hasn't got the required permission
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{android.Manifest.permission.CALL_PHONE},   //request specific permission from user
                                    10);
                            return;
                        }else {     //have got permission
                            try{
                                startActivity(intent);  //call activity and make phone call
                            }
                            catch (android.content.ActivityNotFoundException ex){
                                Toast.makeText(getApplicationContext(),"yourActivity is not founded",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                passLocation.setLatitude(passengerCalled.getLatitude());
                passLocation.setLongitude(passengerCalled.getLongitude());
                address.setText("Address: "+getAddressByLocation(passLocation));
                passName.setText("Name: "+passengerCalled.getName());
                passPhone.setText("Phone: "+passengerCalled.getPhone());
                passengerLocationMarker(passLocation);
            }
        }

    }

    public String getAddressByLocation(Location location){
        String filterAddress = "";
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses =
                    geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() > 0) {
                for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
                    filterAddress += addresses.get(0).getAddressLine(i) + " ";
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e2) {
            // TODO: handle exception
            e2.printStackTrace();
        }
        return filterAddress;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location")
                        .setMessage("Do you want to open location?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        switch (item.getItemId()) {

            case R.id.setting_item:
                Intent i = new Intent(MapsActivity.this,SettingActivity.class);
                startActivity(i);
            break;

            case R.id.logout_item:
                SharedPreferences sp = MapsActivity.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);
                sp.edit().clear().commit();
                Intent i2 = new Intent(MapsActivity.this, LoginActivity.class);
                startActivity(i2);
            break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
