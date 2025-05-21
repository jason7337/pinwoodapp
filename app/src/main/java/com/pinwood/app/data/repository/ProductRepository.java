package com.pinwood.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinwood.app.data.model.product.Product;

import java.util.List;
import java.util.Map;

public class ProductRepository extends FirestoreRepository {
    private static ProductRepository instance;

    private ProductRepository() {
        // Constructor privado para singleton
    }

    public static synchronized ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }

    public LiveData<List<Product>> getFeaturedProducts() {
        return getCollectionData(
            "products",
            "featured",
            true,
            10,
            Product::fromMap
        );
    }

    public LiveData<Product> getProductById(String productId) {
        MutableLiveData<Product> productData = new MutableLiveData<>();
        
        executor.execute(() -> {
            try {
                Object documentRef = getDocumentReference("products", productId);
                if (documentRef == null) {
                    productData.postValue(null);
                    return;
                }
                
                // Obtener el documento
                Object task = documentRef.getClass().getMethod("get").invoke(documentRef);
                
                // Registrar listeners de éxito y fallo
                Class<?> successListenerClass = Class.forName("com.google.android.gms.tasks.OnSuccessListener");
                Class<?> failureListenerClass = Class.forName("com.google.android.gms.tasks.OnFailureListener");
                
                // Crear listener de éxito
                Object successListener = java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[]{successListenerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("onSuccess")) {
                            Object documentSnapshot = args[0];
                            Boolean exists = (Boolean) documentSnapshot.getClass()
                                .getMethod("exists")
                                .invoke(documentSnapshot);
                                
                            if (exists) {
                                Map<String, Object> data = (Map<String, Object>) documentSnapshot.getClass()
                                    .getMethod("getData")
                                    .invoke(documentSnapshot);
                                    
                                Product product = Product.fromMap(data);
                                productData.postValue(product);
                            } else {
                                productData.postValue(null);
                            }
                        }
                        return null;
                    }
                );
                
                // Crear listener de fallo
                Object failureListener = java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[]{failureListenerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("onFailure")) {
                            productData.postValue(null);
                        }
                        return null;
                    }
                );
                
                // Añadir listeners
                task.getClass()
                    .getMethod("addOnSuccessListener", successListenerClass)
                    .invoke(task, successListener);
                    
                task.getClass()
                    .getMethod("addOnFailureListener", failureListenerClass)
                    .invoke(task, failureListener);
                
            } catch (Exception e) {
                productData.postValue(null);
            }
        });
            
        return productData;
    }

    public LiveData<List<Product>> getProductsByCategory(String category) {
        return getCollectionData(
            "products",
            "category",
            category,
            0,
            Product::fromMap
        );
    }
}