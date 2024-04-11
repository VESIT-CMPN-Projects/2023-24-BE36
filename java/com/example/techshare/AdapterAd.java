package com.example.techshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techshare.databinding.RowAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterAd extends RecyclerView.Adapter<AdapterAd.HolderAd> implements Filterable {
    private RowAddBinding binding;
    private CartItemClickListener cartItemClickListener;
    private static final String TAG = "ADAPTER_AD_TAG";

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelAd> cartItemsList = new ArrayList<>(); // List to store cart items

    private Context context;
    public ArrayList<ModelAd> adArrayList;
    private ArrayList<ModelAd> filterList;

    private FilterAd filter;


    public AdapterAd(Context context, ArrayList<ModelAd> adArrayList) {
        this.context = context;
        this.adArrayList = adArrayList;
        this.filterList = adArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowAddBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderAd(binding.getRoot());
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onBindViewHolder(@NonNull HolderAd holder, @SuppressLint("RecyclerView") int position) {

        ModelAd modelAd = adArrayList.get(position);

        String title = modelAd.getTitle();
        String description = modelAd.getDescription();
        String address = modelAd.getAddress();
        String condition = modelAd.getCondition();
        String price = modelAd.getPrice();
        long timestamp = modelAd.getTimestamp();
        String formattedDate = Utils.formatTimestampDate(timestamp);

        loadAdFirstImage(modelAd, holder);

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite(modelAd, holder);
            checkInCart(modelAd, holder);
        }

        holder.titleTv.setText((title));
        holder.descriptionTv.setText((description));
        holder.addressTv.setText((address));
        holder.conditionTv.setText((condition));
        holder.priceTv.setText((price));
        holder.dateTv.setText((formattedDate));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null) {
                Intent intent = new Intent(context, AdDetailsActivity.class);
                intent.putExtra("adId", modelAd.getId());
                context.startActivity(intent);
            }
                else{
                Utils.toast(context,"You are not logged in!");
                }
            }
        });


        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean favorite = modelAd.isFavorite();
                if (favorite) {
                    Utils.removeFromFavorite(context, modelAd.getId());
                } else {
                    Utils.addToFavorite(context, modelAd.getId());
                }
            }
        });


        holder.addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               checkInCart(modelAd,holder);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                if (modelAd.getId() != null)
                    ref.child(firebaseAuth.getUid()).child("Cart").child(modelAd.getId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean inCart = snapshot.exists();
                                    // boolean inCart = modelAd.isCart();
                                    if (firebaseAuth.getCurrentUser() != null) {
                                        if (inCart) {
                                            Log.d(TAG, "onClick:addtocartBtn " + inCart);
                                            Utils.removeFromCart(context, modelAd);
                                            holder.addToCartBtn.setImageResource(R.drawable.ic_cart_add);
                                        } else {

                                            Log.d(TAG, "onClick:addtocartBtn " + inCart);

                                            Utils.addToCart(context, modelAd);
                                            holder.addToCartBtn.setImageResource(R.drawable.ic_cart);
                                        }
                                        if (cartItemClickListener != null) {
                                            cartItemClickListener.onCartItemAdded(cartItemsList);
                                        }
                                    } else {
                                        Utils.toast(context, "You are not logged in!");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

            }
        });
        int daysLeft = Utils.daysLeft(modelAd.getRented_on(), modelAd.getRent_period());

        if (daysLeft < 0) {

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "" + Utils.AD_STATUS_AVAILABLE);
            hashMap.put("rented_to", "");
            hashMap.put("rented_on", 0);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
            if (modelAd.getId() != null)
                ref.child(modelAd.getId()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.e(TAG, "onSuccess: Marked as Rented");

                                holder.addToCartBtn.setVisibility(View.VISIBLE);
                                holder.addressTv.setText(modelAd.getAddress());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure", e);
                            }
                        });
        }

        if (modelAd.getStatus() != null && modelAd.getStatus().equals("RENTED")) {
            holder.addToCartBtn.setVisibility(View.GONE);
            int dL = Utils.daysLeft(modelAd.getRented_on(), modelAd.getRent_period());
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(modelAd.getRented_to());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String temp = snapshot.child("name").getValue().toString();
                    holder.addressTv.setText("Rented to " + temp + " for " + String.valueOf(dL) + " more days");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

//            holder.itemView.setEnabled(false);
//            holder.itemView.setAlpha(0.5f); // Set alpha to indicate disabled state
        } else {
            //holder.addToCartBtn.setVisibility(View.VISIBLE);
//            holder.itemView.setEnabled(true);
//            holder.itemView.setAlpha(1.0f); // Set alpha to indicate enabled state
        }
    }


    private void checkInCart(ModelAd modelAd, HolderAd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        if (modelAd.getId() != null)
            ref.child(firebaseAuth.getUid()).child("Cart").child(modelAd.getId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean inCart = snapshot.exists();
//                        modelAd.setCart(inCart);
                            Log.d(TAG, "onDataChange: checkIncart " + inCart);
                            if (inCart) {
                                holder.addToCartBtn.setImageResource(R.drawable.ic_cart);

                            } else {
                                holder.addToCartBtn.setImageResource(R.drawable.ic_cart_add);
                            }
                            if (firebaseAuth.getUid().equals(modelAd.getUid())) {
                                holder.addToCartBtn.setImageResource(0);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

    }


    private void checkIsFavorite(ModelAd modelAd, HolderAd holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        if (modelAd.getId() != null)
            ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean favorite = snapshot.exists();

                            modelAd.setFavorite(favorite);
                            if (favorite) {

                                holder.favBtn.setImageResource(R.drawable.ic_fav_red_yes);

                            } else {

                                holder.favBtn.setImageResource(R.drawable.ic_fav_red_no);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


    }

    private void loadAdFirstImage(ModelAd modelAd, HolderAd holder) {
        Log.d(TAG, "loadAdFirstImage: ");

        String adId = modelAd.getId();
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
            if (adId != null)
                reference.child(adId).child("Images").limitToFirst(1)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String imageUrl = "" + ds.child("imageUrl").getValue();
                                    Log.d(TAG, "onDataChange: imageUrl" + imageUrl);


                                    try {
                                        Glide.with(context)
                                                .load(imageUrl)
                                                .placeholder(R.drawable.ic_image_white)
                                                .into(holder.imageIv);
                                    } catch (Exception e) {
                                        Log.e(TAG, "onDataChange: ", e);
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        } catch (Exception e) {
            Log.e(TAG, "loadAdFirstImage: ", e);
        }

    }

    @Override
    public int getItemCount() {
        return adArrayList.size();
    }

    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new FilterAd(this, filterList);
        }
        return filter;
    }

    public interface CartItemClickListener {
        void onCartItemAdded(ArrayList<ModelAd> cartItems);
    }


    public void setCartItemClickListener(CartItemClickListener listener) {
        this.cartItemClickListener = listener;
    }

    class HolderAd extends RecyclerView.ViewHolder {

        ShapeableImageView imageIv;
        TextView titleTv, descriptionTv, addressTv, conditionTv, priceTv, dateTv, badgeCount;
        ImageButton favBtn, addToCartBtn;

        public HolderAd(@NonNull View itemView) {
            super(itemView);
            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            favBtn = binding.favBtn;
            addToCartBtn = binding.addToCartBtn;
            addressTv = binding.addressTv;
            conditionTv = binding.conditionTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;


        }
    }

}
