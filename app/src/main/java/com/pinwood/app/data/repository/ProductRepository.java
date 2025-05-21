package com.pinwood.app.data.repository;

import android.content.Context;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.pinwood.app.data.local.preferences.PreferenceManager;
import com.pinwood.app.data.model.product.Product;
import com.pinwood.app.utils.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Repositorio que gestiona los productos de la aplicación.
 * Implementa patrón Repository para proporcionar una API limpia al ViewModel.
 * Coordina entre fuentes de datos remotas (Firestore) y caché local.
 */
public class ProductRepository extends FirestoreRepository {
    private static final String TAG = "ProductRepository";
    private static ProductRepository instance;
    
    private Context context;
    private PreferenceManager preferenceManager;
    
    // Caché en memoria para productos
    private final Map<String, Product> productCache = new HashMap<>();
    private final Map<String, List<Product>> categoryProductsCache = new HashMap<>();
    private final Map<String, Long> cacheTimes = new HashMap<>();
    
    // Tiempo máximo de caché en milisegundos (30 minutos)
    private static final long CACHE_EXPIRATION = TimeUnit.MINUTES.toMillis(30);
    
    // Constructor privado para implementar singleton
    private ProductRepository(Context context) {
        this.context = context.getApplicationContext();
        this.preferenceManager = new PreferenceManager(this.context);
    }
    
