package com.pinwood.app.data.model.product;

import java.util.HashMap;
import java.util.Map;

public class ArModel {
    private String url;
    private String format; // "glb", "gltf", etc.
    
    // Constructor vacío requerido para Firestore
    public ArModel() {
    }
    
    public ArModel(String url, String format) {
        this.url = url;
        this.format = format;
    }
    
    // Método para convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("format", format);
        return map;
    }
    
    // Método para crear un objeto ArModel desde un Map de Firestore
    public static ArModel fromMap(Map<String, Object> map) {
        if (map == null) return null;
        
        ArModel arModel = new ArModel();
        arModel.url = (String) map.get("url");
        arModel.format = (String) map.get("format");
        
        return arModel;
    }
    
    // Getters y setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
}