package com.example.techshare;

public class ModelCart {
    private String itemId;
    private String itemBrand;
    private String itemCategory;
    private String itemCondition;
    private String itemPrice;
    private String itemName;
    private long itemAddedOn;
    private int itemRentPeriod;

    public ModelCart() {
    }

    public ModelCart(String itemId, String itemBrand, String itemCategory, String itemCondition, String itemPrice, String itemName, long itemAddedOn, int itemRentPeriod) {
        this.itemId = itemId;
        this.itemBrand = itemBrand;
        this.itemCategory = itemCategory;
        this.itemCondition = itemCondition;
        this.itemPrice = itemPrice;
        this.itemName = itemName;
        this.itemAddedOn = itemAddedOn;
        this.itemRentPeriod = itemRentPeriod;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemBrand() {
        return itemBrand;
    }

    public void setItemBrand(String itemBrand) {
        this.itemBrand = itemBrand;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public long getItemAddedOn() {
        return itemAddedOn;
    }

    public void setItemAddedOn(long itemAddedOn) {
        this.itemAddedOn = itemAddedOn;
    }

    public double getItemRentPeriod() {
        return itemRentPeriod;
    }

    public void setItemRentPeriod(int itemRentPeriod) {
        this.itemRentPeriod = itemRentPeriod;
    }
}
