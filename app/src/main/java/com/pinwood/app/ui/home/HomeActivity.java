package com.pinwood.app.ui.home;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.pinwood.app.R;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);
        
        // Por defecto, cargar el fragmento de inicio
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.navigation_categories) {
            // Aquí se cargará el fragmento de categorías cuando esté implementado
            fragment = new HomeFragment(); // Provisional
        } else if (itemId == R.id.navigation_cart) {
            // Aquí se cargará el fragmento de carrito cuando esté implementado
            fragment = new HomeFragment(); // Provisional
        } else if (itemId == R.id.navigation_profile) {
            // Aquí se cargará el fragmento de perfil cuando esté implementado
            fragment = new HomeFragment(); // Provisional
        }
        
        return loadFragment(fragment);
    }
    
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}