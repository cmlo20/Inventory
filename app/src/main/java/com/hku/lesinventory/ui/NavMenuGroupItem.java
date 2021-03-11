package com.hku.lesinventory.ui;

public class NavMenuGroupItem {
    private String name;
    private int iconId;

    public NavMenuGroupItem(String name, int iconId) {
        this.name = name;
        this.iconId = iconId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIcon(int resId) {
        this.iconId = resId;
    }

    public int getIconId() {
        return iconId;
    }

    public String toString() {
        return name;
    }
}
