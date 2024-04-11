package com.example.techshare;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReverseGeocodeTask extends AsyncTask<Double, Void, String> {

    public interface OnGeocodeListener {
        void onGeocode(String address, String newAddress);
        Context getContext();
    }

    private OnGeocodeListener onGeocodeListener;

    public ReverseGeocodeTask(OnGeocodeListener onGeocodeListener) {
        this.onGeocodeListener = onGeocodeListener;
    }

    @Override
    protected String doInBackground(Double... params) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        double latitude = params[0];
        double longitude = params[1];
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder addressString = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressString.append(address.getAddressLine(i)).append(", ");
                }
                return addressString.toString();
            }
        } catch (IOException e) {
            Log.e("ReverseGeocodeTask", "Error getting address for latitude " + latitude + " and longitude " + longitude, e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String address) {
        if (onGeocodeListener != null) {
            onGeocodeListener.onGeocode(address, address);
        }
    }

    private Context getContext() {
        return onGeocodeListener != null ? onGeocodeListener.getContext() : null;
    }
}