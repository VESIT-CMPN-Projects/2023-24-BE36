package com.example.techshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techshare.databinding.RowAddBinding;
import com.example.techshare.databinding.RowCartItemBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class AdapterCart extends RecyclerView.Adapter<AdapterCart.HolderCart> {

    private RowCartItemBinding binding;
    private Context context;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelCart> cartList;
    ArrayList<ModelCart> cartList2;

    int amount=0;

    public AdapterCart(Context context, ArrayList<ModelCart> cartList) {
        this.context = context;
        this.cartList = cartList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderCart onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowCartItemBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderCart(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCart holder, @SuppressLint("RecyclerView")  int position) {
        ModelCart modelCart = cartList.get(position);
        String itemName = modelCart.getItemName();
        String itemBrand = modelCart.getItemBrand();
        String itemCategory = modelCart.getItemCategory();
        String itemCondition = modelCart.getItemCondition();
        String itemPrice = modelCart.getItemPrice();
        String itemRentPeriod = String.valueOf((int)modelCart.getItemRentPeriod());
        long timestamp = modelCart.getItemAddedOn();
        String itemAddedOn = Utils.formatTimestampDate(timestamp);

        loadAdFirstImage(modelCart,holder);

        holder.itemNameTv.setText((itemName));
        holder.itemBrandTv.setText((itemBrand));
        holder.itemCategoryTv.setText((itemCategory));
        holder.itemConditionTv.setText((itemCondition));
        holder.itemPriceTv.setText((itemPrice));
        holder.itemRentPeriod.setText("(For "+ itemRentPeriod+" Days)");
        holder.itemAddedOn.setText((itemAddedOn));

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Ads").child(modelCart.getItemId());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("status").getValue().toString().equals("RENTED")) {
                    Utils.removeFromCartOnCheckout(context, modelCart.getItemId().toString());


                }
                else{
                    holder.removeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.removeFromCartOnCheckout(context, modelCart.getItemId().toString());

                            cartList2= new ArrayList<>();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("Cart");
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    cartList2.clear();
                                    for (DataSnapshot ds : snapshot.getChildren()) {

                                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                                        if (map != null) {
                                            ModelCart modelCart = new ModelCart(
                                                    map.get("itemId").toString(),
                                                    map.get("itemBrand").toString(),
                                                    map.get("itemCategory").toString(),
                                                    map.get("itemCondition").toString(),
                                                    map.get("itemPrice").toString(),
                                                    map.get("itemName").toString(),
                                                    Long.parseLong(map.get("itemAddedOn").toString()),
                                                    Integer.parseInt(map.get("itemRentPeriod").toString())
                                            );
                                            if (modelCart != null) {
                                                cartList2.add(modelCart);
                                            }
                                        }

                                        Collections.sort(cartList2, new Comparator<ModelCart>() {
                                            @Override
                                            public int compare(ModelCart ad1, ModelCart ad2) {
                                                return Long.compare(ad2.getItemAddedOn(), ad1.getItemAddedOn()); // Descending order
                                            }
                                        });




                                    }
                                    CartActivity.refreshAmount(cartList2);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle onCancelled
                                }
                            });


                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class HolderCart extends RecyclerView.ViewHolder {

        ShapeableImageView itemImageIv;
        TextView itemNameTv, itemPriceTv, itemBrandTv, itemCategoryTv, itemConditionTv, itemAddedOn, itemRentPeriod,itemPriceLabelTv;
        ImageButton removeBtn;


        public HolderCart(@NonNull View itemView) {
            super(itemView);

            itemImageIv = binding.itemImageIv;
            itemNameTv = binding.itemNameTv;
            itemPriceTv = binding.itemPriceTv;
            itemRentPeriod = binding.itemRentPeriod;
            removeBtn = binding.removeBtn;
            itemBrandTv = binding.itemBrandTv;
            itemConditionTv = binding.itemConditionTv;
            itemCategoryTv = binding.itemCategoryTv;
            itemAddedOn = binding.itemAddedOn;
            itemPriceLabelTv=binding.itemPriceLabelTv;
        }
    }



    private void loadAdFirstImage(ModelCart modelCart, AdapterCart.HolderCart holder) {

        String adId = modelCart.getItemId();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
        reference.child(adId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String imageUrl ="" + ds.child("imageUrl").getValue();


                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_image_white)
                                        .into(holder.itemImageIv);
                            } catch (Exception e) {
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}

