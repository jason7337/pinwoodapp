package com.pinwood.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pinwood.app.R;
import com.pinwood.app.ui.home.adapter.CategoryAdapter;
import com.pinwood.app.ui.home.adapter.ProductAdapter;
import com.pinwood.app.ui.home.viewmodel.HomeViewModel;
import com.pinwood.app.utils.ImageLoader;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView featuredProductsRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private ImageView bannerImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Inicializar vistas
        initViews(view);
        
        // Configurar RecyclerViews
        setupRecyclerViews();
        
        // Configurar ViewModel
        setupViewModel();
        
        // Configurar listeners
        setupListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        categoriesRecyclerView = view.findViewById(R.id.categories_recycler_view);
        featuredProductsRecyclerView = view.findViewById(R.id.featured_products_recycler_view);
        bannerImageView = view.findViewById(R.id.banner_image);
    }
    
    private void setupRecyclerViews() {
        // Configurar adaptador de categorías
        categoryAdapter = new CategoryAdapter(requireContext());
        categoriesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriesRecyclerView.setAdapter(categoryAdapter);
        
        // Configurar adaptador de productos
        productAdapter = new ProductAdapter(requireContext());
        featuredProductsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        featuredProductsRecyclerView.setAdapter(productAdapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, 
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
            .get(HomeViewModel.class);
        
        // Observar categorías
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setCategories(categories);
        });
        
        // Observar productos destacados
        viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.setProducts(products);
        });
        
        // Observar banner
        viewModel.getBannerImageUrl().observe(getViewLifecycleOwner(), bannerUrl -> {
            if (bannerUrl != null && !bannerUrl.isEmpty()) {
                ImageLoader.loadImage(requireContext(), bannerUrl, bannerImageView, R.drawable.pinwoodapplogo);
            }
        });
        
        // Observar errores
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void setupListeners() {
        // Click en categoría
        categoryAdapter.setOnCategoryClickListener(category -> {
            Toast.makeText(requireContext(), "Categoría seleccionada: " + category, Toast.LENGTH_SHORT).show();
            // Implementar navegación a pantalla de categoría
        });
        
        // Click en producto
        productAdapter.setOnProductClickListener(product -> {
            Toast.makeText(requireContext(), "Producto seleccionado: " + product.getName(), Toast.LENGTH_SHORT).show();
            // Implementar navegación a pantalla de detalle del producto
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos cuando el fragmento se reanuda
        viewModel.refreshData();
    }
}