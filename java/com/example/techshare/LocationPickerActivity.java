package com.example.techshare;
import android.Manifest;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.techshare.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityLocationPickerBinding binding;

    private static final String TAG = "LOCATION_PICKER_TAG";
    private static final int DEFAULT_ZOOM = 15;
    private GoogleMap mMap = null;

    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation = null;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.doneLl.setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        Places.initialize(this, getString(R.string.google_map_api_key));

        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        Place.Field[] placesList = new Place.Field[]{Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG};

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(placesList));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.d(TAG, "onError: status"+status);

            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {

                String id = place.getId();
                String title = place.getName();
                LatLng latLng = place.getLatLng();
                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;
                selectedAddress = place.getAddress();

                Log.d(TAG, "onPlaceSelected: ID" + id);
                Log.d(TAG, "onPlaceSelected: Title" + title);
                Log.d(TAG, "onPlaceSelected: Latitude" + selectedLatitude);
                Log.d(TAG, "onPlaceSelected: Longitude" + selectedLongitude);
                Log.d(TAG, "onPlaceSelected: Address" + selectedAddress);

                Geocoder geocoder = new Geocoder(LocationPickerActivity.this);
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        selectedAddress = address.getAddressLine(0);
                        Log.d(TAG, "onPlaceSelected: Address" + selectedAddress);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onPlaceSelected: ", e);
                }

                addMarker(latLng, title, selectedAddress);

            }
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.toolbarGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGPSEnabled()) {
                    requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    Utils.toast(LocationPickerActivity.this,"Location is not turn ON! Turn it on to show current location");
                }
            }
        });

        binding.doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("latitude", selectedLatitude);
                intent.putExtra("longitude", selectedLongitude);
                intent.putExtra("address",""+selectedAddress.toString());
                setResult(RESULT_OK,intent);

                finish();
            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");

        mMap = googleMap;

        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;

                Log.d(TAG, "onMapClick: selectedLatitude" + selectedLatitude);
                Log.d(TAG, "onMapClick: selectedLongitude" + selectedLongitude);

                addressFromLatLng(latLng);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private ActivityResultLauncher<String> requestLocationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {

                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: ");

                    if (isGranted) {
                        mMap.setMyLocationEnabled(true);
                        pickCurrentPlace();
                    } else {
                        Utils.toast(LocationPickerActivity.this,"Permission Denied...!");
                    }
                }
            }
    );

    private void addressFromLatLng(LatLng latLng) {
        Log.d(TAG, "addressFromLatLng: ");
        Geocoder geocoder = new Geocoder(this);
        try {

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

            Address address = addressList.get(0);

            String addressLine = address.getAddressLine(0);
            String countryName = address.getCountryName();
            String adminArea = address.getAdminArea();
            String subAdminArea = address.getSubAdminArea();
            String Locality = address.getLocality();
            String subLocality = address.getSubLocality();
            String postalCode = address.getPostalCode();


            selectedAddress = "" + addressLine;
            addMarker(latLng,""+subLocality,""+addressLine);



        } catch (Exception e) {
            Log.d(TAG, "addressFromLatLng: ",e);
        }
    }

    private void pickCurrentPlace() {
        Log.d(TAG, "pickCurrentPlace: ");
        if (mMap == null) {
            return;
    }
        detectAndShowDevicesLocationMap();

}

    @SuppressLint("MissingPermission")
    private void detectAndShowDevicesLocationMap(){


    try {
         Task<Location> locationResult  = mFusedLocationProviderClient.getLastLocation();
        locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    mLastKnownLocation = location;

                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();

                    Log.d(TAG, "onSuccess: selectedLatitude" + selectedLatitude);
                    Log.d(TAG, "onSuccess:selectedLongitude" + selectedLongitude);

                    LatLng latLng = new LatLng(selectedLatitude, selectedLongitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

                    addressFromLatLng(latLng);
                }  else {
                    Log.d(TAG, "onSuccess:Location is Null");
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e);

                    }
                });


    } catch(Exception e)

    {
        Log.e(TAG, "detectAndShowDevicesLocationMap: ",e );
        }
    }

    private boolean isGPSEnabled(){
        LocationManager ln = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = ln.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception e) {
            Log.e(TAG, "isGPSEnabled: ", e);
        }

        try {
            networkEnabled = ln.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        } catch (Exception e){
            Log.e(TAG, "isGPSEnabled: ",e );
        }

        return !(!gpsEnabled && !networkEnabled);

    }

    private void addMarker(LatLng latLng, String title, String address){

        Log.d(TAG, "addMarker: latitude"+latLng.latitude);

        Log.d(TAG, "addMarker: longitude"+latLng.longitude);

        Log.d(TAG, "addMarker: title"+title);

        Log.d(TAG, "addMarker: address"+address);

        mMap.clear();

        try {

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("" + title);
            markerOptions.snippet(""+ address);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));


            binding.doneLl.setVisibility(View.VISIBLE);
            binding.selectedPlaceTv.setText(address);

        } catch (Exception e){
            Log.e(TAG, "addMarker: ",e);
        }

    }
}