package com.example.catmap;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
<<<<<<< Updated upstream
import android.widget.ImageView;
=======
import android.widget.SearchView;
>>>>>>> Stashed changes

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
<<<<<<< Updated upstream

=======
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.indooratlas.android.sdk.IAGeofence;
import com.indooratlas.android.sdk.IAGeofence.Builder;
>>>>>>> Stashed changes
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {
    private static final int MAX_DIMENSION = 2048;

    private GoogleMap mMap;
<<<<<<< Updated upstream
    private Marker mMarker;

=======
    private List<Polyline> mPolylines = new ArrayList<>();
    private Marker mDestinationMarker;
    private Marker mHeadingMarker;
    private Circle mCircle;
    private Marker mMarker;
>>>>>>> Stashed changes
    private Target mTarget;

    private boolean mCameraUpdate = true;
    private boolean mShowIndoor = false;

    private IALocationManager mIALocationManager;
    private IARegion mVenue = null;
    private IARegion mFloorPlan = null;
    private Integer mFloorLevel = null;
    private GroundOverlay mGroundOverlay = null;
<<<<<<< Updated upstream
=======
    private IARoute mRoute;
    private IAGeofence.Builder dest;
    private IAGeofence dest2;
>>>>>>> Stashed changes
    private IALocationListener mListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            if (mMap == null) return;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mCameraUpdate) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5f));
                mCameraUpdate = false;
            }
        }
    };

    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_VENUE)
                mVenue = region;
            else if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                if (mGroundOverlay == null || !region.equals(mFloorPlan)) {
                    mCameraUpdate = true;

                    if (mGroundOverlay != null) {
                        mGroundOverlay.remove();
                        mGroundOverlay = null;
                    }

                    mFloorPlan = region;
                    final IAFloorPlan floorPlan = region.getFloorPlan();
<<<<<<< Updated upstream
=======

                    /**
                    * @brief Loads maps from IndoorAtlas servers

                    * @param bitmap. Object that holds the map information
                    * @param from. Helps display image of map from IndoorAtlas
                    */

>>>>>>> Stashed changes
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
                    ImageView rufusHome = (ImageView)findViewById(R.id.rufus);
                    rufusHome.setImageResource(R.drawable.rufus);
                    mGroundOverlay.setTransparency(0.0f);
                }

                mShowIndoor = true;
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_VENUE)
                mVenue = region;
            else if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                if (mGroundOverlay != null)
                    mGroundOverlay.setTransparency(0.5f);

                mShowIndoor = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIALocationManager = IALocationManager.create(this);
        mIALocationManager.registerRegionListener(mRegionListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng pos) {
<<<<<<< Updated upstream
                MarkerOptions markerOptions = new MarkerOptions().position(pos);
                mMap.clear();
                mMap.addMarker(markerOptions);
=======
                if (mMap != null) { //If the map is loaded

                    SearchView guidance = (SearchView) findViewById(R.id.searchBar);
                    CharSequence query = guidance.getQuery();
                    String s = toString(query);
                    dest = dest.withId(s);
                    mWayfindingDestination = new IAWayfindingRequest.Builder()
                        .withFloor(mFloorLevel)
                        .withLatitude(dest2.getMaxLatitude())
                        .withLongitude(dest2.getMaxLongitude())
                        .build();
                    // The "Guidance" feature
                    mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);

                    if (mDestinationMarker == null)
                        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    else
                        mDestinationMarker.setPosition(pos);
                }
>>>>>>> Stashed changes
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIALocationManager != null) {
            mIALocationManager.removeLocationUpdates(mListener);
            mIALocationManager.registerRegionListener(mRegionListener);
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
<<<<<<< Updated upstream
=======


    /**
    * @brief Displays circle that representes users location

    * @param center LatLng Object that holds useres location
    * @param accuracyRadius number that reprecents radius of the ccuracy circle
    */

    private void showLocationCircle(LatLng center, double accuracyRadius) {
        if (mCircle == null) {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null) {
                mCircle = mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(accuracyRadius)
                        .fillColor(0)
                        .strokeColor(0)
                        .zIndex(1.0f)
                        .visible(true)
                        .strokeWidth(5.0f));
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pawprint))
                        .anchor(0.5f, 0.5f)
                        .flat(true));
            }
        } else {
             // move existing markers position to received location
            mCircle.setCenter(center);
            mMarker.setPosition(center);
            mCircle.setRadius(accuracyRadius);
        }
    }

    /**
    * @brief Displays updates status of circle that representes users location

    * @param pos LatLng Object that holds useres location
    * @param radius number that reprecents radius of the ccuracy circle
    */

    private void updateLocation(LatLng pos, double radius) {
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
                    .flat(true));
            }
        } else {
            mCircle.setCenter(pos);
            mHeadingMarker.setPosition(pos);
            mCircle.setRadius(radius);
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
                    .add(new LatLng(leg.getBegin().getLatitude(), leg.getBegin().getLongitude()))   //Continuously get the newest lat/lng cords until arrival
                    .add(new LatLng(leg.getEnd().getLatitude(), leg.getEnd().getLongitude()));

            if (leg.getBegin().getFloor() == mFloorLevel && leg.getEnd().getFloor() == mFloorLevel)
                polylineOptions.color(0xFF0000FF);
            else
                polylineOptions.color(0x300000FF);

            mPolylines.add(mMap.addPolyline(polylineOptions));
        }
    }
>>>>>>> Stashed changes
}
