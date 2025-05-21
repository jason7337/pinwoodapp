package com.pinwood.app.utils;

public class Constants {
    // Firebase Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_PRODUCTS = "products";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_ORDERS = "orders";
    
    // Firebase User Sub-collections
    public static final String SUBCOLLECTION_CART = "cart";
    public static final String SUBCOLLECTION_FAVORITES = "favorites";
    
    // Firebase Product Sub-collections
    public static final String SUBCOLLECTION_REVIEWS = "reviews";
    
    // Shared Preferences
    public static final String PREFS_NAME = "PinwoodPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_EMAIL = "user_email";
    
    // Order Status
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_PROCESSING = "processing";
    public static final String ORDER_STATUS_SHIPPED = "shipped";
    public static final String ORDER_STATUS_DELIVERED = "delivered";
    public static final String ORDER_STATUS_CANCELED = "canceled";
    
    // Intent Extra Keys
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_MODEL_URL = "model_url";
    public static final String EXTRA_ORDER_ID = "order_id";
    
    // Request Codes
    public static final int REQUEST_CODE_CAMERA_PERMISSION = 101;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 102;
    public static final int REQUEST_CODE_AR_ACTIVITY = 103;
    
    // Notification Channels
    public static final String CHANNEL_ORDERS = "orders_channel";
    public static final String CHANNEL_PROMOTIONS = "promotions_channel";
}