package com.hku.lesinventory.model;

public interface Item {
    int getId();
    int getBrandId();
    int getCategoryId();
    String getName();
    String getDescription();
    String getImageUriString();
    int getQuantity();
}
