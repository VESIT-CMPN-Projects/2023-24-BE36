package com.example.techshare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static final String MESSAGE_TYPE_TEXT="TEXT";
    public static final String MESSAGE_TYPE_IMAGE="IMAGE";
    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_RENTED = "RENTED";

    private static boolean isItemDisabled = false;
    public static final String[] categories = {
            "Mobiles",
            "Laptop/Computer",
            "Cameras",
            "Flying Drones",
            "PlayStations",
            "Digital Watches",
            "HeadPhones",
            "Virtual Headset",
            "X Box",
            "CD's",
            "Speakers"
    };

    public static int findIndex(String target) {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    public static final int[] categoryIcons = {
            R.drawable.ic_category_mobile,
            R.drawable.ic_category_laptop,
            R.drawable.ic_category_camera,
            R.drawable.ic_category_drone,
            R.drawable.ic_category_playstation,
            R.drawable.ic_category_watch,
            R.drawable.ic_category_headphones,
            R.drawable.ic_category_vr,
            R.drawable.ic_category_xbox,
            R.drawable.ic_category_cd,
            R.drawable.ic_category_speaker

    };

    public static final String[] conditions = {"As new", "Used", "Older Model"};

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestampDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();
        return date;
    }

    public static String formatTimestampDateTime(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString();
        return date;
    }
    public static void addToFavorite(Context context, String adId) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're  not Logged In!!");
        } else {
            long timestamp = Utils.getTimestamp();


            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("adId", adId);
            hashMap.put("timestamp", timestamp);


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Added to favorite");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Failed to add to favorite due to" + e.getMessage());
                        }

                    });
        }
    }

    public static void removeFromFavorite(Context context, String adId) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Utils.toast(context,"You're not logged in!");
        } else {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context,"Removed from favorite");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Utils.toast(context,"Failed to remove from favorite due to"+e.getMessage());

                        }
                    });

        }
    }

    public static String chatPath(String receiptUid, String yourUid){

        String[] arrayUids = new String[]{receiptUid,yourUid};

        Arrays.sort(arrayUids);
        String chatPath=arrayUids[0]+"_"+arrayUids[1];

        return chatPath;
    }

    public  static  void callIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phone)));
        context.startActivity(intent);
    }
    public  static  void smsIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",Uri.encode(phone),null));
        context.startActivity(intent);
    }

    public  static  void mapIntent(Context context, double latitude, double longitude){

        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr="+latitude+","+longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if(mapIntent.resolveActivity(context.getPackageManager())!=null){

        }
        else{
            Utils.toast(context, "Google Map not installed");
        }
        context.startActivity(mapIntent);
    }

    public static void addToCart(Context context, ModelAd cartItem) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're  not Logged In!!");
        } else {
            long timestamp = Utils.getTimestamp();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("itemId",cartItem.getId());
                hashMap.put("itemName",cartItem.getTitle());
                hashMap.put("itemPrice",cartItem.getPrice());
                hashMap.put("itemRentPeriod",cartItem.getRent_period());
                hashMap.put("itemBrand",cartItem.getBrand());
                hashMap.put("itemCategory",cartItem.getCategory());
                hashMap.put("itemCondition",cartItem.getCondition());
                hashMap.put("itemAddedOn", timestamp);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(firebaseAuth.getUid()).child("Cart").child(cartItem.getId())
                    .setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Utils.toast(context, "Item Added to cart");
                                cartItem.setCart(true);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.toast(context, "Failed to add to cart due to" + e.getMessage());
                            }

                        });

        }
    }

    public static void removeFromCart(Context context, ModelAd cartItem) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're  not Logged In!!");
        } else {
            long timestamp = Utils.getTimestamp();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(firebaseAuth.getUid()).child("Cart").child(cartItem.getId())
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Utils.toast(context, "Item removed from cart");
                                cartItem.setCart(false);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.toast(context, "Failed to remove from cart due to" + e.getMessage());
                            }

                        });




        }
    }

    public static void removeFromCartOnCheckout(Context context, String itemId) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're  not Logged In!!");
        } else {
            long timestamp = Utils.getTimestamp();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Cart").child(itemId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Item removed from cart");


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Failed to remove from cart due to" + e.getMessage());
                        }

                    });




        }
    }



    static int daysLeft(long rentedOnTimestamp, int rentPeriod){



        long currentTimestamp = System.currentTimeMillis();
        long rentToTimeMillis = TimeUnit.DAYS.toMillis(rentPeriod);
        long rentalPeriodEndTimestamp = rentedOnTimestamp + rentToTimeMillis;
        long remainingTimeMillis = rentalPeriodEndTimestamp-currentTimestamp;
        int remainingDays = (int) TimeUnit.MILLISECONDS.toDays(remainingTimeMillis );

        return remainingDays;

    }


    private static long getTimestampFromDate(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        long timestamp2=0;
        try {
            // Parse the date string into a Date object
            Date date = dateFormat.parse(dateString);

            // Get the timestamp in milliseconds from the Date object
            timestamp2 = date.getTime();

            // Print the timestamp
            System.out.println("Timestamp (milliseconds): " + timestamp2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp2;
    }
    private static String getDateFromTimeStamp(long timestamp){
        // Create a Date object using the timestamp
        Date date = new Date(timestamp);

        // Define the date format for formatting the Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Format the Date object into a readable date string
        String formattedDate = dateFormat.format(date);

        // Print the formatted date
        System.out.println("Formatted Date: " + formattedDate);
        return formattedDate;
    }
}




