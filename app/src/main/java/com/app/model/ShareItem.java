package com.app.model;

public class ShareItem {
    String imageUrl;   // for chat user
    int iconRes;       // for drawable icon
    String name;
    String packageName;

    // For URL-based items
    public ShareItem(String imageUrl, String name, String packageName) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.packageName = packageName;
    }

    // For drawable-based items
    public ShareItem(int iconRes, String name, String packageName) {
        this.iconRes = iconRes;
        this.name = name;
        this.packageName = packageName;
    }

    public boolean hasImageUrl() { return imageUrl != null && !imageUrl.isEmpty(); }

    public String getImageUrl() { return imageUrl; }

    public int getIconRes() { return iconRes; }

    public String getName() { return name; }

    public String getPackageName() { return packageName; }
}


