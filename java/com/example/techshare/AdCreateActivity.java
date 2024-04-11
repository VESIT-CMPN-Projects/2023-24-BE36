package com.example.techshare;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.net.Uri;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.widget.PopupMenu;
import android.content.Intent;


import com.example.techshare.databinding.ActivityAdCreateBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdCreateActivity extends AppCompatActivity {

    private ActivityAdCreateBinding binding;

    private static final String TAG = "AD_CREATE_TAG";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private Uri imageUri = null;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String address = "";
    private ArrayList<ModelImagePicked> ImagePickedArrayList;

    private AdapterImagesPicked adapterImagesPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoryAct.setAdapter(adapterCategories);

        ArrayAdapter<String> adapterConditions = new ArrayAdapter<>(this, R.layout.row_condition_act, Utils.conditions);
        binding.conditionAct.setAdapter(adapterConditions);

        ImagePickedArrayList = new ArrayList<>();
        loadImages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolbarAdImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });

        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdCreateActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });


        binding.AdPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        if (data != null) {
                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            address = data.getStringExtra("address");

                            Log.d(TAG, "onActivityResult:latitude:" + latitude);
                            Log.d(TAG, "onActivityResult:longitude:" + longitude);
                            binding.locationAct.setText(address);

                        }

                    } else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Utils.toast(AdCreateActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void loadImages() {
        Log.d(TAG, "loadImages:");

        adapterImagesPicked = new AdapterImagesPicked(this, ImagePickedArrayList);
        binding.imagesRv.setAdapter(adapterImagesPicked);
    }

    private void showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions:");

        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarAdImageBtn);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == 1) {

                    String[] cameraPermissions;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        cameraPermissions = new String[]{Manifest.permission.CAMERA};
                    } else {
                        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    }
                    requestCameraPermissions.launch(cameraPermissions);


                } else if (itemId == 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        String storagePermissions = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        requestStoragePermissions.launch(storagePermissions);
                    }
                }
                return true;
            }
        });
    }

    private ActivityResultLauncher<String> requestStoragePermissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted:" + isGranted);

                    if (isGranted) {
                        pickImageGallery();

                    } else {

                        Utils.toast(AdCreateActivity.this, "Storage Permission Denied.....");
                    }

                }
            }
    );
    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult:");
                    Log.d(TAG, "onActivityResult" + result.toString());

                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {

                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        pickImageCamera();

                    } else {
                        Utils.toast(AdCreateActivity.this, "Camera or Storage or both Permissions Denied...");
                    }
                }
            }
    );

    private void pickImageGallery() {

        Log.d(TAG, "pickImageGallery:");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {

        Log.d(TAG, "pickImageCamera:");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMPORARY_IMAGE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMPORARY_IMAGE_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult:");

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri" + imageUri);

                        String timestamp = "" + Utils.getTimestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        ImagePickedArrayList.add(modelImagePicked);
                        loadImages();
                    } else {
                        Utils.toast(AdCreateActivity.this, "Cancelled...!");
                    }
                }
            }
    );
    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult:");

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Log.d(TAG, "onActivityResult: imageUri" + imageUri);

                        String timestamp = "" + System.currentTimeMillis();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        ImagePickedArrayList.add(modelImagePicked);
                        loadImages();
                    } else {
                        Utils.toast(AdCreateActivity.this, "Cancelled...!");
                    }

                }
            }
    );
    private String brand = "";
    private String category = "";
    private String condition = "";

    private String price = "";
    private String title = "";
    private String description = "";

    private int rent_period = 0;
    private long rented_on = 0000000000;
    private String rented_to = "None";

    private void validateData() {
        Log.d(TAG, "validateData:");
        brand = binding.brandEt.getText().toString().trim();
        category = binding.categoryAct.getText().toString().trim();
        condition = binding.conditionAct.getText().toString().trim();
        address = binding.locationAct.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        rent_period = Integer.parseInt(binding.rentPeriodEt.getText().toString());
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if (brand.isEmpty()) {
            binding.brandEt.setError("Enter Brand");
            binding.brandEt.requestFocus();
        } else if (category.isEmpty()) {
            binding.categoryAct.setError("Choose Category");
            binding.categoryAct.requestFocus();
        } else if (condition.isEmpty()) {
            binding.conditionAct.setError("Select Condition");
            binding.conditionAct.requestFocus();
        } else if (address.isEmpty()) {
            binding.locationAct.setError("Choose Location");
            binding.locationAct.requestFocus();
        } else if (rent_period == 0) {
            binding.rentPeriodEt.setError("Enter Rent Period (Days)");
            binding.rentPeriodEt.requestFocus();
        } else if (price.isEmpty()) {
            binding.priceEt.setError("Enter Price");
            binding.priceEt.requestFocus();
        } else if (title.isEmpty()) {
            binding.titleEt.setError("Enter Description");
            binding.titleEt.requestFocus();
        } else if (description.isEmpty()) {
            binding.descriptionEt.setError("Enter Description");
            binding.descriptionEt.requestFocus();
        } else if (ImagePickedArrayList.isEmpty()) {
            Utils.toast(this, "Please pick at-least one Image");
        } else {
            postAd();


        }
    }

    private void postAd() {
        Log.d(TAG, "postAd:");
        progressDialog.setMessage("Publishing Ad");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        DatabaseReference refAds = FirebaseDatabase.getInstance().getReference("Ads");
        String keyId = refAds.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + keyId);
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("brand", "" + brand);
        hashMap.put("category", "" + category);
        hashMap.put("condition", "" + condition);
        hashMap.put("address", "" + address);
        hashMap.put("price", "" + price);
        hashMap.put("rent_period", rent_period);
        hashMap.put("rented_on", rented_on);
        hashMap.put("rented_to", rented_to);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("status", "" + Utils.AD_STATUS_AVAILABLE);
        hashMap.put("timestamp", timestamp);
        hashMap.put("longitude", longitude);
        hashMap.put("latitude", latitude);

        refAds.child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Ad Published");

                        uploadImagesStorage(keyId);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailure", e);
                        progressDialog.dismiss();
                        Utils.toast(AdCreateActivity.this, "Failed to publish Ad due to" + e.getMessage());
                    }
                });
    }

    private void uploadImagesStorage(String adId) {
        Log.d(TAG, "uploadImagesStorage:");

        for (int i = 0; i < ImagePickedArrayList.size(); i++) {
            ModelImagePicked modelImagePicked = ImagePickedArrayList.get(i);

            String imageName = modelImagePicked.getId();
            String filePathAndName = "Ads/" + imageName;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            int imageIndexForProgress = i + 1;

            storageReference.putFile(modelImagePicked.getImageUri())
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                            String message = "uploading " + imageIndexForProgress + "/" + ImagePickedArrayList.size() + " images..\nProgress: " + (int) progress + "%";
                            Log.d(TAG, "onProgress: message" + message);
                            progressDialog.setMessage(message);
                            progressDialog.show();

                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "onSuccess:");
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri uploadedImageUrl = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", "" + modelImagePicked.getId());
                                hashMap.put("imageUrl", "" + uploadedImageUrl);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                                ref.child(adId).child("Images")
                                        .child(imageName)
                                        .updateChildren(hashMap);
                            }
                            progressDialog.setMessage("Uploaded");
                            progressDialog.show();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(AdCreateActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1000);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure", e);
                            progressDialog.setMessage("Upload Failed!");
                            progressDialog.show();
                            progressDialog.dismiss();

                        }
                    });
        }
    }
}
