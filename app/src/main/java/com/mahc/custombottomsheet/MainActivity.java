package com.mahc.custombottomsheet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mahc.custombottomsheet.model.DirectionObject;
import com.mahc.custombottomsheet.model.LegsObject;
import com.mahc.custombottomsheet.model.PolylineObject;
import com.mahc.custombottomsheet.model.RouteObject;
import com.mahc.custombottomsheet.model.StepsObject;
import com.mahc.custombottomsheet.model.places.PlacesPOJO;
import com.mahc.custombottomsheet.model.places.StoreModel;
import com.mahc.custombottomsheet.retrofit.APIClient;
import com.mahc.custombottomsheet.retrofit.ApiInterface;
import com.mahc.custombottomsheet.util.AppConstants;
import com.mahc.custombottomsheet.util.AppUtils;
import com.mahc.custombottomsheet.util.GpsUtils;
import com.mahc.custombottomsheet.util.GsonRequest;
import com.mahc.custombottomsheet.util.Helper;
import com.mahc.custombottomsheet.util.VolleySingleton;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;
import retrofit2.Call;
import retrofit2.Callback;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, PlacesListener {
    GoogleApiClient mGoogleApiClient;
    Circle circle;
    CircleOptions circleOptions;
    private GoogleMap mMap;
    MapView mapview;
    private boolean isContinue = false;
    private boolean isGPS = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    TextView tvSchoolName,schoolAddress;
    private LocationCallback locationCallback;
    int[] mDrawables = {
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3
    };
    private List<LatLng> latLngList;
    TextView bottomSheetTextView;
    List<StoreModel> storeModels;
    ApiInterface apiService;
    FloatingActionButton floatingActionButton;
    String latLngString;
    LatLng latLng;
    private MarkerOptions SourceLocationMarker;
    List<PlacesPOJO.CustomA> results;
     BottomSheetBehaviorGoogleMapsLike behavior;
    Marker mCurrLocationMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapview=(MapView)findViewById(R.id.mapview);
        floatingActionButton=(FloatingActionButton)findViewById(R.id.floatingActionButton);
        latLngList = new ArrayList<LatLng>();
        tvSchoolName=(TextView)findViewById(R.id.tvSchoolName);
        schoolAddress=(TextView)findViewById(R.id.schoolAddress);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        circleOptions = new CircleOptions();
        setLocationRequest();
        locationCallback();
        try {
            mapview.onCreate(savedInstanceState);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapview.getMapAsync(this);
        /**
         * If we want to listen for states callback
         */
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
      //  behavior.setPeekHeight(200);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");

                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        floatingActionButton.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        floatingActionButton.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        floatingActionButton.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        MergedAppBarLayout mergedAppBarLayout = findViewById(R.id.mergedappbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle(getString(R.string.school_info));
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
            }
        });

        bottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);


        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        //behavior.setCollapsible(false);
    }
    void setLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000);
    }
    private void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();
    }
    void GpsEnable() {
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;

            }
        });
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    void locationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latLng=new LatLng(location.getLatitude(),location.getLongitude());
                      //  latLngString = location.getLatitude() + "," + location.getLongitude();
                        setMapLocation(location);

                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapview != null)
            mapview.onResume();
        GpsEnable();
        getLocation();
    }

    void setMapLocation(Location location) {

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        setGeoFence(latLng);
       /* SourceLocationMarker = new MarkerOptions();
        SourceLocationMarker.position(latLng);
        // markerOptions.title("Current Position");
        SourceLocationMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(SourceLocationMarker);
*/
        //move map camera

       /* CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);*/
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
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("", "Can't find style. Error: ", e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                tvSchoolName.setText(storeModels.get(Integer.parseInt(marker.getTitle())).name);
                schoolAddress.setText(storeModels.get(Integer.parseInt(marker.getTitle())).address);
           if (behavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN) {
               floatingActionButton.setVisibility(View.VISIBLE);
             /*  ItemPagerAdapter adapter = new ItemPagerAdapter(MainActivity.this,storeModels.get(Integer.parseInt(marker.getTitle())).name);
               ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
               viewPager.setAdapter(adapter);*/
               getDirection(marker.getPosition(), Integer.parseInt(marker.getTitle()));

               behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);

           }
                return false;
            }
        });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                    mMap.setMyLocationEnabled(true);

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapview != null)
            mapview.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapview != null)
            mapview.onDestroy();

    }


    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setLocationRequest();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null)
                setMapLocation(lastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    void setGeoFence(LatLng point) {
        if (circle == null) {
            // Specifying the center of the circle
            circleOptions.center(point);

            // Radius of the circle
            circleOptions.radius(1 * 1609.34);// Converting Miles into Meters...

            // Border color of the circle
            circleOptions.strokeColor(Color.parseColor("#77C47E"));

            // Fill color of the circle
            // 0x represents, this is an hexadecimal code
            // 55 represents percentage of transparency. For 100% transparency, specify 00.
            // For 0% transparency ( ie, opaque ) , specify ff
            // The remaining 6 characters(00ff00) specify the fill color
            //circleOptions.fillColor(Color.parseColor("#F8F4E3"));

            // Border width of the circle
            circleOptions.strokeWidth(50);

           /* circle.remove();
        }*/
            // Adding the circle to the GoogleMap
            circle = mMap.addCircle(circleOptions);

            float currentZoomLevel = getZoomLevel(circle);
            float animateZomm = currentZoomLevel + 5;

            Log.e("Zoom Level:", currentZoomLevel + "");
            Log.e("Zoom Level Animate:", animateZomm + "");


            //move map camera

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 14));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
            getSchools(point);
        }
    }
    void getSchools(LatLng latLng){
     //   apiService = APIClient.getClient().create(ApiInterface.class);
       // fetchStores("school","school");
        new NRPlaces.Builder()
                .listener(this)
                .key(APIClient.GOOGLE_PLACE_API_KEY)
                .latlng(latLng.latitude, latLng.longitude)
                .radius(1500).type(PlaceType.SCHOOL)
                .build()
                .execute();

    }


    public void fetchStores(String placeType, String businessName) {


        /**
         * For Locations In India McDonalds stores aren't returned accurately
         */

        //Call<PlacesPOJO.Root> call = apiService.doPlaces(placeType, latLngString,"\""+ businessName +"\"", true, "distance", APIClient.GOOGLE_PLACE_API_KEY);

        Call<PlacesPOJO.Root> call = apiService.doPlaces(placeType, latLngString, businessName, true, "distance", APIClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesPOJO.Root>() {

            @Override
            public void onResponse(Call<PlacesPOJO.Root> call, retrofit2.Response<PlacesPOJO.Root> response) {
                PlacesPOJO.Root root = response.body();


                if (response.isSuccessful()) {

                    if (root.status.equals("OK")) {

                        results = root.customA;
                        storeModels = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {

                            if (i == 10)
                                break;
                            PlacesPOJO.CustomA info = results.get(i);

                            storeModels.add(new StoreModel(info.name, info.vicinity, "", "",info.geometry.locationA.lat,info.geometry.locationA.lng));


                            // fetchDistance(info);

                        }
                        for(int i=0;i<storeModels.size();i++) {
                            int color= AppUtils.getMarkerColor(i);
                            if(color==AppConstants.RED) {
                                final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.primary)));

                            }//    markers.put(hamburg.getId(), "http://img.india-forums.com/images/100x100/37525-a-still-image-of-akshay-kumar.jpg");
                            else if(color==AppConstants.GREEN) {
                                final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.upper_primary)));

                            }
                            else if(color==AppConstants.VIOLET) {
                                final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.secondary)));

                            }
                            else  {
                                final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.higher_secondary)));

                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "No matches found near you", Toast.LENGTH_SHORT).show();
                    }

                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<PlacesPOJO.Root> call, Throwable t) {
                // Log error here since request failed
                call.cancel();
            }
        });


    }
    public float getZoomLevel(Circle circle) {
        float zoomLevel = 0;
        if (circle != null) {
            double radius = circle.getRadius();
            double scale = radius / 400;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel + .5f;
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(List<Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storeModels = new ArrayList<>();
                for (int i = 0; i < places.size(); i++) {

                    if (i == 10)
                        break;
                    Place info = places.get(i);

                    storeModels.add(new StoreModel(info.getName(), info.getVicinity(), "", "",""+info.getLatitude(),""+info.getLongitude()));
                    // fetchDistance(info);

                }

                for(int i=0;i<storeModels.size();i++) {
                    int color= AppUtils.getMarkerColor(i);
                    if(color==AppConstants.RED) {
                        final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.primary)));

                    }//    markers.put(hamburg.getId(), "http://img.india-forums.com/images/100x100/37525-a-still-image-of-akshay-kumar.jpg");
                    else if(color==AppConstants.GREEN) {
                        final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.upper_primary)));

                    }
                    else if(color==AppConstants.VIOLET) {
                        final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.secondary)));

                    }
                    else  {
                        final Marker hamburg = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(storeModels.get(i).lat), Double.parseDouble(storeModels.get(i).longi))).title("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.higher_secondary)));

                    }
                }
            }
        });
    }

    @Override
    public void onPlacesFinished() {

    }

    void getDirection(LatLng l, int p) {
        if (latLngList.size() > 0) {

            latLngList.clear();
        }
        refreshMap(mMap);
        SourceLocationMarker = new MarkerOptions();
        SourceLocationMarker.position(latLng);
        // markerOptions.title("Current Position");
        SourceLocationMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(SourceLocationMarker);
        latLngList.add(latLng);
        //    Log.d(TAG, "Marker number " + latLngList.size());
        mMap.addMarker(SourceLocationMarker);
        int color=AppUtils.getMarkerColor(p);
        if(color==AppConstants.RED) {
            mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.fromResource(R.drawable.primary)));
        }

        else if(color==AppConstants.GREEN) {
            mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.fromResource(R.drawable.upper_primary)));

        }
        else if(color==AppConstants.VIOLET) {
            mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.fromResource(R.drawable.secondary)));

        }
        else  {
            mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.fromResource(R.drawable.higher_secondary)));


        }
        LatLng defaultLocation = SourceLocationMarker.getPosition();
        LatLng destinationLocation = l;
        //use Google Direction API to get the route between these Locations
        String directionApiPath = Helper.getUrl(String.valueOf(defaultLocation.latitude), String.valueOf(defaultLocation.longitude),
                String.valueOf(destinationLocation.latitude), String.valueOf(destinationLocation.longitude));
        // Log.d(TAG, "Path " + directionApiPath);
        getDirectionFromDirectionApiServer(directionApiPath);

    }
    private void getDirectionFromDirectionApiServer(String url) {
        GsonRequest<DirectionObject> serverRequest = new GsonRequest<DirectionObject>(
                Request.Method.GET,
                url,
                DirectionObject.class,
                createRequestSuccessListener(),
                createRequestErrorListener());
        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                Helper.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(serverRequest);
    }

    private Response.Listener<DirectionObject> createRequestSuccessListener() {
        return new Response.Listener<DirectionObject>() {
            @Override
            public void onResponse(DirectionObject response) {
                try {
                    Log.d("JSON Response", response.toString());
                    if (response.getStatus().equals("OK")) {
                        List<LatLng> mDirections = getDirectionPolylines(response.getRoutes());
                        drawRouteOnMap(mMap, mDirections);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        };
    }

    private List<LatLng> getDirectionPolylines(List<RouteObject> routes) {
        List<LatLng> directionList = new ArrayList<LatLng>();
        for (RouteObject route : routes) {
            List<LegsObject> legs = route.getLegs();
            for (LegsObject leg : legs) {
                List<StepsObject> steps = leg.getSteps();
                for (StepsObject step : steps) {
                    PolylineObject polyline = step.getPolyline();
                    String points = polyline.getPoints();
                    List<LatLng> singlePolyline = decodePoly(points);
                    for (LatLng direction : singlePolyline) {
                        directionList.add(direction);
                    }
                }
            }
        }
        return directionList;
    }

    private Response.ErrorListener createRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {
        circle.setVisible(false);
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLACK).geodesic(true);
        options.addAll(positions);
        int p=1;
        if(positions!=null && positions.size()>2){
            p=positions.size()/2;
        }
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(p).latitude, positions.get(p).longitude))
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

}
