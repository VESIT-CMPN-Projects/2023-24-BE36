package com.example.techshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.techshare.databinding.ActivityAdSellerProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdSellerProfileActivity extends AppCompatActivity {

    private String sellerUid="";
    private ActivityAdSellerProfileBinding binding;

    private ArrayList<ModelAd> adArrayList;

    private Context context;

    private static final String TAG ="AD_SELLER_PROFILE_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAdSellerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sellerUid=getIntent().getStringExtra("sellerUid");
        Log.d(TAG,"onCreate:SellerUid"+sellerUid);
        loadSellerDetails();
        loadAds();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    private void loadSellerDetails(){

        Log.d(TAG,"loadSellerDetail");
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name =""+snapshot.child("name").getValue();
                        String profileImageUrl =""+snapshot.child("profileImageUrl").getValue();
                        long timestamp =(Long)snapshot.child("timestamp").getValue();
                        String formattedDate =Utils.formatTimestampDate(timestamp);

                        binding.sellerNameTv.setText(name);
                        binding.sellerMemberSinceTv.setText(formattedDate);
                        try{
                            Glide.with(AdSellerProfileActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.sellerProfileIv);

                        }catch(Exception e){
                            Log.e(TAG,"onDataChange",e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    private void loadAds(){

        Log.d(TAG,"loadAds");
        adArrayList =  new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Ads");
        ref.orderByChild("uid").equalTo(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adArrayList.clear();

                        for(DataSnapshot ds:snapshot.getChildren()){
                        try{
                            ModelAd modelAd=ds.getValue(ModelAd.class);
                            adArrayList.add(modelAd);

                        }catch (Exception e){

                            Log.e(TAG,"onDataChange: loadads",e);

                        }

                        }
                        AdapterAd adapterAd=new AdapterAd(AdSellerProfileActivity.this, adArrayList);
                        binding.adsRv.setAdapter(adapterAd);

                        String adsCount=""+adArrayList.size();
                        binding.publishedAdsCountTv.setText(adsCount);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





    }
}