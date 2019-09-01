package routes.example.routes;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class GoogleMap extends FragmentActivity implements OnMapReadyCallback {
    private LocationRequest mLocationRequest;
    public Button end;
    private FusedLocationProviderClient mFusedLocationClient;
    public Boolean firstrun;
    LatLng prevmarker = new LatLng(0,0);
    private long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
    private long FASTEST_INTERVAL = 1000; /* 2 sec */
    private com.google.android.gms.maps.GoogleMap mMap;
    private LocationCallback locationCallback;
    private DatabaseReference mDatabase;
    public Double lat;
    public Double lng;
    private Button button_begin, button_details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_google_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ()
                .findFragmentById (R.id.map);
        mapFragment.getMapAsync (this);
        startLocationUpdates ();
        firstrun = true;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        end = findViewById (R.id.endbutton);
        button_begin = findViewById (R.id.buttonbegin);
        button_details = findViewById (R.id.buttondetails);
        end.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Lock lock = new Lock(1);
                mDatabase.child("Lock").child(PostDetails.Key).setValue(lock);
                Intent intent = new Intent (GoogleMap.this,Main2Activity.class);
                startActivity (intent);
                finish();
                stopLocationUpdates();
            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest ();
        mLocationRequest.setPriority (LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval (UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval (FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder ();
        builder.addLocationRequest (mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build ();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient (this);
        settingsClient.checkLocationSettings (locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (checkSelfPermission (Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission (Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient (this).requestLocationUpdates (mLocationRequest, new LocationCallback () {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged (locationResult.getLastLocation ());
                    }
                },
                Looper.myLooper ());
    }

    public void onLocationChanged(Location location) {

        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString (location.getLatitude ()) + "," +
                Double.toString (location.getLongitude ());
        Toast.makeText (this, msg, Toast.LENGTH_SHORT).show ();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng (location.getLatitude (), location.getLongitude ());
        if (firstrun){
            prevmarker = latLng;
            firstrun = false;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance ();
        DatabaseReference myRef = database.getReference ("Maps").child(PostDetails.Key).push();
        String key = myRef.getKey ();
        Long tsLong = System.currentTimeMillis();
        Long ts = tsLong;
        lng=location.getLongitude ();
        lat=location.getLatitude ();
        Map map = new Map(lng,lat,ts);
        map.setPostKey (key);
        myRef.setValue (map);
        Polyline line = mMap.addPolyline(new PolylineOptions ()
                .add(latLng,prevmarker)
                .width(20)
                .color(Color.GREEN));
        prevmarker=latLng;
    }

    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap googleMap) {
        mMap = googleMap;
        if (checkPermissions ()) {
            if (checkSelfPermission (Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission (Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled (true);
        }
    }
    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                Integer.parseInt (Manifest.permission.ACCESS_FINE_LOCATION));
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
