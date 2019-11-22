package com.hcmiu.thesis.mats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hcmiu.thesis.mats.DataAccess.DriverDataAccess;
import com.hcmiu.thesis.mats.DataAccess.PassengerDataAccess;
import com.hcmiu.thesis.mats.DataAccess.TripAccess;
import com.hcmiu.thesis.mats.Model.Passenger;
import com.hcmiu.thesis.mats.Model.TaxiDriver;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.hcmiu.thesis.mats.R.drawable.taxi;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Location myLocation = new Location("myLocation");
    private Location myDestination = new Location("myDestination");
    ArrayList<TaxiDriver> taxiList = new ArrayList<>();
    ArrayList<LatLng> taxiMarkerWayList = new ArrayList<>();
    Passenger myPassenger;

    private double old_my_lat = 0.000000;
    private double old_my_long = 0.000000;

    int SET_PICK_UP_PLACE = 1;

    Marker myMarker;
    Marker taxiRuning;

    float time_taxi = 0;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private String username = "";
    private String password = "";

    private LocationManager locationManager;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private Timer t;

    String des_update_trip = "";

    private boolean keyOpenLocation = false;

    TextView name;
    TextView esti_price;
    TextView destination;
    TextView esti_time;
    TextView people_size;

    boolean taxiMovingKey = false;
    boolean checkInfoWindowClick = false;
    private Location taxiNewLocationToSaved = new Location("taxiNewLocationToSaved");

    String des_price = "0";
    String trip_id = "";

    boolean checkPickup = false;

    TaxiDriver taxiMoving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        //kiem tra co allow permission chua, trong ham nay co hoat dong khac
        if (checkLocationPermission()){
            if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                displayLocationSettingsRequest(getApplicationContext());
                keyOpenLocation = true;
            }
        }

        SharedPreferences sp = MapsActivity.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);
        username = sp.getString("email", null);
        password = sp.getString("password", null);
        Log.i("aaaaaaaaaaa",username+"       "+password);
        if(username == null || password == null || username == "" || password == ""){
            Intent i = new Intent(MapsActivity.this, LoginActivity.class);
            startActivity(i);
        }
        else {
            myPassenger = new Passenger(sp.getString("id",null),username,sp.getString("phone",null),password,sp.getString("name",null));
        }
        setContentView(R.layout.activity_maps);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setGoogleApiForLocation();

        name = (TextView) findViewById(R.id.name);
        esti_price = (TextView) findViewById(R.id.esti_price);
        destination = (TextView) findViewById(R.id.destination);
        esti_time = (TextView) findViewById(R.id.esti_time);
        people_size = (TextView) findViewById(R.id.people_size);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.get_destination);

        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.get_pickupplace);

        autocompleteFragment.setHint("Choose your destination");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng latLng = place.getLatLng();

                myDestination.setLatitude(latLng.latitude);
                myDestination.setLongitude(latLng.longitude);

                float distance = 0;
                String time = "";
                try {
                    distance = Float.parseFloat(getDistanceDuration(myLocation,myDestination)[0]);
                    time = getDistanceDuration(myLocation,myDestination)[1];
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                float cal_price = Math.round(11500*(distance));
                des_price = NumberFormat.getNumberInstance(Locale.US).format(time_taxi*600+cal_price)+" VND";

                esti_price.setText("Fare Estimate: "+des_price);
                des_update_trip = ""+place.getName();
                destination.setText("Destination: "+des_update_trip);
                esti_time.setText("Time Estimate: "+time+" minutes");
                people_size.setText("Max Size: 4 People");

                esti_price.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("FARE DETAIL")
                                .setMessage("600VND/MIN AND 11,500VND/KM")
                                .show();
                    }
                });
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

        autocompleteFragment2.setHint("Choose your pick-up place");
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng latLng = place.getLatLng();
                checkPickup = true;
                myLocation.setLatitude(latLng.latitude);
                myLocation.setLongitude(latLng.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                if(myDestination.getLatitude()!=0.000000&&myDestination.getLongitude()!=0.000000) {
                    float distance = 0;
                    String time = "";
                    try {
                        distance = Float.parseFloat(getDistanceDuration(myLocation,myDestination)[0]);
                        time = getDistanceDuration(myLocation,myDestination)[1];
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    float cal_price = Math.round(11500*(distance));
                    des_price = NumberFormat.getNumberInstance(Locale.US).format(time_taxi*600+cal_price)+" VND";

                    esti_price.setText("Fare Estimate: "+des_price);
                    destination.setText("Destination: "+place.getName());
                    esti_time.setText("Time Estimate: "+time+" minutes");
                    people_size.setText("Max Size: 4 People");

                    esti_price.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(MapsActivity.this)
                                    .setTitle("FARE DETAIL")
                                    .setMessage("600VND/MIN AND 11,500VND/KM")
                                    .show();
                        }
                    });
                }
            }

            @Override
            public void onError(Status status) {

            }
        });

        if (autocompleteFragment.isRemoving()){
            Toast.makeText(MapsActivity.this,"aasdassa",Toast.LENGTH_SHORT).show();
        }

        //Declare the timer
        t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      //show taxis
                                      Log.i("Taxi Moving Test","moveeeeeeeeeeee");
                                      new MyTask().execute();
                                  }

                              },
                //Set how long before to start calling the TimerTask (in milliseconds)
                0,
                //Set the amount of time between each execution (in milliseconds)
                2000);

    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("aaa", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("aaa", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("aaa", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("aaa", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id){
                    case R.id.history:
                        Toast.makeText(getApplicationContext(),"History",Toast.LENGTH_SHORT).show();
                        Intent i3 = new Intent(MapsActivity.this,History.class);
                        i3.putExtra("Passenger", myPassenger);
                        startActivity(i3);
                        break;
                    case R.id.settings:
                        Toast.makeText(getApplicationContext(),"Settings",Toast.LENGTH_SHORT).show();
                        Intent i2 = new Intent(MapsActivity.this,SettingActivity.class);
                        startActivity(i2);
                        break;
                    case R.id.logout:
                        SharedPreferences sp = MapsActivity.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);
                        sp.edit().clear().commit();
                        Intent i = new Intent(MapsActivity.this, LoginActivity.class);
                        startActivity(i);
                        break;

                }
                return true;
            }
        });
        View header = navigationView.getHeaderView(0);
        TextView tv_email = (TextView)header.findViewById(R.id.tv_email);
        tv_email.setText(username);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
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

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        setAfterLoadMap();

    }

    public void setAfterLoadMap(){
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
        mMap.setMyLocationEnabled(true);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                checkInfoWindowClick = true;

                if (taxiMovingKey==false){

                    t.cancel();

                    if (taxiList.size()>0){

                        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                //return null;
                                Log.i("pikachucccc",""+taxiMovingKey);
                                View v = getLayoutInflater().inflate(R.layout.infowindow_layout, null);
                                TextView tv_lng = (TextView) v.findViewById(R.id.tv_lng);
                                tv_lng.setText("CANCEL");
                                return v;
                            }
                        });

                        Location nearestTaxi = new Location("nearestTaxi");

                        TaxiDriver taxi_nearest = taxiList.get(0);
                        nearestTaxi.setLatitude(taxi_nearest.getLatitude());
                        nearestTaxi.setLongitude(taxi_nearest.getLongitude());
                        float nearestDistance = 0;
                        float nearestTime = 0;
                        try {
                            nearestDistance = Float.parseFloat(getDistanceDuration(myLocation, nearestTaxi)[0]);
                            nearestTime = Float.parseFloat(getDistanceDuration(myLocation, nearestTaxi)[1]);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        for (int i = 1; i < taxiList.size(); i++) {
                            TaxiDriver taxi = taxiList.get(i);
                            Location compare = new Location("compare");
                            compare.setLatitude(taxi.getLatitude());
                            compare.setLongitude(taxi.getLongitude());
                            float result = 0;
                            float time = 0;
                            try {
                                result = Float.parseFloat(getDistanceDuration(myLocation, compare)[0]);
                                time = Float.parseFloat(getDistanceDuration(myLocation, compare)[1]);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (result < nearestDistance) {
                                taxi_nearest = taxi;
                                nearestDistance = result;
                                nearestTime = time;
                                nearestTaxi = compare;
                            }
                        }

                        Log.i("nearest taxi", "" + nearestTaxi);

                        time_taxi = nearestTime;
                        Log.i("timeeeeeeeee", "" + time_taxi);

                        taxiMoving = taxi_nearest;

                        UpdateDriverCalled updateDriverCalled = new UpdateDriverCalled(taxi_nearest.getId());
                        updateDriverCalled.execute();

                        java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
                        Log.i("uuuu",""+myLocation.getLatitude());
                        UpdateTrip updateTrip = new UpdateTrip(myPassenger.getId(),taxi_nearest.getId(),des_price,date,getAddressByLocation(myLocation),taxi_nearest.getCar_type());
                        updateTrip.execute();

                        new GetDriverRating().execute();

                        taxiMovingKey = true;

                        //make taxi moving
                        String url = getMapsApiDirectionsUrlWithTwoPoints(nearestTaxi, myLocation);
                        ReadTask downloadTask = new ReadTask(1,1,false);
                        downloadTask.execute(url);

                    }else {
                        Toast.makeText(getApplicationContext(),"There are no taxi arround you!",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    taxiMovingKey = false;
                    checkInfoWindowClick = false;
                    // Use the Builder class for convenient dialog construction
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setMessage("Would you like to cancel this call?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                    if(taxiNewLocationToSaved.getLatitude()!=0.000000&&taxiNewLocationToSaved.getLongitude()!=0.000000) {
                                        new UpdateDriverPosition(taxiNewLocationToSaved.getLatitude(), taxiNewLocationToSaved.getLongitude()).execute();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.cancel();
                                }
                            });
                    // Create the AlertDialog object and return it
                    builder.show();
                }
            }
        });

        if (checkInfoWindowClick==false) {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    //return null;
                    Log.i("pikachuuuuuu", "saasdasdsada");
                    return prepareInfoView(marker);
                }
            });
        }

