package com.pinwood.app.data.model.user;

import java.util.HashMap;
import java.util.Map;

public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    // Constructor vacío requerido para Firestore
    public Address() {
    }
    
    public Address(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Método para convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("street", street);
        map.put("city", city);
        map.put("state", state);
        map.put("zipCode", zipCode);
        map.put("country", country);
        return map;
    }
    
    // Método para crear un objeto Address desde un Map de Firestore
    public static Address fromMap(Map<String, Object> map) {
        if (map == null) return null;
        
        Address address = new Address();
        address.street = (String) map.get("street");
        address.city = (String) map.get("city");
        address.state = (String) map.get("state");
        address.zipCode = (String) map.get("zipCode");
        address.country = (String) map.get("country");
        
        return address;
    }
    
    // Getters y setters
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
}