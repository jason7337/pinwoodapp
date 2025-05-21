package com.pinwood.app.data.model.user;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private Address address;
    private String profileImageUrl;
    private Date createdAt;
    
    // Constructor vacío requerido para Firestore
    public User() {
    }
    
    public User(String userId, String name, String email, String phone, Address address, String profileImageUrl, Date createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
    }
    
    // Método para convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);
        if (address != null) {
            map.put("address", address.toMap());
        }
        map.put("profileImageUrl", profileImageUrl);
        map.put("createdAt", createdAt);
        return map;
    }
    
    // Método para crear un objeto User desde un Map de Firestore
    public static User fromMap(Map<String, Object> map) {
        if (map == null) return null;
        
        User user = new User();
        user.userId = (String) map.get("userId");
        user.name = (String) map.get("name");
        user.email = (String) map.get("email");
        user.phone = (String) map.get("phone");
        
        Map<String, Object> addressMap = (Map<String, Object>) map.get("address");
        if (addressMap != null) {
            user.address = Address.fromMap(addressMap);
        }
        
        user.profileImageUrl = (String) map.get("profileImageUrl");
        user.createdAt = (Date) map.get("createdAt");
        
        return user;
    }
    
    // Getters y setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}