package com.hku.lesinventory.model;

import java.util.Date;

public interface Instance {
    int getId();
    int getItemId();
    int getLocationId();
    String getRfidUii();
    String getBarcode();
    Date getCheckedInAt();
}
