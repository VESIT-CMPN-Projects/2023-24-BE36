package com.example.techshare;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.techshare.databinding.ActivityAdDetailsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdDetailsActivity extends AppCompatActivity implements AdapterAd.CartItemClickListener {

    private ActivityAdDetailsBinding binding;
    private static final String TAG = "AD_DETAILS_TAG";

    private AdapterAd.CartItemClickListener cartItemClickListener;
    private FirebaseAuth firebaseAuth;
    private String adId = "";
    private double adLatitude = 0;
    private double adLongitude = 0;
    private String sellerUid = null;
    private String sellerPhone = "";

    private boolean favorite = false;

    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarEditBtn.setVisibility(View.GONE);
        binding.toolbarDeleteBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);

        adId = getIntent().getStringExtra("adId");
        Log.d(TAG, "onCreate :adId" + adId);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();

        }

        loadAdDetails();
        loadAdImages();
        loadCart();
        checkInCart();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolbarDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(AdDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Delete Ad")
                        .setMessage("Are you sure you want to delete this Ad?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAd();
                                Intent intent = new Intent(AdDetailsActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        binding.toolbarEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.toolbarFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorite) {
                    Utils.removeFromFavorite(AdDetailsActivity.this, adId);
                } else {
                    Utils.addToFavorite(AdDetailsActivity.this, adId);
                }
            }
        });

        binding.goToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdDetailsActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });


        binding.sellerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdDetailsActivity.this, AdSellerProfileActivity.class);
                intent.putExtra("sellerUid", sellerUid);
                startActivity(intent);


            }
        });

        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdDetailsActivity.this, ChatActivity.class);
                intent.putExtra("receiptUid", sellerUid);
                startActivity(intent);

            }
        });

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.callIntent(AdDetailsActivity.this, sellerPhone);
            }
        });

        binding.smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.smsIntent(AdDetailsActivity.this, sellerPhone);
            }
        });

        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.mapIntent(AdDetailsActivity.this, adLatitude, adLongitude);
            }
        });


        binding.addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ModelAd> modelAdArrayList = new ArrayList<>();
                DatabaseReference adRef = FirebaseDatabase.getInstance().getReference("Ads").child(adId);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                adRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@org.checkerframework.checker.nullness.qual.NonNull DataSnapshot snapshot) {

                        ModelAd modelAd = snapshot.getValue(ModelAd.class);
                        modelAdArrayList.add(modelAd);

                        if (adId.equals(modelAd.getId()))
                            ref.child(firebaseAuth.getUid()).child("Cart").child(adId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            boolean inCart = snapshot.exists();

                                            if (inCart) {

                                                Utils.removeFromCart(getApplicationContext(), modelAd);
                                                binding.addToCartBtn.setText("Add to cart");
                                                binding.addToCartBtn.setIconResource(R.drawable.ic_cart_add);
                                                Log.d(TAG, "remove from cart");
                                            } else {
                                                Utils.addToCart(getApplicationContext(), modelAd);
                                                binding.addToCartBtn.setText("Remove from cart");
                                                binding.addToCartBtn.setIconResource(R.drawable.ic_cart);
                                                Log.d(TAG, "add to cart");
                                            }
                                            if (firebaseAuth.getUid().equals(modelAd.getUid())) {
                                                binding.addToCartBtn.setVisibility(View.GONE);
                                            }
                                            if (cartItemClickListener != null) {
                                                cartItemClickListener.onCartItemAdded(modelAdArrayList);
                                            }


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        loadCart();
        Log.d(TAG, "onPostCreate: " + "ad details activoty");

    }

    public void checkInCart() {

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference("Ads").child(adId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@org.checkerframework.checker.nullness.qual.NonNull DataSnapshot snapshot) {

                ModelAd modelAd = snapshot.getValue(ModelAd.class);
                if (adId.equals(modelAd.getId()))
                    ref.child(firebaseAuth.getUid()).child("Cart").child(adId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean inCart = snapshot.exists();
                                    // modelAd.setCart(inCart);
                                    if (inCart) {
                                        binding.addToCartBtn.setText("Remove from cart");
                                        binding.addToCartBtn.setIconResource(R.drawable.ic_cart);
                                    } else {
                                        binding.addToCartBtn.setText("Add to cart");
                                        binding.addToCartBtn.setIconResource(R.drawable.ic_cart_add);
                                    }
                                    if (firebaseAuth.getUid().equals(modelAd.getUid())) {
                                        binding.addToCartBtn.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void test(boolean inCart, ModelAd modelAd, ArrayList<ModelAd> modelAdArrayList) {

    }

    public interface CartItemClickListener {
        void onCartItemAdded(ArrayList<ModelAd> cartItems);
    }


    public void setCartItemClickListener(AdapterAd.CartItemClickListener listener) {
        this.cartItemClickListener = listener;
    }


    private void loadAdDetails() {
        Log.d(TAG, "LoadadDetails:");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {

                            double latitude = snapshot.child("latitude").getValue(Double.class);
                            double longitude = snapshot.child("longitude").getValue(Double.class);

                            Map<String, Object> map = new HashMap<>();
                            map.put("latitude", latitude);
                            map.put("longitude", longitude);
                            ModelAd modelAd = snapshot.getValue(ModelAd.class);

                            sellerUid = modelAd.getUid();

                            String title = modelAd.getTitle();
                            String description = modelAd.getDescription();
                            String address = modelAd.getAddress();
                            String condition = modelAd.getCondition();
                            String price = modelAd.getPrice();
                            String category = modelAd.getCategory();
                            adLatitude = modelAd.getLatitude();
                            adLongitude = modelAd.getLongitude();
                            long timestamp = modelAd.getTimestamp();
                            long rented_on = modelAd.getRented_on();

                            String formattedDate = Utils.formatTimestampDate(timestamp);

                            if (sellerUid.equals(firebaseAuth.getUid())) {

                                binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                                binding.toolbarDeleteBtn.setVisibility(View.VISIBLE);
                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.GONE);
                                binding.sellerProfileLabelTv.setVisibility(View.GONE);
                                binding.sellerProfileCv.setVisibility(View.GONE);
                                binding.addToCartBtn.setVisibility(View.GONE);

                            } else {
                                binding.toolbarEditBtn.setVisibility(View.GONE);
                                binding.toolbarDeleteBtn.setVisibility(View.GONE);
                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.VISIBLE);
                                binding.sellerProfileLabelTv.setVisibility(View.VISIBLE);
                                binding.sellerProfileCv.setVisibility(View.VISIBLE);
                                binding.addToCartBtn.setVisibility(View.VISIBLE);

                            }
                            if (firebaseAuth.getUid().equals(modelAd.getUid())) {
                                binding.addToCartBtn.setVisibility(View.GONE);
                            }

                            binding.toolbarTitleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.conditionTv.setText(condition);
                            binding.priceTv.setText(price);
                            binding.dateTv.setText(formattedDate);


                            Utils.findIndex(category);
                            binding.categoryIconIv.setImageResource(Utils.categoryIcons[Utils.findIndex(category)]);

//                            binding.categoryTv.setText(category);
                            loadSellerDetails();


                        } catch (
                                Exception e) {

                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadSellerDetails() {
        Log.d(TAG, "loadSellerDetails");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                        long timestamp = (Long) snapshot.child("timestamp").getValue();

                        String formattedDate = Utils.formatTimestampDate(timestamp);

                        sellerPhone = phoneCode + "" + phoneNumber;


                        binding.sellerNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);
                        try {

                            Glide.with(AdDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.sellerProfileIv);


                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        DatabaseReference adref = FirebaseDatabase.getInstance().getReference("Ads").child(adId);
        adref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ModelAd modelAd = snapshot.getValue(ModelAd.class);

                if (modelAd.getStatus().equals("RENTED")) {
                    binding.addToCartBtn.setVisibility(View.GONE);
                    int dL = Utils.daysLeft(modelAd.getRented_on(), modelAd.getRent_period());
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(modelAd.getRented_to());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String temp = snapshot.child("name").getValue().toString();
                            binding.addressTv.setText("Rented to " + temp + " for " + String.valueOf(dL) + " more days");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    //  binding.addToCartBtn.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: ");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        favorite = snapshot.exists();

                        Log.d(TAG, "onDataChange: favorite: " + favorite);
                        if (favorite) {
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_red_yes);
                        } else {
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_red_no);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void loadAdImages() {
        Log.d(TAG, "loadAdimages");
        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        imageSliderArrayList.clear();
                        Log.d(TAG, "onDataChange: loadAdimages");
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);
                            imageSliderArrayList.add(modelImageSlider);

                        }

                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(AdDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void deleteAd() {
        Log.d(TAG, "deleteAd");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: deleted");
                        Utils.toast(AdDetailsActivity.this, "Deleted");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure ", e);
                        Utils.toast(AdDetailsActivity.this, "Failed to delete due to " + e.getMessage());
                    }
                });

    }


    private void loadCart() {
        FirebaseAuth firebaseAuth;
        firebaseAuth = FirebaseAuth.getInstance();

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid()).child("Cart");

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int itemCount = (int) snapshot.getChildrenCount();
                    Log.d("CartItemCount", "Item count in cart: " + itemCount);
                    updateCartCount(itemCount);
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        String cartAdId = ds.child("itemId").getValue().toString();
//                        if (cartAdId.equals(adId)) {
//                            binding.addToCartBtn.setText("Remove from cart");
//                            binding.addToCartBtn.setIconResource(R.drawable.ic_cart);
//                        } else {
//                            binding.addToCartBtn.setText("Add to cart");
//                            binding.addToCartBtn.setIconResource(R.drawable.ic_cart_add);
//                        }


                    }


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
