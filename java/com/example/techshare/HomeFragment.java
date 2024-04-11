package com.example.techshare;

import android.annotation.SuppressLint;
import android.app.Activity;
import  android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.FrameMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.techshare.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements AdapterAd.CartItemClickListener  {

    private FragmentHomeBinding binding;

    private static final String TAG = "HOME_TAG";
    private static final int MAX_DISTANCE_TO_LOAD_ADS_KM = 10;

    private Context mContext;

    private ArrayList<ModelAd> adArrayList;

    private AdapterAd adapterAd;

    private SharedPreferences locationSp;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";


    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);
        //loadCart();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationSp = mContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE);
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "");

        if (currentLatitude == 0.0 || currentLongitude == 0.0) {
            Log.d(TAG, "onViewCreated: currentLatitude or currentLongitude is null");
        } else {
            if (currentAddress.isEmpty()) {
                Log.d(TAG, "onViewCreated: currentAddress is empty");
            } else {
                binding.locationTv.setText(currentAddress);
            }
        }

        loadCategories();

        loadAds("All");

loadCart();


        binding.goToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent intent = new Intent(getContext(), CartActivity.class);
                    startActivity(intent);
                }else{
                    Utils.toast(getContext(),"You are not logged in!");
                }
            }
        });

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: Query" + s);

                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);

                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.locationCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, LocationPickerActivity.class);
                locationPickerActivityResult.launch(intent);
            }
        });
    }



    private ActivityResultLauncher<Intent> locationPickerActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: RESULT_OK");

                        Intent data = result.getData();
                        if (data != null) {
                            Log.d(TAG, "onActivityResult: Location Picked ");
                            currentLatitude = data.getDoubleExtra("latitude", 0.0);
                            currentLongitude = data.getDoubleExtra("longitude", 0.0);
                            currentAddress = data.getStringExtra("address");

                            locationSp.edit()
                                    .putFloat("CURRENT_LATITUDE", Float.parseFloat("" + currentLatitude))
                                    .putFloat("CURRENT_LONGITUDE", Float.parseFloat("" + currentLongitude))
                                    .putString("CURRENT_ADDRESS", currentAddress)
                                    .apply();

                            binding.locationTv.setText(currentAddress);

                            loadAds("All");


                        }
                    } else {
                        Log.d(TAG, "onActivityResult: Cancelled!!!");
                        Utils.toast(mContext, "Cancelled!!");
                    }

                }
            }
    );


    private void loadCategories() {

        ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();

        ModelCategory modelCategoryAll = new ModelCategory("All", R.drawable.ic_category_all);
        categoryArrayList.add(modelCategoryAll);


        for (int i = 0; i < Utils.categories.length; i++) {
            ModelCategory modelCategory = new ModelCategory(Utils.categories[i], Utils.categoryIcons[i]);
            categoryArrayList.add(modelCategory);
        }

        AdapterCategory adapterCategory = new AdapterCategory(mContext, categoryArrayList, new RvListenerCategory() {
            @Override
            public void onCategoryClick(ModelCategory modelCategory) {

                loadAds(modelCategory.getCategory());

            }
        });

        binding.categoriesRv.setAdapter(adapterCategory);

    }


    @SuppressLint("SuspiciousIndentation")
    private void loadCart(){
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();

        try {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid()).child("Cart");

            if(cartRef!=null)
            cartRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int itemCount = (int) snapshot.getChildrenCount();
                        Log.d("CartItemCount", "Item count in cart: " + itemCount);
                        updateCartCount(itemCount);

                    } else {
                        updateCartCount(0);
                        Log.d("CartItemCount", "Cart is empty");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CartItemCount", "Error retrieving cart items: " + error.getMessage());
                }

            });
        }
        catch (Exception e){
            Log.e(TAG, "loadCart: ",e);
        }

    }

    private void loadAds(String category) {
        Log.d(TAG, "loadAds: Category " + category);

        adArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adArrayList.clear();


                for (DataSnapshot ds : snapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) ds.getValue();
                    if (map != null) {
                        ModelAd modelAd = new ModelAd(map.get("id").toString(), map.get("uid").toString(), map.get("brand").toString(), map.get("category").toString(), map.get("condition").toString(), map.get("address").toString(), map.get("price").toString(), map.get("title").toString(), map.get("description").toString(), map.get("status").toString(), Long.parseLong(map.get("timestamp").toString()), map, Integer.parseInt(map.get("rent_period").toString()),Long.parseLong(map.get("rented_on").toString()) , map.get("rented_to").toString());
                        if (modelAd == null) {
                            Log.d(TAG, "onDataChange: modelAd is null");
                            continue;
                        }



                        int rent_period= modelAd.getRent_period();
                        double latitude = modelAd.getLatitude();
                        double longitude = modelAd.getLongitude();
                        if (Double.isNaN(latitude) || Double.isInfinite(latitude) || Double.isNaN(longitude) || Double.isInfinite(longitude)) {
                            Log.d(TAG, "onDataChange: latitude or longitude is null or invalid");
                            continue;
                        }
                        double distance = calculatedDistanceKm(latitude, longitude);
                        Log.d(TAG, "onDataChange: distance" + distance);

                        if (distance < 0.0) {
                            continue;
                        }

                        if (category.equals("All")) {
                            if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM) {
                                adArrayList.add(modelAd);
                            }
                        } else {
                            if (modelAd.getCategory().equals(category)) {
                                if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM) {
                                    adArrayList.add(modelAd);
                                }
                            }
                        }
                    }

                }

                Collections.sort(adArrayList, new Comparator<ModelAd>() {
                    @Override
                    public int compare(ModelAd ad1, ModelAd ad2) {
                        return Long.compare(ad2.getTimestamp(), ad1.getTimestamp()); // Descending order
                    }
                });
                adapterAd = new AdapterAd(mContext, adArrayList);
                adapterAd.setCartItemClickListener(HomeFragment.this); // Set the callback listener
                binding.adsRv.setAdapter(adapterAd);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });

    }

    private double calculatedDistanceKm(double adLatitude, double adLongitude) {

        Log.d(TAG, "calculatedDistanceKm: currentLatitude" + currentLatitude);
        Log.d(TAG, "calculatedDistanceKm: currentLongitude" + currentLongitude);
        Log.d(TAG, "calculatedDistanceKm: adLatitude" + adLatitude);
        Log.d(TAG, "calculatedDistanceKm: adLongitude" + adLongitude);

        if (currentLatitude != 0.0 && currentLongitude != 0.0 && adLatitude != 0.0 && adLongitude != 0.0) {
            Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
            startPoint.setLatitude(currentLatitude);
            startPoint.setLongitude(currentLongitude);

            Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
            endPoint.setLatitude(adLatitude);
            endPoint.setLongitude(adLongitude);

            double distanceInMeters = startPoint.distanceTo(endPoint);
            double distanceInKm = distanceInMeters / 1000;

            return distanceInKm;
        } else {
            Log.d(TAG, "calculatedDistanceKm: one or more location values are invalid");
            return -1.0;


        }

    }

    private void updateCartCount(long itemCount) {
        Log.d("CartItemCount", "Item count in cart: " + itemCount);
        TextView badgeCount = binding.getRoot().findViewById(R.id.goToCartText);

        // Update the badge count text
        badgeCount.setText(String.valueOf(itemCount));


        // Set visibility based on item count
        if (itemCount > 0) {
            badgeCount.setVisibility(View.VISIBLE);
        } else {
            badgeCount.setVisibility(View.GONE);
        }
    }


    @Override
    public void onCartItemAdded(ArrayList<ModelAd> cartItems) {
        loadCart();



    }
}