package com.pinwood.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.pinwood.app.R;
import com.pinwood.app.data.local.preferences.PreferenceManager;
import com.pinwood.app.ui.user.login.LoginActivity;
import com.pinwood.app.ui.user.profile.ProfileFragment;
import com.pinwood.app.utils.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements 
        NavigationBarView.OnItemSelectedListener, 
        NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private PreferenceManager preferenceManager;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Inicializar componentes
        setupComponents();
        
        // Configurar la toolbar
        setSupportActionBar(toolbar);
        
        // Configurar el Navigation Drawer
        setupDrawer();
        
        // Configurar Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(this);
        
        // Actualizar la información del usuario en el header
        updateNavigationHeader();
        
        // Por defecto, cargar el fragmento de inicio
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Inicio");
            }
        }
    }
    
    private void setupComponents() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
        preferenceManager = new PreferenceManager(this);
        firebaseAuth = FirebaseAuth.getInstance();
    }
    
    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    
    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.nav_header_name);
        TextView tvEmail = headerView.findViewById(R.id.nav_header_email);
        CircleImageView imageProfile = headerView.findViewById(R.id.profile_image);
        
        // Establecer nombre y correo desde las preferencias
        tvName.setText(preferenceManager.getUserName());
        tvEmail.setText(preferenceManager.getUserEmail());
        
        // Cargar imagen de perfil si existe
        String profileImageUrl = preferenceManager.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            ImageLoader.loadImage(this, profileImageUrl, imageProfile, R.drawable.ic_profile);
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = "";
        
        int itemId = item.getItemId();
        
        // Manejar selección de elementos del Navigation Drawer
        if (itemId == R.id.nav_home || itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
            title = "Inicio";
        } else if (itemId == R.id.nav_categories || itemId == R.id.navigation_categories) {
            // Aquí se cargará el fragmento de categorías cuando esté implementado
            fragment = new HomeFragment(); // Provisional
            title = "Categorías";
        } else if (itemId == R.id.nav_cart || itemId == R.id.navigation_cart) {
            // Aquí se cargará el fragmento de carrito cuando esté implementado
            fragment = new HomeFragment(); // Provisional
            title = "Carrito";
        } else if (itemId == R.id.nav_profile || itemId == R.id.navigation_profile) {
            fragment = new ProfileFragment();
            title = "Perfil";
        } else if (itemId == R.id.nav_orders) {
            // Fragmento de pedidos pendiente de implementación
            fragment = new HomeFragment(); // Provisional
            title = "Mis Pedidos";
        } else if (itemId == R.id.nav_settings) {
            // Fragmento de configuración pendiente de implementación
            fragment = new HomeFragment(); // Provisional
            title = "Configuración";
        } else if (itemId == R.id.nav_logout) {
            logoutUser();
            return true;
        }
        
        // Actualizar título de la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        
        // Cargar el fragmento seleccionado
        boolean result = loadFragment(fragment);
        
        // Si la selección fue desde el navigation drawer, cerrar el drawer
        if (itemId == R.id.nav_home || itemId == R.id.nav_categories || 
            itemId == R.id.nav_cart || itemId == R.id.nav_profile || 
            itemId == R.id.nav_orders || itemId == R.id.nav_settings) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        
        return result;
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
    
    private void logoutUser() {
        firebaseAuth.signOut();
        preferenceManager.clearUserData();
        
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}