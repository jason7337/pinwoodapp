package com.pinwood.app.data.model.cart;

import java.util.HashMap;
import java.util.Map;

public class CartItem {
    private String productId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;
    
    // Constructor vacío requerido para Firestore
    public CartItem() {
    }
    
    public CartItem(String productId, String name, double price, int quantity, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }
    
    // Método para convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("name", name);
        map.put("price", price);
        map.put("quantity", quantity);
        map.put("imageUrl", imageUrl);
        return map;
    }
    
    // Método para crear un objeto CartItem desde un Map de Firestore
    public static CartItem fromMap(Map<String, Object> map) {
        if (map == null) return null;
        
        CartItem cartItem = new CartItem();
        cartItem.productId = (String) map.get("productId");
        cartItem.name = (String) map.get("name");
        
        if (map.get("price") instanceof Double) {
            cartItem.price = (Double) map.get("price");
        } else if (map.get("price") instanceof Long) {
            cartItem.price = ((Long) map.get("price")).doubleValue();
        }
        
        if (map.get("quantity") instanceof Integer) {
            cartItem.quantity = (Integer) map.get("quantity");
        } else if (map.get("quantity") instanceof Long) {
            cartItem.quantity = ((Long) map.get("quantity")).intValue();
        }
        
        cartItem.imageUrl = (String) map.get("imageUrl");
        
        return cartItem;
    }
    
    // Getters y setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    // Método para calcular el subtotal de esta línea del carrito
    public double getSubtotal() {
        return price * quantity;
    }
}