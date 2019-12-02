package com.example.catmap;

/**
 * @file MapsActivity
 *
 * Handles all navigational tasks through Google Maps & IndoorAtlas SDKs.
 */

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.IARoute;
import com.indooratlas.android.sdk.IAWayfindingListener;
import com.indooratlas.android.sdk.IAWayfindingRequest;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @brief MapsActivity class.
 *
 * Handles location tracking (indoor and outdoor), destination searching/marking, and indoor wayfinding.
 */
public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {
    private static final int MAX_DIMENSION = 2048;

    /** GoogleMaps object. */
    private GoogleMap mMap;
    /** JSON object array for database. */
    private JSONArray jsonArray;
    /** Map with room names as keys and their stored JSON objects as values. */
    private Map<String, JSONObject> places = new LinkedHashMap<>();

    /** Polyline list for pathing. */
    private List<Polyline> mPolylines = new ArrayList<>();
    /** Destination coordinates. Defaults to null */
    private LatLng mDestination = null;
    /** Destination map marker. */
    private Marker mDestinationMarker;
    /** User location marker. */
    private Marker mHeadingMarker;
    /** Accuracy radius. Displayed around user's location marker. */
    private Circle mCircle;

    /** Target for fetching bitmaps for floor plans. */
    private Target mTarget;

    /** Checks if map camera needs updating. */
    private boolean mCameraUpdate = true;

    /** Main IndoorAtlas object. Implicitly handles location tracking and related tasks. */
    private IALocationManager mIALocationManager;
    /** Current region's floor plan. Defaults to null. */
    private IARegion mFloorPlan = null;
    /** Current floor number. */
    private int mFloorLevel;
    /** Floor plan map overlay. */
    private GroundOverlay mGroundOverlay = null;
    /** IndoorAtlas-calculated route to destination. */
    private IARoute mRoute;

    /** IndoorAtlas user location listener. */
    private IALocationListener mListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            // No map displayed, so exit.
            if (mMap == null) return;

            // Convert IA location data into more flexible LatLng format.
            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            // Fetch current floor level. If user is on a new floor from prev., then update map accordingly.
            final int newFloorLevel = location.getFloorLevel();
            if (mFloorLevel != newFloorLevel)
                updateRoute();
            mFloorLevel = newFloorLevel;

            // Request map updates and update camera if needed.
            updateLocation(latLng, location.getAccuracy(), location.getBearing());
            if (mCameraUpdate) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5f));
                mCameraUpdate = false;
            }
        }
    };

    /** IndoorAtlas region listener. Detects indoor-outdoor transitions. */
    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(@NotNull IARegion region) {
            // Check for region type. If region has a floor plan, run updates.
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                // If no current floor plans displayed or new floor plan has been fetched, update map.
                if (mGroundOverlay == null || !region.equals(mFloorPlan)) {
                    // Request camera update.
                    mCameraUpdate = true;

                    // If existing floor plan, remove it.
                    if (mGroundOverlay != null) {
                        mGroundOverlay.remove();
                        mGroundOverlay = null;
                    }

                    // Set current floor plan to user's current region.
                    mFloorPlan = region;
                    // Fetch floor plan.
                    final IAFloorPlan floorPlan = region.getFloorPlan();
                    // Set new Target to load floor plans onto map.
                    mTarget = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            // If valid region floor plan, start map updates.
                            if (mFloorPlan != null && floorPlan.getId().equals(mFloorPlan.getId())) {
                                // Remove any existing ground overlays.
                                if (mGroundOverlay != null)
                                    mGroundOverlay.remove();

                                if (mMap != null) {
                                    // Grab bitmap information and find geographic center.
                                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                                    IALatLng latLng = floorPlan.getCenter();
                                    LatLng center = new LatLng(latLng.latitude, latLng.longitude);
                                    // Set ground overlay options for floor plan.
                                    GroundOverlayOptions floorPlanOverlay = new GroundOverlayOptions()
                                            .image(bitmapDescriptor)
                                            .zIndex(0.0f)
                                            .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                                            .bearing(floorPlan.getBearing());

                                    // Set overlay onto map.
                                    mGroundOverlay = mMap.addGroundOverlay(floorPlanOverlay);
                                }
                            }
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            mFloorPlan = null;
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            // Empty.
                        }
                    };

                    // Make Picasso request.
                    RequestCreator request = Picasso.with(MapsActivity.this).load(floorPlan.getUrl());
                    int bmWidth = floorPlan.getBitmapWidth();
                    int bmHeight = floorPlan.getBitmapHeight();

                    if (bmWidth > MAX_DIMENSION) request.resize(MAX_DIMENSION, 0);
                    else if (bmHeight > MAX_DIMENSION) request.resize(0, MAX_DIMENSION);

                    request.into(mTarget);
                } else {
                    mGroundOverlay.setTransparency(0.0f);
                }
            }
        }

        @Override
        public void onExitRegion(@NotNull IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                // If existing floor plan, set it to 50% transparency.
                if (mGroundOverlay != null)
                    mGroundOverlay.setTransparency(0.5f);
            }
        }
    };

    /** IndoorAtlas wayfinding destination. */
    private IAWayfindingRequest mWayfindingDestination;
    /** IndoorAtlas wayfinding listener for navigation to destination. */
    private IAWayfindingListener mWayfindingListener = new IAWayfindingListener() {
        @Override
        public void onWayfindingUpdate(@NotNull IARoute route) {
            // Update global route var.
            mRoute = route;

            // Check if user has approximately arrived at their destination.
            boolean hasArrived;
            if (route.getLegs().size() == 0) {
                hasArrived = false;
            } else {
                final double FINISH_THRESHOLD_METERS = 8.0;
                double routeLength = 0;
                for (IARoute.Leg leg : route.getLegs())
                    routeLength += leg.getLength();

                hasArrived = routeLength < FINISH_THRESHOLD_METERS;
            }

            // If user has arrived, remove route from map and stop listening for wayfinding updates.
            if (hasArrived) {
                mRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();
            }

            // Update map with new route.
            updateRoute();
        }
    };

    /**
     * @brief Initalizes map and navigational tasks on activity start.
     *
     * Starts Google Maps & IndoorAtlas processes, loads local copy of room database, and initializes
     * UI elements such as the search bar and directions button.
     *
     * @param savedInstanceState Passed Bundle object containing the state.
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize IndoorAtlas location manager.
        mIALocationManager = IALocationManager.create(this);
        mIALocationManager.registerRegionListener(mRegionListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map))
                .getMapAsync(this);

        // Start Google Maps location tracking. Request permissions.
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

        // Initialize array of room objects.
        ArrayList<String> placeNames = new ArrayList<String>();
        try {
            // Load JSON objects from database file.
            JSONObject jsonObject = new JSONObject(loadJSONFromAsset());
            jsonArray = jsonObject.getJSONArray("places");

            // For each JSON object, put name and object as key and value into map.
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject room = jsonArray.getJSONObject(i);

                places.put(room.getString("name"), room);
                placeNames.add(room.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Initialize autocomplete search bar.
        final androidx.appcompat.widget.SearchView locationSearch = findViewById(R.id.search);
        androidx.appcompat.widget.SearchView.SearchAutoComplete searchAutoComplete = locationSearch.findViewById(androidx.appcompat.R.id.search_src_text);
        // Set adapter to user room data.
        searchAutoComplete.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, placeNames));

        // Set event listener for search autocomplete items.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String query = (String) adapterView.getItemAtPosition(i);
                locationSearch.setQuery(query, true);
            }
        });

        // Set event listener for search bar.
        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Get JSON object associated with entered query.
                JSONObject room = places.get(query);
                // Default coordinates.
                double latitude = 0;
                double longitude = 0;

                // If successful query, get saved coordinates.
                if (room != null) {
                    try {
                        latitude = room.getDouble("latitude");
                        longitude = room.getDouble("longitude");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // Set destination to saved coordinates (default or queried).
                mDestination = new LatLng(latitude, longitude);

                // Update camera and place marker.
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mDestination));
                if (mDestinationMarker == null)
                    mDestinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(mDestination)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                else
                    mDestinationMarker.setPosition(mDestination);

                // Remove all route polylines on map.
                for (Polyline pl : mPolylines)
                    pl.remove();
                mPolylines.clear();

                // Remove any existing wayfinding tasks.
                mRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();

                // Quick call to updateRoute() to clean map.
                updateRoute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        Button directions = findViewById(R.id.button);
        directions.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // On user click, get directions to existing destination.
                if (mDestination != null)
                    getDirections(mDestination);
            }
        });
    }

    /**
     * @brief Initializes Google Maps API and map overlay.
     *
     * Starts Google Maps processes and generates actual map element.
     *
     * @param googleMap Google Maps map object.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Disable Google Maps user location marker and camera recenter button.
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // If user clicks on map, mark location and start IndoorAtlas wayfinding to destination.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng pos) {
                if (mMap != null) {
                    mWayfindingDestination = new IAWayfindingRequest.Builder()
                        .withFloor(mFloorLevel)
                        .withLatitude(pos.latitude)
                        .withLongitude(pos.longitude)
                        .build();

                    mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);

                    if (mDestinationMarker == null)
                        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    else
                        mDestinationMarker.setPosition(pos);
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        // Empty.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Empty.
    }
    @Override
    public void onProviderEnabled(String provider) {
        // Empty.
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Empty.
    }

    /**
     * On app resume, continue listening for location and wayfinding updates.
     */
    @Override
    protected void onResume() {
        super.onResume();

        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);
        mIALocationManager.registerRegionListener(mRegionListener);

        if (mWayfindingDestination != null)
            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
    }

    /**
     * On app pause, stop listening for location and wayfinding updates.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (mIALocationManager != null) {
            mIALocationManager.removeLocationUpdates(mListener);
            mIALocationManager.registerRegionListener(mRegionListener);

            if (mWayfindingDestination != null)
                mIALocationManager.removeWayfindingUpdates();
        }
    }

    /**
     * On app destroy, remove Google Maps fragment and destroy location manager.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        MapFragment mapFragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            this.getFragmentManager().beginTransaction().remove(mapFragment).commit();

        mIALocationManager.destroy();
    }

    /**
     * @brief Update user location on map.
     *
     * Given user's coordinates, place user location marker on map with appropriate accuracy circle
     * and bearing to show user direction.
     *
     * @param pos User's position as coordinates.
     * @param radius Calculated position accuracy.
     * @param bearing User's heading direction.
     */
    private void updateLocation(LatLng pos, double radius, double bearing) {
        // If no circle, generate new circle and set user's location marker.
        if (mCircle == null) {
            if (mMap != null) {
                mCircle = mMap.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(radius)
                    .fillColor(0x201681FB)
                    .strokeColor(0x500A78DD)
                    .zIndex(1.0f)
                    .visible(true)
                    .strokeWidth(5.0f));
                mHeadingMarker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                    .anchor(0.5f, 0.5f)
                    .rotation((float) bearing)
                    .flat(true));
            }
        // Else, update the existing circle and marker.
        } else {
            mCircle.setCenter(pos);
            mCircle.setRadius(radius);
            mHeadingMarker.setPosition(pos);
            mHeadingMarker.setRotation((float) bearing);
        }
    }

    /**
     * @brief Update route to destination.
     *
     * Update polylines from current route and represent the change on the map to show directions to
     * the user.
     */
    private void updateRoute() {
        // Remove all current polylines.
        for (Polyline pl : mPolylines)
            pl.remove();
        mPolylines.clear();

        // If no route, exit function.
        if (mRoute == null)
            return;

        // For each leg of the route, add a new polyline.
        for (IARoute.Leg leg : mRoute.getLegs()) {
            if (leg.getEdgeIndex() == null)
                continue;

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(new LatLng(leg.getBegin().getLatitude(), leg.getBegin().getLongitude()))
                    .add(new LatLng(leg.getEnd().getLatitude(), leg.getEnd().getLongitude()));

            if (leg.getBegin().getFloor() == mFloorLevel && leg.getEnd().getFloor() == mFloorLevel)
                polylineOptions.color(0xFF0000FF);
            else
                polylineOptions.color(0x300000FF);

            // Add polyline to map.
            mPolylines.add(mMap.addPolyline(polylineOptions));
        }
    }

    /**
     * @brief Fetch directions to destination.
     *
     * Given a destination, update the camera and start making IndoorAtlas wayfinding requests.
     *
     * @param location Wayfinding destination as coordinates.
     */
    public void getDirections(LatLng location) {
        // Update camera and initialize wayfinder.
        mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
        mWayfindingDestination = new IAWayfindingRequest.Builder()
                .withFloor(mFloorLevel)
                .withLatitude(location.latitude)
                .withLongitude(location.longitude)
                .build();

        // Start wayfinding updates.
       mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
    }

    /**
     * @brief Read JSON data from database.
     *
     * Read JSON data from room database and load into JSON string.
     *
     * @return JSON object string.
     */
    public String loadJSONFromAsset() {
        try {
            // Initialize input stream object and open database file.
            InputStream ins = this.getAssets().open("places.json");
            byte[] buffer = new byte[ins.available()];

            // Read from file and close.
            ins.read(buffer);
            ins.close();

            // Return the resulting JSON string.
            String json = new String(buffer, "UTF-8");
            return json;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}