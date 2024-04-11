package com.example.techshare;

import java.util.ArrayList;
import android.widget.Filter;


public class FilterAd extends Filter  {


    private AdapterAd adapter;
    private ArrayList<ModelAd> filterList;

    public FilterAd(AdapterAd adapter, ArrayList<ModelAd> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results = new FilterResults();
        if (constraint!= null && constraint.length()>0) {

            constraint = constraint.toString().toUpperCase();

            ArrayList<ModelAd> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size();i++) {
                if (filterList.get(i).getBrand().toUpperCase().contains(constraint)||
                 filterList.get(i).getCategory().toUpperCase().contains(constraint) ||
                 filterList.get(i).getCondition().toUpperCase().contains(constraint) ||
                 filterList.get(i).getTitle().toUpperCase().contains(constraint)
                ) {
                    filterModels.add(filterList.get(i));

                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {


        adapter.adArrayList = (ArrayList<ModelAd>) results.values;
        adapter.notifyDataSetChanged();
    }
}