//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                Location temp = new Location("temp");
//                temp.setLatitude(latLng.latitude);
//                temp.setLongitude(latLng.longitude);
//                float distance = 0;
//                try {
//                    distance = getDistance(myLocation,temp);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Log.i("rrrrrrrrrr",distance +"km");
//            }
//        });
    }

    public String[] getDistanceDuration(Location loc1, Location loc2) throws InterruptedException {

        String[] list = new String[2];
        Distance thread = new Distance(loc1,loc2);
        Thread t = new Thread(thread); t.start(); t.join(); list[0] = thread.getDistanceValue(); list[1] = thread.getDurationValue();


        Log.i("ffffffffffkkkkkk",""+ list[0] +"     "+list[1]);
        return list;
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

    public void setGoogleApiForLocation(){
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

    public class Distance implements Runnable {
        private volatile String distance ;
        private volatile String duration ;
        String url = "";

        public Distance(Location loc1 ,Location loc2){
            this.url = getMapsApiDirectionsUrlWithTwoPoints(loc1,loc2);
        }

        @Override
        public void run() {
            try  {
                //Log.i("pppppppppppppp","ppppppppppppppppp");
                String data = "";
                JSONObject jObject;
                List<List<HashMap<String, String>>> routes = null;
                try {
                    HttpConnection http = new HttpConnection();
                    data = http.readUrl(url);
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                }
                try {
                    jObject = new JSONObject(data);
                    PathJSONParser parser = new PathJSONParser();
                    routes = parser.parse(jObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if(routes.size()<1){
                    Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                    return;
                }

// Traversing through all the routes
                for(int i=0;i<routes.size();i++) {

// Fetching i-th route
                    List<HashMap<String, String>> path = routes.get(i);

// Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        if (j == 0) { // Get distance from the list
                            distance = (String) point.get("distance");
                            continue;
                        } else if (j == 1) { // Get duration from the list
                            duration = (String) point.get("duration");
                            continue;
                        }
                    }
                }

                Log.i("ffbbbbbbbbbbdistance",""+distance);
                Log.i("ffbbbbbbbbbbduration",""+duration);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getDistanceValue() {
            distance = distance.substring(0, distance.indexOf(" "));
            //in km
            return distance;
        }
        public String getDurationValue() {
            //in minute
            duration = duration.substring(0, duration.indexOf(" "));
            return duration;
        }
    }

    private String getMapsApiDirectionsUrl() {
//        String waypoints = "waypoints=optimize:true|"
//                + myLocation.getLatitude() + "," + myLocation.getLongitude()
//                + "||" + latitude + ","
//                + longitude;

        //String sensor = "sensor=false";
        //String params = waypoints + "&" + sensor;
        String output = "json";
        String params = "origin="+myLocation.getLatitude()+","+myLocation.getLongitude()+"&destination="+myDestination.getLatitude()+","+myDestination.getLongitude();
        String serverAPIKey = "AIzaSyCQFAyfzpvU7j5Kz7kyXiPEBRhe4_61kgo";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params + "&key=" + serverAPIKey;
        Log.i("assadasdasda",url);
        return url;
    }

    private String getMapsApiDirectionsUrlWithTwoPoints(Location begin, Location end) {
        String output = "json";
        String params = "origin="+begin.getLatitude()+","+begin.getLongitude()+"&destination="+end.getLatitude()+","+end.getLongitude();
        String serverAPIKey = "AIzaSyCQFAyfzpvU7j5Kz7kyXiPEBRhe4_61kgo";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params + "&key=" + serverAPIKey;
        Log.i("assadasdasda",url);
        return url;
    }

//    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(-34, 151)).title("Marker"));
//    }

    private void handleNewLocation(Location location) {

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));

        myMarker = myLocationMarker(location);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

        if (checkPickup==false) {
            myLocation.setLatitude(currentLatitude);
            myLocation.setLongitude(currentLongitude);
        }
    }

    private void getTaxi(){
        for (int i = 0; i<taxiList.size();i++){
            TaxiDriver taxi = taxiList.get(i);
            taxiMarker(taxi.getLatitude(),taxi.getLongitude());
        }
    }

    private View prepareInfoView(Marker marker){
        // Getting view from the layout file info_window_layout
        View v = getLayoutInflater().inflate(R.layout.infowindow_layout, null);

        // Returning the view containing InfoWindow contents
        return v;
    }

    private class MyTask extends AsyncTask<Object, Object, Boolean> {

        private String email;
        private String password;

        public MyTask(){
            //this.email = email;
            //this.password = password;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                taxiList.clear();
                PassengerDataAccess pass = new PassengerDataAccess();
                final ResultSet result = pass.selectTaxi();
                while (result.next()) {
                    //Log.i("pikachuu",""+result.getString("name"));
                    //Log.i("pikachuu2",""+result.getString("latitude"));
                    //Log.i("pikachuu3",""+result.getString("longitude"));
                    TaxiDriver one_taxi = new TaxiDriver(result.getString("id"),result.getString("email"),result.getString("phone"),result.getString("name"),result.getDouble("latitude"),result.getDouble("longitude"),result.getString("car_type"));
                    taxiList.add(one_taxi);
                    //taxiMarker(name,latitude,longitude);
                }

                //when user move to other location
                if (myPassenger!=null&&myLocation.getLatitude()!=0.000000&&myLocation.getLongitude()!=0.000000) {
                    if (myLocation.getLatitude()!=old_my_lat||myLocation.getLongitude()!=old_my_long) {
                        old_my_lat = myLocation.getLatitude();
                        old_my_long = myLocation.getLongitude();
                        pass.updateLocation(myPassenger.getId(), myLocation.getLatitude(), myLocation.getLongitude());
                    }
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
            mMap.clear();
            myLocationMarker(myLocation);
            if (taxiList.size()>0){
                getTaxi();
            }
        }

    }

    private Marker myLocationMarker(Location location) {
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_passenger))
                .position(new LatLng(location.getLatitude(),location.getLongitude()));
        Marker myLocMarker = mMap.addMarker(options);
        myLocMarker.showInfoWindow();

        return myLocMarker;
    }

    private Marker myLocationMarker2(LatLng location) {
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(taxi))
                .position(new LatLng(location.latitude,location.longitude));
        Marker myLocMarker = mMap.addMarker(options);

        return myLocMarker;
    }

    private Marker taxiMarker(double latitude, double longitude) {
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(taxi))
                .position(new LatLng(latitude,longitude));
        Marker myLocMarker = mMap.addMarker(options);

        Log.i("taxilocationsss","aaaaaaaaaaaaaaa");
        Log.i("debug",""+myLocMarker);

        return myLocMarker;
    }

    /*private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }*/

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
        }
        else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

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
        if (keyOpenLocation) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            keyOpenLocation = false;
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

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private class ReadTask extends AsyncTask<String, Void, String> {

        private int key;
        private int keyMove;
        private boolean isToDes;

        public ReadTask(int key, int keyMove, boolean isToDes){
            this.key = key;
            this.keyMove = keyMove;
            this.isToDes = isToDes;
        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                //Log.d("Background Task", e.toString());
            }
            //Log.i("nnnnnnnnnnnnnn",data);
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask(key,keyMove,isToDes).execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        private int key;
        private int keyMove;
        private boolean isToDes;

        public ParserTask(int key, int keyMove, boolean isToDes){
            this.key = key;
            this.keyMove = keyMove;
            this.isToDes = isToDes;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                //Log.i("nnmmmmmmmmmm",jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.i("routeeeeeeeee",""+routes);
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;

            if(routes.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<routes.size();i++){
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = routes.get(i);

                // Fetching all the points in i-th route
                for(int j=2;j <path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    //Log.i("newwwwwwwwww",""+point);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                //Log.i("pointtttttttt",""+points);

                //Log.i("ssssssssssssssss    ",""+key);
                //Toast.makeText(getApplicationContext(),"key: "+key,Toast.LENGTH_LONG).show();

                //key = 1, make taxi moving
                if (key==1) {
                    //Log.i("destination",""+myDestination);
                    if(myDestination.getLatitude()!=0.000000&&myDestination.getLongitude()!=0.000000) {
                        String url = getMapsApiDirectionsUrl();
                        ReadTask downloadTask = new ReadTask(0,0,false);
                        downloadTask.execute(url);
                    }
                    taxiMarkerWayList.addAll(points);
                    mMap.clear();
                    if (!isToDes) {
                        myMarker = myLocationMarker(myLocation);
                    }
                    Handler handler1 = new Handler();
                    taxiRuning = myLocationMarker2(taxiMarkerWayList.get(0));
                    for (int j = 1; j<taxiMarkerWayList.size() ;j++) {
                        final int final_j = j;
                        handler1.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (taxiMovingKey) {
                                    taxiRuning.remove();
                                    taxiRuning = myLocationMarker2(taxiMarkerWayList.get(final_j));
                                }
                                else {
                                    taxiNewLocationToSaved.setLatitude(taxiMarkerWayList.get(final_j).latitude);
                                    taxiNewLocationToSaved.setLongitude(taxiMarkerWayList.get(final_j).longitude);
                                }
                                if (final_j==taxiMarkerWayList.size()-1){
                                    if (keyMove==1) { //move to passenger
                                        if(myDestination.getLatitude()!=0.000000&&myDestination.getLongitude()!=0.000000) {
                                            new AlertDialog.Builder(MapsActivity.this)
                                                    .setMessage("Would you like to start trip?")
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            mMap.clear();
                                                            taxiMarkerWayList.clear();
                                                            String url = getMapsApiDirectionsUrlWithTwoPoints(myLocation, myDestination);
                                                            ReadTask downloadTask = new ReadTask(1, 0, true);
                                                            downloadTask.execute(url);
                                                        }
                                                    })
                                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // do nothing
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .show();
                                        }
                                    }
                                    else { //move to destination
                                        //add rating
                                        // custom dialog
                                        final Dialog dialog = new Dialog(MapsActivity.this);
                                        dialog.setContentView(R.layout.custom_dialog);
                                        dialog.setTitle("Driver Rating");

                                        final int[] star = {0};
                                        RatingBar rating = (RatingBar) dialog.findViewById(R.id.ratingBar);
                                        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                            @Override
                                            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                                                star[0] = Math.round(ratingBar.getRating());
                                            }
                                        });

                                        Button dialogButton = (Button) dialog.findViewById(R.id.button3);
                                        // if button is clicked, close the custom dialog
                                        dialogButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new UpdateRating(star[0]).execute();
                                                dialog.dismiss();
                                            }
                                        });

                                        dialog.show();
                                    }
                                }
                            }
                        }, 1000*(j+1)); //speed of taxi moving
                    }
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
                //key = 0, draw path
                if (key==0) {
                    mMap.addPolyline(lineOptions);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_PICK_UP_PLACE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                //Log.i("sssss", "Place: " + place.getLatLng());
                mMap.clear();

                LatLng latLng = place.getLatLng();

                //myLocation.setLatitude(latLng.latitude);
                //myLocation.setLongitude(latLng.longitude);

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("CALL TAXI"));
            }
            else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                //Log.i("aaaaa", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }
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
                driver.updateDriverLocation(1, latitude, longitude);
                TripAccess trip = new TripAccess();
                if (trip_id!=""){
                    trip.deleteTrip(trip_id);
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
            Intent refresh = new Intent(MapsActivity.this, MapsActivity.class);
            startActivity(refresh);//Start the same Activity
            finish(); //finish Activity.
        }

    }

    private class UpdateRating extends AsyncTask<Object, Object, Boolean> {

        private int star;

        public UpdateRating(int star) {
            this.star = star;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
            try {

                TripAccess trip = new TripAccess();
                if (trip_id!=""){
                    trip.updateTripRating(trip_id,star);
                    int sum = 0;
                    int size = 0;
                    final ResultSet result = trip.selectAllDriverRating(taxiMoving.getId());
                    while (result.next()) {
                        if (result.getInt("rating")!=0) {
                            sum += result.getInt("rating");
                            size++;
                        }
                    }
                    trip.updateDriverRating(taxiMoving.getId(),sum/size);
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
            Intent refresh = new Intent(MapsActivity.this, MapsActivity.class);
            startActivity(refresh);//Start the same Activity
            finish(); //finish Activity.
        }

    }

    private class GetDriverRating extends AsyncTask<Object, Object, Boolean> {

        int sum = 0;
        int size = 0;
        double star = 0.0;

        @Override
        protected Boolean doInBackground(Object... params) {
            //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
            try {

                TripAccess trip = new TripAccess();
                final ResultSet result = trip.selectAllDriverRating(taxiMoving.getId());
                while (result.next()) {
                    if (result.getInt("rating")!=0) {
                        sum += result.getInt("rating");
                        size++;
                    }
                }
                if (size>0) {
                    star = sum / size;
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
            String star_tail = " Stars";

            if (star<2){
                star_tail = " Star";
            }

            name.setText("DRIVER INFO");
            esti_price.setText("Rating: "+star + star_tail);
            destination.setText("Name: "+taxiMoving.getName());
            esti_time.setText("Phone: "+taxiMoving.getPhone());
            people_size.setText("Time: "+time_taxi+" minutes");
        }

    }

    private class UpdateTrip extends AsyncTask<Object, Object, Boolean> {

        String pass_id;
        String driver_id;
        String price;
        java.sql.Date date;
        String start_address;
        String des_address;
        String car_type;

        public UpdateTrip(String pass_id, String driver_id, String price, java.sql.Date date, String start_address, String car_type) {
            this.pass_id = pass_id;
            this.driver_id = driver_id;
            this.price = price;
            this.date = date;
            this.start_address = start_address;
            this.car_type = car_type;
            this.des_address = "No Infomation";
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
            try {
                //Log.i("qqqqqqqqqq","asfjhweiglweighkjewhgkje");
                int rand = (int) (new Date().getTime()/1000) + (1 + (int)(Math.random() * 1000000));
                trip_id = ""+rand;
                TripAccess trip = new TripAccess();
                if (price=="0"||price==""){
                    price = "No Infomation";
                }
                if(myDestination.getLatitude()!=0.000000&&myDestination.getLongitude()!=0.000000) {
                    des_address = des_update_trip;
                }
                else {
                    des_address = "No Infomation";
                }
                trip.insertNewTrip(trip_id,pass_id,driver_id,price,date,start_address,des_address,car_type);
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

    //neu check permission xong
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
                        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                            displayLocationSettingsRequest(getApplicationContext());
                            keyOpenLocation = true;
                        }
                        else {
                            setGoogleApiForLocation();
                        }
                        setAfterLoadMap();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    private class UpdateDriverCalled extends AsyncTask<Object, Object, Boolean> {

        String driver_id;
        public UpdateDriverCalled(String driver_id){
            this.driver_id = driver_id;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                DriverDataAccess driver = new DriverDataAccess();
                driver.updateCallTaxi(driver_id,myPassenger.getId());
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
}
