package com.pinwood.app.data.model.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {
    private String productId;
    private String name;
    private String description;
    private double price;
    private String category;
    private Dimensions dimensions;
    private List<String> imageUrls;
    private List<ArModel> arModels;
    private int availableStock;
    private List<String> tags;
    
    // Constructor vacío requerido para Firestore
    public Product() {
        imageUrls = new ArrayList<>();
        arModels = new ArrayList<>();
        tags = new ArrayList<>();
    }
    
    public Product(String productId, String name, String description, double price, String category, 
                  Dimensions dimensions, List<String> imageUrls, List<ArModel> arModels, 
                  int availableStock, List<String> tags) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.dimensions = dimensions;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.arModels = arModels != null ? arModels : new ArrayList<>();
        this.availableStock = availableStock;
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    
    // Método para convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("name", name);
        map.put("description", description);
        map.put("price", price);
        map.put("category", category);
        
        if (dimensions != null) {
            map.put("dimensions", dimensions.toMap());
        }
        
        map.put("imageUrls", imageUrls);
        
        List<Map<String, Object>> arModelMaps = new ArrayList<>();
        for (ArModel model : arModels) {
            arModelMaps.add(model.toMap());
        }
        map.put("arModels", arModelMaps);
        
        map.put("availableStock", availableStock);
        map.put("tags", tags);
        
        return map;
    }
    
    // Método para crear un objeto Product desde un Map de Firestore
    public static Product fromMap(Map<String, Object> map) {
        if (map == null) return null;
        
        Product product = new Product();
        product.productId = (String) map.get("productId");
        product.name = (String) map.get("name");
        product.description = (String) map.get("description");
        
        if (map.get("price") instanceof Double) {
            product.price = (Double) map.get("price");
        } else if (map.get("price") instanceof Long) {
            product.price = ((Long) map.get("price")).doubleValue();
        }
        
        product.category = (String) map.get("category");
        
        Map<String, Object> dimensionsMap = (Map<String, Object>) map.get("dimensions");
        if (dimensionsMap != null) {
            product.dimensions = Dimensions.fromMap(dimensionsMap);
        }
        
        List<String> imageUrls = (List<String>) map.get("imageUrls");
        if (imageUrls != null) {
            product.imageUrls = imageUrls;
        }
        
        List<Map<String, Object>> arModelMaps = (List<Map<String, Object>>) map.get("arModels");
        if (arModelMaps != null) {
            product.arModels = new ArrayList<>();
            for (Map<String, Object> modelMap : arModelMaps) {
                product.arModels.add(ArModel.fromMap(modelMap));
            }
        }
        
        if (map.get("availableStock") instanceof Long) {
            product.availableStock = ((Long) map.get("availableStock")).intValue();
        } else if (map.get("availableStock") instanceof Integer) {
            product.availableStock = (Integer) map.get("availableStock");
        }
        
        List<String> tags = (List<String>) map.get("tags");
        if (tags != null) {
            product.tags = tags;
        }
        
        return product;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Dimensions getDimensions() {
        return dimensions;
    }
    
    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public List<ArModel> getArModels() {
        return arModels;
    }
    
    public void setArModels(List<ArModel> arModels) {
        this.arModels = arModels;
    }
    
    public int getAvailableStock() {
        return availableStock;
    }
    
    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}