package com.example.catmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.content.Intent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;

import java.util.ArrayList;

/**
 * @file MainActivity
 * Handles initializing steps for app upon startup.
 */

/**
 * @brief MainActivity class.
 * Main activity that is called at the start of the app. Fetches UI, handles permissions and makes
 * call to Maps activity, which handles all navigational (GoogleMaps / IndoorAtlas) code.
 */
public class MainActivity extends AppCompatActivity {
    private final int CODE_PERMISSIONS = 1;

    /**
     * @brief Initializes app on launch.
     * Calls layout and checks for permissions from user's device. Starts MapsActivity.
     *
     * @param savedInstanceState Passed Bundle object containing the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] neededPermissions = {
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION //May need to be ACCESS_FINE_LOCATION in order to run. The permission seems to be bugged.
        };
        ActivityCompat.requestPermissions(this, neededPermissions, CODE_PERMISSIONS);

        startActivity(new Intent(this, MapsActivity.class));
    }

    /**
     * @brief Request permissions from user.
     *
     * @param requestCode Arbitrary int value (default 1).
     * @param permissions Array of permissions to ask user for.
     * @param grantResults Array of permissions checks as boolean ints (0 or 1).
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
