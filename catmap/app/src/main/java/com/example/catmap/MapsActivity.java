package com.example.catmap;

import androidx.fragment.app.FragmentActivity;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;

public class MapsActivity extends FragmentActivity implements IALocationListener, OnMapReadyCallback {
    private IALocationManager mIALocationManager;
    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mIALocationManager = IALocationManager.create(this);
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
    }

    // Declare IA location listener, define update operations.
    public void onLocationChanged(IALocation location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (mMarker == null) {
            if (mMap != null) {
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(200.0f)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
            }
        } else {
            mMarker.setPosition(latLng);
        }
    }

    // On resuming the app, begin requesting location updates.
    @Override
    protected void onResume() {
        super.onResume();
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), this);
    }

    // Stop location requests on app pause.
    @Override
    protected void onPause() {
        super.onPause();
        if (mIALocationManager != null)
            mIALocationManager.removeLocationUpdates(this);
    }

    public void onDestoy() {
        mIALocationManager.destroy();

        MapFragment mapFragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            this.getFragmentManager().beginTransaction().remove(mapFragment).commit();

        super.onDestroy();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // N/A
    }
}