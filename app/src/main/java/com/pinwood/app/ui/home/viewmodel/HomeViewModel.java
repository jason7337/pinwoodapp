package com.pinwood.app.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pinwood.app.data.model.product.Product;
import com.pinwood.app.data.repository.CategoryRepository;
import com.pinwood.app.data.repository.ProductRepository;
import com.pinwood.app.data.repository.PromotionRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MediatorLiveData<List<Product>> featuredProducts = new MediatorLiveData<>();
    private final MediatorLiveData<List<String>> categories = new MediatorLiveData<>();
    private final MediatorLiveData<String> bannerImageUrl = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;

    private LiveData<List<Product>> featuredProductsSource;
    private LiveData<List<String>> categoriesSource;
    private LiveData<String> bannerImageUrlSource;

    public HomeViewModel() {
        // Inicializar valores por defecto
        featuredProducts.setValue(new ArrayList<>());
        categories.setValue(new ArrayList<>());
        bannerImageUrl.setValue("");
        isLoading.setValue(false);
        
        // Inicializar repositorios
        productRepository = ProductRepository.getInstance();
        categoryRepository = CategoryRepository.getInstance();
        promotionRepository = PromotionRepository.getInstance();
        
        // Cargar datos
        loadData();
    }

    public LiveData<List<Product>> getFeaturedProducts() {
        return featuredProducts;
    }

    public LiveData<List<String>> getCategories() {
        return categories;
    }

    public LiveData<String> getBannerImageUrl() {
        return bannerImageUrl;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    private void loadData() {
        isLoading.setValue(true);
        
        try {
            // Si ya había fuentes anteriores, las removemos
            if (featuredProductsSource != null) {
                featuredProducts.removeSource(featuredProductsSource);
            }
            if (categoriesSource != null) {
                categories.removeSource(categoriesSource);
            }
            if (bannerImageUrlSource != null) {
                bannerImageUrl.removeSource(bannerImageUrlSource);
            }
            
            // Obtenemos los productos destacados
            featuredProductsSource = productRepository.getFeaturedProducts();
            featuredProducts.addSource(featuredProductsSource, products -> {
                if (products != null) {
                    featuredProducts.setValue(products);
                } else {
                    featuredProducts.setValue(new ArrayList<>());
                }
                isLoading.setValue(false);
            });
            
            // Obtenemos las categorías
            categoriesSource = categoryRepository.getAllCategories();
            categories.addSource(categoriesSource, categoryList -> {
                if (categoryList != null) {
                    categories.setValue(categoryList);
                } else {
                    categories.setValue(new ArrayList<>());
                }
            });
            
            // Obtenemos la URL del banner
            bannerImageUrlSource = promotionRepository.getActiveBannerImageUrl();
            bannerImageUrl.addSource(bannerImageUrlSource, url -> {
                bannerImageUrl.setValue(url != null ? url : "");
            });
        } catch (Exception e) {
            errorMessage.setValue("Error al cargar datos: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    public void refreshData() {
        loadData();
    }
}