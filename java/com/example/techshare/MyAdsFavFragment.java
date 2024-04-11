package com.example.techshare;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.techshare.databinding.FragmentMyAdsFavBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class MyAdsFavFragment extends Fragment {
    private FragmentMyAdsFavBinding binding;

    private static final String TAG = "FAV_TAG";

    private Context mContext;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelAd> adArrayList;

    private AdapterAd adapterAd;


    public MyAdsFavFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadAds();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged:", e);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void loadAds() {
        Log.d(TAG, "loadAds:");
        adArrayList = new ArrayList<>();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid()).child("Favorites");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String adId = ds.child("adId").getValue(String.class);
                    if (adId != null) {
                        loadAdDetails(adId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadAds:onCancelled", error.toException());
            }
        });
    }

    private void loadAdDetails(String adId) {
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference("Ads").child(adId);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);

                    Map<String, Object> map = new HashMap<>();
                    map.put("latitude", latitude);
                    map.put("longitude", longitude);
                    ModelAd modelAd=snapshot.getValue(ModelAd.class);

                    if (modelAd != null) {
                        adArrayList.add(modelAd);
                        Collections.sort(adArrayList, new Comparator<ModelAd>() {
                            @Override
                            public int compare(ModelAd ad1, ModelAd ad2) {
                                return Long.compare(ad2.getTimestamp(), ad1.getTimestamp()); // Descending order
                            }
                        });

                        adapterAd = new AdapterAd(requireContext(), adArrayList);
                        binding.adsRv.setAdapter(adapterAd);
                        adapterAd.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "loadAdDetails:onDataChange", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadAdDetails:onCancelled", error.toException());
            }
        });
    }

}
