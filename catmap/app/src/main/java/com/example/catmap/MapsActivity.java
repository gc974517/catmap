package com.example.catmap;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {
    private static final int MAX_DIMENSION = 2048;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient mPlacesClient;
    private List<AutocompletePrediction> autocompletePredictions;

    private JSONArray jsonArray;
    private JSONObject jsonPlace;
    private Map<String, JSONObject> places = new LinkedHashMap<>();
    private ArrayAdapter<String> adapter;

    private Location mLocation;
    private LocationCallback mLocationCallback;
    private List<Polyline> mPolylines = new ArrayList<>();
    private LatLng mDestination = null;
    private Marker mDestinationMarker;
    private Marker mHeadingMarker;
    private Circle mCircle;

    private Target mTarget;

    private boolean mCameraUpdate = true;
    private boolean mIndoors = false;

    private IALocationManager mIALocationManager;
    private IARegion mVenue = null;
    private IARegion mFloorPlan = null;
    private int mFloorLevel;
    private GroundOverlay mGroundOverlay = null;
    private IARoute mRoute;

    LayoutInflater inflater = null;
    private TextView textViewTitle;
    private RelativeLayout rl_custominfo;

    private IALocationListener mListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            if (mMap == null) return;

            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            final int newFloorLevel = location.getFloorLevel();
            if (mFloorLevel != newFloorLevel)
                updateRoute();
            mFloorLevel = newFloorLevel;

            updateLocation(latLng, location.getAccuracy(), location.getBearing());
            if (mCameraUpdate) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5f));
                mCameraUpdate = false;
            }
        }
    };

    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(@NotNull IARegion region) {
            if (region.getType() == IARegion.TYPE_VENUE) {
                mVenue = region;
                mIndoors = false;
            } else if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                if (mGroundOverlay == null || !region.equals(mFloorPlan)) {
                    mCameraUpdate = true;

                    if (mGroundOverlay != null) {
                        mGroundOverlay.remove();
                        mGroundOverlay = null;
                    }

                    mFloorPlan = region;
                    final IAFloorPlan floorPlan = region.getFloorPlan();
                    mTarget = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            if (mFloorPlan != null && floorPlan.getId().equals(mFloorPlan.getId())) {
                                if (mGroundOverlay != null)
                                    mGroundOverlay.remove();

                                if (mMap != null) {
                                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                                    IALatLng latLng = floorPlan.getCenter();
                                    LatLng center = new LatLng(latLng.latitude, latLng.longitude);
                                    GroundOverlayOptions floorPlanOverlay = new GroundOverlayOptions()
                                            .image(bitmapDescriptor)
                                            .zIndex(0.0f)
                                            .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                                            .bearing(floorPlan.getBearing());

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

                    RequestCreator request = Picasso.with(MapsActivity.this).load(floorPlan.getUrl());
                    int bmWidth = floorPlan.getBitmapWidth();
                    int bmHeight = floorPlan.getBitmapHeight();

                    if (bmWidth > MAX_DIMENSION) request.resize(MAX_DIMENSION, 0);
                    else if (bmHeight > MAX_DIMENSION) request.resize(0, MAX_DIMENSION);

                    request.into(mTarget);
                } else {
                    mGroundOverlay.setTransparency(0.0f);
                }

                mIndoors = true;
            }
        }

        @Override
        public void onExitRegion(@NotNull IARegion region) {
            if (region.getType() == IARegion.TYPE_VENUE) {
                mVenue = region;
                mIndoors = false;
            } else if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                if (mGroundOverlay != null)
                    mGroundOverlay.setTransparency(0.5f);

                mIndoors = true;
            }
        }
    };

    private IAWayfindingRequest mWayfindingDestination;
    private IAWayfindingListener mWayfindingListener = new IAWayfindingListener() {
        @Override
        public void onWayfindingUpdate(@NotNull IARoute route) {
            mRoute = route;

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

            if (hasArrived) {
                mRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();
            }

            updateRoute();
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIALocationManager = IALocationManager.create(this);
        mIALocationManager.registerRegionListener(mRegionListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map))
                .getMapAsync(this);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

        ArrayList<String> placeNames = new ArrayList<String>();
        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromAsset());
            jsonArray = jsonObject.getJSONArray("places");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject room = jsonArray.getJSONObject(i);

                places.put(room.getString("name"), room);
                placeNames.add(room.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (places.get("Room 1") == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "large sad", Toast.LENGTH_SHORT);
            toast.show();
        }

        final androidx.appcompat.widget.SearchView locationSearch = findViewById(R.id.search);
        androidx.appcompat.widget.SearchView.SearchAutoComplete searchAutoComplete = locationSearch.findViewById(androidx.appcompat.R.id.search_src_text);
        String arr[] = {"help", "me"};
        searchAutoComplete.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, placeNames));

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String query = (String) adapterView.getItemAtPosition(i);
                locationSearch.setQuery(query, true);
            }
        });

        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                JSONObject room = places.get(query);
                double latitude = 0;
                double longitude = 0;

                if (room != null) {
                    try {
                        latitude = room.getDouble("latitude");
                        longitude = room.getDouble("longitude");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mDestination = new LatLng(latitude, longitude);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(mDestination));
                if (mDestinationMarker == null)
                    mDestinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(mDestination)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                else
                    mDestinationMarker.setPosition(mDestination);

                for (Polyline pl : mPolylines)
                    pl.remove();
                mPolylines.clear();

                mRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();

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
                if (mDestination != null)
                    getDirections(mDestination);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

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

    @Override
    protected void onResume() {
        super.onResume();

        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);
        mIALocationManager.registerRegionListener(mRegionListener);

        if (mWayfindingDestination != null)
            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MapFragment mapFragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            this.getFragmentManager().beginTransaction().remove(mapFragment).commit();

        mIALocationManager.destroy();
    }

    private void updateLocation(LatLng pos, double radius, double bearing) {
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
        } else {
            mCircle.setCenter(pos);
            mCircle.setRadius(radius);
            mHeadingMarker.setPosition(pos);
            mHeadingMarker.setRotation((float) bearing);
        }
    }

    private void updateRoute() {
        for (Polyline pl : mPolylines)
            pl.remove();
        mPolylines.clear();

        if (mRoute == null)
            return;

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

            mPolylines.add(mMap.addPolyline(polylineOptions));
        }
    }

    public void getDirections(LatLng location) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
        mWayfindingDestination = new IAWayfindingRequest.Builder()
                .withFloor(mFloorLevel)
                .withLatitude(location.latitude)
                .withLongitude(location.longitude)
                .build();

       mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
    }

    public String loadJSONFromAsset() {
        try {
            InputStream ins = this.getAssets().open("places.json");
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            String json = new String(buffer, "UTF-8");
            return json;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}


//39.326, -82.10694 lab 107