package com.pinwood.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryRepository extends FirestoreRepository {
    private static CategoryRepository instance;

    private CategoryRepository() {
        // Constructor privado para singleton
    }

    public static synchronized CategoryRepository getInstance() {
        if (instance == null) {
            instance = new CategoryRepository();
        }
        return instance;
    }

    public LiveData<List<String>> getAllCategories() {
        MutableLiveData<List<String>> categoriesLiveData = new MutableLiveData<>();
        
        executor.execute(() -> {
            try {
                // Obtener todas las categorías
                LiveData<List<Map<String, Object>>> result = getCollectionData(
                    "categories",
                    null,
                    null,
                    0,
                    data -> data // Devolver el mapa completo
                );
                
                // Observar el resultado y extraer los nombres de categoría
                result.observeForever(categoryMaps -> {
                    List<String> categoryNames = new ArrayList<>();
                    
                    for (Map<String, Object> categoryMap : categoryMaps) {
                        String name = (String) categoryMap.get("name");
                        if (name != null && !name.isEmpty()) {
                            categoryNames.add(name);
                        }
                    }
                    
                    categoriesLiveData.postValue(categoryNames);
                });
            } catch (Exception e) {
                categoriesLiveData.postValue(new ArrayList<>());
            }
        });
            
        return categoriesLiveData;
    }
}