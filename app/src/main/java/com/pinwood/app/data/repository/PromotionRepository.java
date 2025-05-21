package com.pinwood.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Map;

public class PromotionRepository extends FirestoreRepository {
    private static PromotionRepository instance;

    private PromotionRepository() {
        // Constructor privado para singleton
    }

    public static synchronized PromotionRepository getInstance() {
        if (instance == null) {
            instance = new PromotionRepository();
        }
        return instance;
    }

    public LiveData<String> getActiveBannerImageUrl() {
        MutableLiveData<String> bannerUrlLiveData = new MutableLiveData<>();
        
        executor.execute(() -> {
            try {
                // Obtener promociones activas
                LiveData<List<Map<String, Object>>> result = getCollectionData(
                    "promotions",
                    "active",
                    true,
                    1,
                    data -> data // Devolver el mapa completo
                );
                
                // Observar el resultado y extraer la URL de la imagen
                result.observeForever(promotions -> {
                    if (promotions != null && !promotions.isEmpty()) {
                        String url = (String) promotions.get(0).get("imageUrl");
                        bannerUrlLiveData.postValue(url);
                    } else {
                        bannerUrlLiveData.postValue("");
                    }
                });
            } catch (Exception e) {
                bannerUrlLiveData.postValue("");
            }
        });
            
        return bannerUrlLiveData;
    }
}