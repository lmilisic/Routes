package routes.example.routes;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import okhttp3.Route;

public class Rute<distance1> extends FragmentActivity implements OnMapReadyCallback {
    private Double lng;
    private Double lat;
    private Long vrijeme;
    private Double metri;
    private Double brzina;
    private Long prevtimestamp;
    private Long timestamp;
    private GoogleMap mMap;
    private Double r,g,b;
    private Button buttonend;
    private Long rp,gp,bp;
    private Double distance1;
    private int srednja = 0;
    private int vrijeme1 = 0;
    private Integer distance3 = 0;
    private Integer vrijeme3 = 0;
    private TextView distancatxt;
    private TextView vrijemetxt;
    private TextView srednjatxt;
    private TextView najbrzinatxt;
    private Double najbrzina = -100.0;
    public Boolean firstrun = true;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference().child ("Maps").child (PostDetails.Key);
    LatLng prevmarker = new LatLng(0,0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_rute);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ()
                .findFragmentById (R.id.map);
        mapFragment.getMapAsync (this);
        distance1 = 0.0;
        distancatxt=findViewById (R.id.distanca);
        vrijemetxt=findViewById (R.id.vrijemetx);
        srednjatxt=findViewById (R.id.srednjatx);
    }

    @Override
    protected void onStart() {
        super.onStart ();
        myRef.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren ()){
                    lng= (Double) ds.child("lng").getValue();
                    lat= (Double) ds.child("lat").getValue();
                    LatLng latLng = new LatLng (lat, lng);
                    if (firstrun){
                        prevmarker = latLng;
                        firstrun = false;
                        prevtimestamp= (Long) ds.child("timestamp").getValue();
                    }
                    timestamp = (Long) ds.child("timestamp").getValue();
                    vrijeme = (timestamp-prevtimestamp)/1000;
                    final int R = 6371; // Radius of the earth
                    double lat1 = prevmarker.latitude;
                    double lng1 = prevmarker.longitude;
                    double latDistance = Math.toRadians(lat - lat1);
                    double lonDistance = Math.toRadians(lng - lng1);
                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double distance = R * c * 1000; // convert to meters

                    distance = Math.pow(distance, 2);
                    metri = Math.sqrt(distance);
                    brzina = (metri/vrijeme)*3.6;
                    b = 0.0;
                    r = 255.0;
                    g = 255.0;
                    if (0<=brzina && brzina<=10){
                        g = (brzina/10)*255;
                    }
                    if (11<=brzina && brzina<=50){
                        r = Math.abs((((brzina-11)/40)-1)*255);
                    }
                    if (10<brzina && brzina<11){
                        r=255.0;
                        g=255.0;
                    }
                    rp = Math.round(r);
                    gp = Math.round(g);
                    bp = Math.round(b);
                    Log.d ("BRZINA", brzina+" "+metri+" "+vrijeme+" "+rp+" "+gp+" "+bp+" "+timestamp+" "+prevtimestamp);
                    String hex = String.format("#%02x%02x%02x", rp, gp, bp);
                        Log.d ("BRZINA", brzina+" "+metri+" "+vrijeme+" "+r+" "+g+" "+b+" "+timestamp+" "+prevtimestamp);
                    Polyline line = mMap.addPolyline(new PolylineOptions ()
                            .add(latLng,prevmarker)
                            .width(5)
                            .color(Color.parseColor (hex)));
                    prevmarker=latLng;
                    prevtimestamp=timestamp;
                    distance1 = distance1 + metri;
                    vrijeme1 = (int) (vrijeme1 + vrijeme);
                    int distance2 = (int) Math.round(distance1);
                    distance3 = distance2;
                    double vrijeme2 = (double) (vrijeme1 / 60);
                    double vrijeme3 = vrijeme2 / 60;
                    double scale = Math.pow(10, 2);
                    double vrijeme4 = Math.round(vrijeme3 * scale) / scale;

                    double distance4 = (double) distance3 / 1000;
                    double scale1 = Math.pow(10, 2);
                    double distance5 = Math.round(distance4 * scale1) / scale1;
                    distancatxt.setText(distance5+ " " + "km");
                    vrijemetxt.setText(vrijeme4 + " " + "h");
                    if (vrijeme1 != 0) {
                        double srednja1 = (double) distance5 / vrijeme4;
                        double scale2 = Math.pow(10, 2);
                        double srednja2 = Math.round(srednja1 * scale2) / scale2;
                        srednjatxt.setText(srednja2 + " " + "km/h");
                    }
                    Log.d("naj", najbrzina + " " + brzina);
                    if (najbrzina<brzina && !(brzina > 70)){
                        najbrzina=brzina;
                    }
                    Log.d("vrijeme3", vrijeme1 + " " + vrijeme3);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        buttonend=findViewById (R.id.buttonend);
        buttonend.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Rute.this, Main2Activity.class);
                startActivity(intent);
                finish();
            }
        });
        Log.d ("DISTANCA", String.valueOf(distance3));

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }

}
