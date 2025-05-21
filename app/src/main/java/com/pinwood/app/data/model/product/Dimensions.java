package com.pinwood.app.data.model.product;

import java.util.HashMap;
import java.util.Map;

public class Dimensions {
    private double width;
    private double height;
    private double depth;
    private String unit; // "cm", "in", etc.
    
    // Constructor vacío requerido para Firestore
    public Dimensions() {
    }
    
    public Dimensions(double width, double height, double depth, String unit) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.unit = unit;
    }
    
    // Método para convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("width", width);
        map.put("height", height);
        map.put("depth", depth);
        map.put("unit", unit);
        return map;
    }
    
    // Método para crear un objeto Dimensions desde un Map de Firestore
    public static Dimensions fromMap(Map<String, Object> map) {
        if (map == null) return null;
        
        Dimensions dimensions = new Dimensions();
        
        if (map.get("width") instanceof Double) {
            dimensions.width = (Double) map.get("width");
        } else if (map.get("width") instanceof Long) {
            dimensions.width = ((Long) map.get("width")).doubleValue();
        }
        
        if (map.get("height") instanceof Double) {
            dimensions.height = (Double) map.get("height");
        } else if (map.get("height") instanceof Long) {
            dimensions.height = ((Long) map.get("height")).doubleValue();
        }
        
        if (map.get("depth") instanceof Double) {
            dimensions.depth = (Double) map.get("depth");
        } else if (map.get("depth") instanceof Long) {
            dimensions.depth = ((Long) map.get("depth")).doubleValue();
        }
        
        dimensions.unit = (String) map.get("unit");
        
        return dimensions;
    }
    
    // Getters y setters
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public double getDepth() {
        return depth;
    }
    
    public void setDepth(double depth) {
        this.depth = depth;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    @Override
    public String toString() {
        return width + " x " + height + " x " + depth + " " + unit;
    }
}