    public static synchronized ProductRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ProductRepository(context);
        }
        return instance;
    }
    
    /**
     * Verifica si hay conexión a internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    /**
     * Verifica si la caché ha expirado
     */
    private boolean isCacheValid(String cacheKey) {
        Long cacheTime = cacheTimes.get(cacheKey);
        if (cacheTime == null) {
            return false;
        }
        return System.currentTimeMillis() - cacheTime < CACHE_EXPIRATION;
    }
    
    /**
     * Guarda un producto en la caché
     */
    private void cacheProduct(Product product) {
        if (product != null && product.getProductId() != null) {
            productCache.put(product.getProductId(), product);
            cacheTimes.put("product_" + product.getProductId(), System.currentTimeMillis());
        }
    }
    
    /**
     * Guarda una lista de productos en caché por categoría
     */
    private void cacheProductsByCategory(String category, List<Product> products) {
        if (category != null && products != null) {
            categoryProductsCache.put(category, new ArrayList<>(products));
            cacheTimes.put("category_" + category, System.currentTimeMillis());
            
            // También guardamos los productos individuales
            for (Product product : products) {
                cacheProduct(product);
            }
        }
    }
    
    /**
     * Obtiene un producto por ID
     * 1. Intenta primero desde la caché
     * 2. Si no está o expiró, lo obtiene de Firestore
     * 3. Lo guarda en caché si fue exitoso
     * 4. Notifica con LiveData
     */
    public LiveData<Product> getProductById(String productId) {
        final MutableLiveData<Product> productData = new MutableLiveData<>();
        
        // Verificar si está en caché y es válido
        if (productCache.containsKey(productId) && isCacheValid("product_" + productId)) {
            productData.postValue(productCache.get(productId));
            return productData;
        }
        
        // Si no hay red, intentamos usar caché aunque haya expirado
        if (!isNetworkAvailable() && productCache.containsKey(productId)) {
            productData.postValue(productCache.get(productId));
            return productData;
        }
        
        // Obtener desde Firestore
        executor.execute(() -> {
            try {
                Object documentRef = getDocumentReference(Constants.COLLECTION_PRODUCTS, productId);
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
                                    
                                // Asegurarse de que el productId está en el mapa
                                if (!data.containsKey("productId")) {
                                    data.put("productId", productId);
                                }
                                
                                Product product = Product.fromMap(data);
                                
                                // Guardar en caché
                                cacheProduct(product);
                                
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
                            Exception exception = (Exception) args[0];
                            Log.e(TAG, "Error al obtener producto: " + exception.getMessage());
                            
                            // Si hay error de red pero tenemos caché, usamos la caché aunque haya expirado
                            if (productCache.containsKey(productId)) {
                                productData.postValue(productCache.get(productId));
                            } else {
                                productData.postValue(null);
                            }
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
                Log.e(TAG, "Error en getProductById: " + e.getMessage());
                
                // Si hay error pero tenemos caché, usamos la caché aunque haya expirado
                if (productCache.containsKey(productId)) {
                    productData.postValue(productCache.get(productId));
                } else {
                    productData.postValue(null);
                }
            }
        });
            
        return productData;
    }
    
    /**
     * Obtiene productos por categoría
     */
    public LiveData<List<Product>> getProductsByCategory(String category) {
        final MutableLiveData<List<Product>> productsData = new MutableLiveData<>();
        
        // Verificar si está en caché y es válido
        if (categoryProductsCache.containsKey(category) && isCacheValid("category_" + category)) {
            productsData.postValue(categoryProductsCache.get(category));
            return productsData;
        }
        
        // Si no hay red, intentamos usar caché aunque haya expirado
        if (!isNetworkAvailable() && categoryProductsCache.containsKey(category)) {
            productsData.postValue(categoryProductsCache.get(category));
            return productsData;
        }
        
        // Obtener desde Firestore
        return getCollectionDataWithCache(
            Constants.COLLECTION_PRODUCTS,
            "category",
            category,
            0,
            Product::fromMap,
            products -> {
                // Guardar en caché
                cacheProductsByCategory(category, products);
            }
        );
    }
    
    /**
     * Obtiene productos destacados
     */
    public LiveData<List<Product>> getFeaturedProducts() {
        final String FEATURED_CACHE_KEY = "featured_products";
        final MutableLiveData<List<Product>> productsData = new MutableLiveData<>();
        
        // Verificar si está en caché y es válido
        if (categoryProductsCache.containsKey(FEATURED_CACHE_KEY) && isCacheValid(FEATURED_CACHE_KEY)) {
            productsData.postValue(categoryProductsCache.get(FEATURED_CACHE_KEY));
            return productsData;
        }
        
        // Si no hay red, intentamos usar caché aunque haya expirado
        if (!isNetworkAvailable() && categoryProductsCache.containsKey(FEATURED_CACHE_KEY)) {
            productsData.postValue(categoryProductsCache.get(FEATURED_CACHE_KEY));
            return productsData;
        }
        
        // Obtener desde Firestore
        return getCollectionDataWithCache(
            Constants.COLLECTION_PRODUCTS,
            "featured",
            true,
            10,
            Product::fromMap,
            products -> {
                // Guardar en caché
                categoryProductsCache.put(FEATURED_CACHE_KEY, new ArrayList<>(products));
                cacheTimes.put(FEATURED_CACHE_KEY, System.currentTimeMillis());
                
                // También guardamos los productos individuales
                for (Product product : products) {
                    cacheProduct(product);
                }
            }
        );
    }
    
    /**
     * Obtiene productos populares (por ejemplo, los más vendidos)
     */
    public LiveData<List<Product>> getPopularProducts(int limit) {
        final String POPULAR_CACHE_KEY = "popular_products";
        final MutableLiveData<List<Product>> productsData = new MutableLiveData<>();
        
        // Verificar si está en caché y es válido
        if (categoryProductsCache.containsKey(POPULAR_CACHE_KEY) && isCacheValid(POPULAR_CACHE_KEY)) {
            productsData.postValue(categoryProductsCache.get(POPULAR_CACHE_KEY));
            return productsData;
        }
        
        // Si no hay red, intentamos usar caché aunque haya expirado
        if (!isNetworkAvailable() && categoryProductsCache.containsKey(POPULAR_CACHE_KEY)) {
            productsData.postValue(categoryProductsCache.get(POPULAR_CACHE_KEY));
            return productsData;
        }
        
        // Obtener desde Firestore
        return getCollectionDataWithCache(
            Constants.COLLECTION_PRODUCTS,
            "popular",
            true,
            limit,
            Product::fromMap,
            products -> {
                // Guardar en caché
                categoryProductsCache.put(POPULAR_CACHE_KEY, new ArrayList<>(products));
                cacheTimes.put(POPULAR_CACHE_KEY, System.currentTimeMillis());
                
                // También guardamos los productos individuales
                for (Product product : products) {
                    cacheProduct(product);
                }
            }
        );
    }
    
    /**
     * Obtiene productos nuevos (últimas adiciones)
     */
    public LiveData<List<Product>> getNewProducts(int limit) {
        final String NEW_CACHE_KEY = "new_products";
        final MutableLiveData<List<Product>> productsData = new MutableLiveData<>();
        
        // Verificar si está en caché y es válido
        if (categoryProductsCache.containsKey(NEW_CACHE_KEY) && isCacheValid(NEW_CACHE_KEY)) {
            productsData.postValue(categoryProductsCache.get(NEW_CACHE_KEY));
            return productsData;
        }
        
        // Si no hay red, intentamos usar caché aunque haya expirado
        if (!isNetworkAvailable() && categoryProductsCache.containsKey(NEW_CACHE_KEY)) {
            productsData.postValue(categoryProductsCache.get(NEW_CACHE_KEY));
            return productsData;
        }
        
        // Obtener desde Firestore y ordenar por fecha
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Object collectionRef = getCollectionReference(Constants.COLLECTION_PRODUCTS);
                if (collectionRef == null) {
                    liveData.postValue(new ArrayList<>());
                    return;
                }
                
                // Obtener query con ordenamiento por fecha
                Class<?> queryClass = Class.forName("com.google.firebase.firestore.Query");
                Object query = collectionRef;
                
                // Ordenar por timestamp descendente
                Method orderByMethod = queryClass.getMethod("orderBy", String.class);
                query = orderByMethod.invoke(query, "timestamp");
                
                Method descMethod = query.getClass().getMethod("descending");
                query = descMethod.invoke(query);
                
                // Aplicar límite
                if (limit > 0) {
                    Method limitMethod = query.getClass().getMethod("limit", long.class);
                    query = limitMethod.invoke(query, (long) limit);
                }
                
                // Obtener datos
                Class<?> taskClass = Class.forName("com.google.android.gms.tasks.Task");
                Method getMethod = query.getClass().getMethod("get");
                Object task = getMethod.invoke(query);
                
                // Añadir listener de completado
                Class<?> completeListenerClass = Class.forName("com.google.android.gms.tasks.OnCompleteListener");
                Object completeListener = java.lang.reflect.Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class<?>[]{completeListenerClass},
                        (proxy, method, args) -> {
                            if (method.getName().equals("onComplete")) {
                                Object taskResult = args[0];
                                Boolean isSuccessful = (Boolean) taskResult.getClass()
                                        .getMethod("isSuccessful")
                                        .invoke(taskResult);
                                
                                if (isSuccessful) {
                                    Object querySnapshot = taskResult.getClass()
                                            .getMethod("getResult")
                                            .invoke(taskResult);
                                    
                                    Method getDocumentsMethod = querySnapshot.getClass()
                                            .getMethod("getDocuments");
                                    List<?> documents = (List<?>) getDocumentsMethod.invoke(querySnapshot);
                                    
                                    List<Product> resultList = new ArrayList<>();
                                    for (Object doc : documents) {
                                        Method getDataMethod = doc.getClass().getMethod("getData");
                                        Map<String, Object> data = (Map<String, Object>) getDataMethod.invoke(doc);
                                        
                                        Method getIdMethod = doc.getClass().getMethod("getId");
                                        String id = (String) getIdMethod.invoke(doc);
                                        
                                        // Añadir ID al mapa de datos si no existe
                                        if (!data.containsKey("productId")) {
                                            data.put("productId", id);
                                        }
                                        
                                        Product item = Product.fromMap(data);
                                        if (item != null) {
                                            resultList.add(item);
                                        }
                                    }
                                    
                                    // Guardar en caché
                                    categoryProductsCache.put(NEW_CACHE_KEY, new ArrayList<>(resultList));
                                    cacheTimes.put(NEW_CACHE_KEY, System.currentTimeMillis());
                                    
                                    // También guardamos los productos individuales
                                    for (Product product : resultList) {
                                        cacheProduct(product);
                                    }
                                    
                                    liveData.postValue(resultList);
                                } else {
                                    // Si hay error pero tenemos caché, usamos la caché aunque haya expirado
                                    if (categoryProductsCache.containsKey(NEW_CACHE_KEY)) {
                                        liveData.postValue(categoryProductsCache.get(NEW_CACHE_KEY));
                                    } else {
                                        liveData.postValue(new ArrayList<>());
                                    }
                                }
                            }
                            return null;
                        });
                
                Method addOnCompleteListenerMethod = taskClass.getMethod("addOnCompleteListener", completeListenerClass);
                addOnCompleteListenerMethod.invoke(task, completeListener);
                
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener nuevos productos: " + e.getMessage());
                
                // Si hay error pero tenemos caché, usamos la caché aunque haya expirado
                if (categoryProductsCache.containsKey(NEW_CACHE_KEY)) {
                    liveData.postValue(categoryProductsCache.get(NEW_CACHE_KEY));
                } else {
                    liveData.postValue(new ArrayList<>());
                }
            }
        });
        
        return liveData;
    }
    
    /**
     * Busca productos por palabra clave en nombre, descripción o tags
     */
    public LiveData<List<Product>> searchProducts(String query) {
        final MediatorLiveData<List<Product>> searchResults = new MediatorLiveData<>();
        
        // Si no hay conexión, buscamos en caché
        if (!isNetworkAvailable()) {
            List<Product> results = new ArrayList<>();
            for (Product product : productCache.values()) {
                if (matchesSearchQuery(product, query)) {
                    results.add(product);
                }
            }
            searchResults.setValue(results);
            return searchResults;
        }
        
        // Obtener todos los productos para filtrar manualmente
        // En una implementación real, se debería usar Cloud Functions o Algolia
        LiveData<List<Product>> allProductsLiveData = getAllProducts();
        
        searchResults.addSource(allProductsLiveData, products -> {
            if (products != null) {
                List<Product> filteredProducts = new ArrayList<>();
                for (Product product : products) {
                    if (matchesSearchQuery(product, query)) {
                        filteredProducts.add(product);
                    }
                }
                searchResults.setValue(filteredProducts);
            }
        });
        
        return searchResults;
    }
    
    /**
     * Verifica si un producto coincide con la búsqueda
     */
    private boolean matchesSearchQuery(Product product, String query) {
        if (query == null || query.trim().isEmpty()) return false;
        
        String lowerQuery = query.toLowerCase();
        
        // Buscar en nombre
        if (product.getName() != null && product.getName().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Buscar en descripción
        if (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Buscar en tags
        if (product.getTags() != null) {
            for (String tag : product.getTags()) {
                if (tag.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Obtiene todos los productos
     */
    public LiveData<List<Product>> getAllProducts() {
        final String ALL_CACHE_KEY = "all_products";
        
        // Verificar si está en caché y es válido
        if (categoryProductsCache.containsKey(ALL_CACHE_KEY) && isCacheValid(ALL_CACHE_KEY)) {
            MutableLiveData<List<Product>> cachedData = new MutableLiveData<>();
            cachedData.setValue(categoryProductsCache.get(ALL_CACHE_KEY));
            return cachedData;
        }
        
        // Obtener desde Firestore
        return getCollectionDataWithCache(
            Constants.COLLECTION_PRODUCTS,
            null,
            null,
            0,
            Product::fromMap,
            products -> {
                // Guardar en caché
                categoryProductsCache.put(ALL_CACHE_KEY, new ArrayList<>(products));
                cacheTimes.put(ALL_CACHE_KEY, System.currentTimeMillis());
                
                // También guardamos los productos individuales
                for (Product product : products) {
                    cacheProduct(product);
                }
            }
        );
    }
    
    /**
     * Versión mejorada de getCollectionData que incluye caché
     */
    private <T> LiveData<List<T>> getCollectionDataWithCache(
            String collectionPath, 
            String whereField, 
            Object whereValue, 
            int limit,
            DocumentConverter<T> converter,
            CacheCallback<T> cacheCallback) {
        
        MutableLiveData<List<T>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Object collectionRef = getCollectionReference(collectionPath);
                if (collectionRef == null) {
                    liveData.postValue(new ArrayList<>());
                    return;
                }
                
                // Aplicar filtro si es necesario
                Object query = collectionRef;
                if (whereField != null && !whereField.isEmpty()) {
                    Class<?> objectClass = Class.forName("java.lang.Object");
                    Method whereEqualToMethod = query.getClass()
                            .getMethod("whereEqualTo", String.class, objectClass);
                    query = whereEqualToMethod.invoke(query, whereField, whereValue);
                }
                
                // Aplicar límite si es necesario
                if (limit > 0) {
                    Method limitMethod = query.getClass().getMethod("limit", long.class);
                    query = limitMethod.invoke(query, (long) limit);
                }
                
                // Obtener datos
                Class<?> taskClass = Class.forName("com.google.android.gms.tasks.Task");
                Method getMethod = query.getClass().getMethod("get");
                Object task = getMethod.invoke(query);
                
                // Añadir listener de completado
                Class<?> completeListenerClass = Class.forName("com.google.android.gms.tasks.OnCompleteListener");
                Object completeListener = java.lang.reflect.Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class<?>[]{completeListenerClass},
                        (proxy, method, args) -> {
                            if (method.getName().equals("onComplete")) {
                                Object taskResult = args[0];
                                Boolean isSuccessful = (Boolean) taskResult.getClass()
                                        .getMethod("isSuccessful")
                                        .invoke(taskResult);
                                
                                if (isSuccessful) {
                                    Object querySnapshot = taskResult.getClass()
                                            .getMethod("getResult")
                                            .invoke(taskResult);
                                    
                                    Method getDocumentsMethod = querySnapshot.getClass()
                                            .getMethod("getDocuments");
                                    List<?> documents = (List<?>) getDocumentsMethod.invoke(querySnapshot);
                                    
                                    List<T> resultList = new ArrayList<>();
                                    for (Object doc : documents) {
                                        Method getDataMethod = doc.getClass().getMethod("getData");
                                        Map<String, Object> data = (Map<String, Object>) getDataMethod.invoke(doc);
                                        
                                        Method getIdMethod = doc.getClass().getMethod("getId");
                                        String id = (String) getIdMethod.invoke(doc);
                                        
                                        // Añadir ID al mapa de datos si no existe
                                        if (!data.containsKey("id")) {
                                            data.put("id", id);
                                        }
                                        
                                        T item = converter.convert(data);
                                        if (item != null) {
                                            resultList.add(item);
                                        }
                                    }
                                    
                                    // Guardar en caché
                                    if (cacheCallback != null) {
                                        cacheCallback.onCacheData(resultList);
                                    }
                                    
                                    liveData.postValue(resultList);
                                } else {
                                    Exception exception = (Exception) taskResult.getClass()
                                            .getMethod("getException")
                                            .invoke(taskResult);
                                            
                                    Log.e(TAG, "Error en getCollectionDataWithCache: " + 
                                          (exception != null ? exception.getMessage() : "Desconocido"));
                                    liveData.postValue(new ArrayList<>());
                                }
                            }
                            return null;
                        });
                
                Method addOnCompleteListenerMethod = taskClass.getMethod("addOnCompleteListener", completeListenerClass);
                addOnCompleteListenerMethod.invoke(task, completeListener);
                
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener datos de la colección: " + e.getMessage());
                liveData.postValue(new ArrayList<>());
            }
        });
        
        return liveData;
    }
    
    /**
     * Interfaz para manejar el guardado en caché
     */
    private interface CacheCallback<T> {
        void onCacheData(List<T> data);
    }
    
    /**
     * Limpia toda la caché
     */
    public void clearCache() {
        productCache.clear();
        categoryProductsCache.clear();
        cacheTimes.clear();
    }
    
    /**
     * Actualiza la caché de un producto específico
     */
    public void refreshProduct(String productId) {
        productCache.remove(productId);
        cacheTimes.remove("product_" + productId);
        getProductById(productId);
    }
    
    /**
     * Actualiza la caché de productos por categoría
     */
    public void refreshCategoryProducts(String category) {
        categoryProductsCache.remove(category);
        cacheTimes.remove("category_" + category);
        getProductsByCategory(category);
    }
    
    /**
     * Actualiza toda la caché de productos
     */
    public void refreshAllProducts() {
        clearCache();
        getAllProducts();
    }
}