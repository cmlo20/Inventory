package com.hku.lesinventory.model;

public interface Instance {
    int getId();
    int getItemId();
    int getLocationId();
    String getRfidUii();
    String getBarcode();
}
