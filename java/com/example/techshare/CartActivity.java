package com.example.techshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.example.techshare.databinding.ActivityCartBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements PaymentResultListener {
    private RecyclerView recyclerView;
    private AdapterCart adapterCart;
    private FirebaseAuth firebaseAuth;
    private static ActivityCartBinding binding;
    private ArrayList<ModelCart> cartList;

    private final static String TAG = "CART_ACTIVITY";
    int amount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartList = new ArrayList<>();
        // Initialize AdapterCart here

        //recyclerView = binding.cartRecyclerView;
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(adapterCart);  // Set the adapter here

        binding.checkoutBtn.setText("Pay Now Rs" + amount);


        loadCartList();
        binding.placeHolder.setVisibility(View.VISIBLE);
        binding.cartRecyclerView.setVisibility(View.GONE);
        binding.checkoutBtn.setVisibility(View.GONE);
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        binding.checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLatestAmountandPay();
            }
        });


    }


    private void loadCartList() {
        cartList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("Cart");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
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
                            cartList.add(modelCart);
                        }
                    }

                    Collections.sort(cartList, new Comparator<ModelCart>() {
                        @Override
                        public int compare(ModelCart ad1, ModelCart ad2) {
                            return Long.compare(ad2.getItemAddedOn(), ad1.getItemAddedOn()); // Descending order
                        }
                    });


                    Log.d("CartActivity", "Cart List Size: " + cartList.size());
                    adapterCart = new AdapterCart(CartActivity.this, cartList);
                    binding.cartRecyclerView.setAdapter(adapterCart);
                    adapterCart.notifyDataSetChanged();

                    if (cartList.size() == 0) {
                        binding.placeHolder.setVisibility(View.VISIBLE);
                        binding.cartRecyclerView.setVisibility(View.GONE);
                        binding.checkoutBtn.setVisibility(View.GONE);
                    } else {
                        binding.placeHolder.setVisibility(View.GONE);
                        binding.cartRecyclerView.setVisibility(View.VISIBLE);
                        binding.checkoutBtn.setVisibility(View.VISIBLE);
                    }


                }
                getTotalAmount();

                // Update the UI with the total amount
                binding.checkoutBtn.setText("Pay Amount: Rs. " + amount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });


    }

    private void getLatestAmountandPay() {
        amount = 0;
        cartList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("Cart");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
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
                            cartList.add(modelCart);
                        }
                    }
                }
                for (ModelCart cartItem : cartList) {
                    int itemPrice = Integer.parseInt(cartItem.getItemPrice());
                    amount += itemPrice + 50;
                }

                DatabaseReference ref2= FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String name = snapshot.child("name").getValue().toString();
                        String email = snapshot.child("email").getValue().toString();
                        String phoneNumber = snapshot.child("phoneNumber").getValue().toString();

                        if (amount != 0) {
//                int amount = Math.round(Float.parseFloat(samount) * 100);
                            Checkout checkout = new Checkout();
                            checkout.setKeyID("rzp_test_0o4aBsx4YEgESu");
                            checkout.setImage(R.drawable.ic_image_white);
                            JSONObject object = new JSONObject();
                            try {
                                object.put("name", name);
                                object.put("description", "Demoing Charges");
                                object.put("send_sms_hash",true);
                                object.put("allow_rotation", true);
                                object.put("currency", "INR");
                                object.put("amount", amount * 100);
                                object.put("prefill.contact", phoneNumber);
                                object.put("prefill.email", email);
                                checkout.open(CartActivity.this, object);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Utils.toast(getApplicationContext(), "Nothing in Cart to pay for");
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


    private int getTotalAmount() {
        for (ModelCart cartItem : cartList) {
            int itemPrice = Integer.parseInt(cartItem.getItemPrice());
            amount += itemPrice + 50;
        }
        return amount;
    }

    static void refreshAmount(ArrayList<ModelCart> cartList2) {
        int amount = 0;
        for (ModelCart cartItem : cartList2) {
            int itemPrice = Integer.parseInt(cartItem.getItemPrice());
            amount += itemPrice + 50;
        }
        binding.checkoutBtn.setText("Pay Amount: Rs. " + amount);
        if (cartList2.size() == 0) {
            binding.placeHolder.setVisibility(View.VISIBLE);
            binding.cartRecyclerView.setVisibility(View.GONE);
            binding.checkoutBtn.setVisibility(View.GONE);
        } else {
            binding.placeHolder.setVisibility(View.GONE);
            binding.cartRecyclerView.setVisibility(View.VISIBLE);
            binding.checkoutBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onPaymentSuccess(String s) {
        Utils.toast(getApplicationContext(), "Payment Successful!");

        for (ModelCart cartItem : cartList) {
            amount = 0;
            binding.checkoutBtn.setText("Pay Now");
//             Utils.removeFromCartOnCheckout(getApplicationContext(), cartItem.getItemId().toString());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "" + Utils.AD_STATUS_RENTED);
            hashMap.put("rented_to", FirebaseAuth.getInstance().getUid().toString());
            hashMap.put("rented_on", Utils.getTimestamp());

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
            ref.child(cartItem.getItemId()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.e(TAG, "onSuccess: Marked as Rented");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(CartActivity.this, HomeFragment.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1000);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure", e);
                        }
                    });
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        Utils.toast(getApplicationContext(), "Payment Failed!");
    }

}