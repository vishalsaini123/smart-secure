package com.smartsecureapp.Activity.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.smartsecureapp.Activity.util.Utils;
import com.smartsecureapp.R;

import java.util.ArrayList;
import java.util.List;


public class LocationFragment extends Fragment {
   // TextView txt_privacy_policy, txt_term_condition;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;
    Double lat,lng;
    RelativeLayout rootLayout;
    LinearLayout ll;
    SwitchCompat switchCompat;
    SupportMapFragment mapFragment ;
    GoogleMap googleMap;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap1) {
            googleMap = googleMap1;

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();

            } else {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(true);
                googleMap.setPadding(0,0,0,90);
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location!=null) {
                            if (getArguments()!=null)
                            {
                                if (getArguments().containsKey("location")){


                                    LatLng barcelona = new LatLng(location.getLatitude(),location.getLongitude());
                                    googleMap.addMarker(new MarkerOptions().position(barcelona).title("Origin"));

                                    LatLng madrid = new LatLng(32.381001,75.549103);
                                    googleMap.addMarker(new MarkerOptions().position(madrid).title("Destination"));
                                    Log.e("aaa",location.getLatitude()+","+location.getLongitude());
                                    //Execute Directions API request
                                    List<LatLng> path = new ArrayList();
                                    GeoApiContext context = new GeoApiContext.Builder()
                                            .apiKey("AIzaSyCTg0SDVX5vwPzShgrCb461w8W6Et-aw9I")
                                            .build();
                                    DirectionsApiRequest req = DirectionsApi.getDirections(context, location.getLatitude()+","+location.getLongitude(), getArguments().getString("location"));
                                    try {
                                        DirectionsResult res = req.await();

                                        //Loop through legs and steps to get encoded polylines of each step
                                        if (res.routes != null && res.routes.length > 0) {
                                            DirectionsRoute route = res.routes[0];

                                            if (route.legs !=null) {
                                                for(int i=0; i<route.legs.length; i++) {
                                                    DirectionsLeg leg = route.legs[i];
                                                    if (leg.steps != null) {
                                                        for (int j=0; j<leg.steps.length;j++){
                                                            DirectionsStep step = leg.steps[j];
                                                            if (step.steps != null && step.steps.length >0) {
                                                                for (int k=0; k<step.steps.length;k++){
                                                                    DirectionsStep step1 = step.steps[k];
                                                                    EncodedPolyline points1 = step1.polyline;
                                                                    if (points1 != null) {
                                                                        //Decode polyline and add points to list of route coordinates
                                                                        List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                                        for (com.google.maps.model.LatLng coord1 : coords1) {
                                                                            path.add(new LatLng(coord1.lat, coord1.lng));
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                EncodedPolyline points = step.polyline;
                                                                if (points != null) {
                                                                    //Decode polyline and add points to list of route coordinates
                                                                    List<com.google.maps.model.LatLng> coords = points.decodePath();
                                                                    for (com.google.maps.model.LatLng coord : coords) {
                                                                        path.add(new LatLng(coord.lat, coord.lng));
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch(Exception ex) {
                                        Log.e("TAG", ex.getMessage());
                                    }

                                    //Draw the polyline
                                    if (path.size() > 0) {
                                        PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(17);
                                        googleMap.addPolyline(opts);
                                    }

                                    googleMap.getUiSettings().setZoomControlsEnabled(true);

                                    lat = location.getLatitude();
                                    lng = location.getLongitude();
                                    LatLng latlng = new LatLng(lat,lng);
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16));
                                }
                            }
                            else{
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                                LatLng sydney = new LatLng(lat,lng);
                                googleMap.addMarker(new MarkerOptions().position(sydney).title(""));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f ));
                            }

                        }else {
                            showSnackBar("Could not fetch your location.");
                        }

                    }
                });
            }




        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        //requestLocationPermission();
        ll = view.findViewById(R.id.ll);
        switchCompat = view.findViewById(R.id.simpleSwitch2);

        if (getDataApiFromShared())
        {
            switchCompat.setChecked(true);
            ll.setVisibility(View.VISIBLE);
        }
        else{
            switchCompat.setChecked(false);
            ll.setVisibility(View.INVISIBLE);
        }

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                UpdateDataApiFromShared(b);
                if (b){

                    ll.setVisibility(View.VISIBLE);

                }else{
                    ll.setVisibility(View.INVISIBLE);

                }
            }
        });

        rootLayout = view.findViewById(R.id.rootLayout);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

                mapFragment =(SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }


    }
    private boolean getDataApiFromShared() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        boolean value = sharedPreferences.getBoolean(Utils.IsLiveLocation, true);
        return value;
    }
    private void UpdateDataApiFromShared(boolean key) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Utils.MyPref, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Utils.IsLiveLocation,key);
        editor.apply();


    }
    private void requestLocationPermission(){

        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_READ_CONTACTS_PERMISSION);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_READ_CONTACTS_PERMISSION)
        {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.setPadding(0,0,0,90);

            Log.e("result","ccccc");
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null) {
                        if (getArguments()!=null)
                        {
                            if (getArguments().containsKey("location")){


                                LatLng barcelona = new LatLng(location.getLatitude(),location.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(barcelona).title("Origin"));

                                LatLng madrid = new LatLng(32.381001,75.549103);
                                googleMap.addMarker(new MarkerOptions().position(madrid).title("Destination"));
                                Log.e("aaa",location.getLatitude()+","+location.getLongitude());
                                //Execute Directions API request
                                List<LatLng> path = new ArrayList();
                                GeoApiContext context = new GeoApiContext.Builder()
                                        .apiKey("AIzaSyCTg0SDVX5vwPzShgrCb461w8W6Et-aw9I")
                                        .build();
                                DirectionsApiRequest req = DirectionsApi.getDirections(context, location.getLatitude()+","+location.getLongitude(), getArguments().getString("location"));
                                try {
                                    DirectionsResult res = req.await();

                                    //Loop through legs and steps to get encoded polylines of each step
                                    if (res.routes != null && res.routes.length > 0) {
                                        DirectionsRoute route = res.routes[0];

                                        if (route.legs !=null) {
                                            for(int i=0; i<route.legs.length; i++) {
                                                DirectionsLeg leg = route.legs[i];
                                                if (leg.steps != null) {
                                                    for (int j=0; j<leg.steps.length;j++){
                                                        DirectionsStep step = leg.steps[j];
                                                        if (step.steps != null && step.steps.length >0) {
                                                            for (int k=0; k<step.steps.length;k++){
                                                                DirectionsStep step1 = step.steps[k];
                                                                EncodedPolyline points1 = step1.polyline;
                                                                if (points1 != null) {
                                                                    //Decode polyline and add points to list of route coordinates
                                                                    List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                                    for (com.google.maps.model.LatLng coord1 : coords1) {
                                                                        path.add(new LatLng(coord1.lat, coord1.lng));
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            EncodedPolyline points = step.polyline;
                                                            if (points != null) {
                                                                //Decode polyline and add points to list of route coordinates
                                                                List<com.google.maps.model.LatLng> coords = points.decodePath();
                                                                for (com.google.maps.model.LatLng coord : coords) {
                                                                    path.add(new LatLng(coord.lat, coord.lng));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch(Exception ex) {
                                    Log.e("TAG", ex.getMessage());
                                }

                                //Draw the polyline
                                if (path.size() > 0) {
                                    PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(17);
                                    googleMap.addPolyline(opts);
                                }

                                googleMap.getUiSettings().setZoomControlsEnabled(true);

                                lat = location.getLatitude();
                                lng = location.getLongitude();
                                LatLng latlng = new LatLng(lat,lng);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16));
                            }
                        }
                        else{
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            LatLng sydney = new LatLng(lat,lng);
                            googleMap.addMarker(new MarkerOptions().position(sydney).title(""));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f ));
                        }

                    }else {
                        showSnackBar("Could not fetch your location.");
                    }

                }
            });
        }

    }

    private void showSnackBar(String msg) {
        Snackbar snackbar = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.btn_red));
        snackbar.show();
    }
}