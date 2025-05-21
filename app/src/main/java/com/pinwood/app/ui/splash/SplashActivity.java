package com.pinwood.app.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.pinwood.app.R;
import com.pinwood.app.ui.home.HomeActivity;
import com.pinwood.app.ui.user.login.LoginActivity;
import com.pinwood.app.utils.FirebaseUtil;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Mostrar splash por 2 segundos y luego iniciar la actividad correspondiente
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndNavigate, SPLASH_DELAY);
    }
    
    private void checkUserAndNavigate() {
        try {
            // Verificar si hay un usuario autenticado usando FirebaseUtil
            boolean isAuthenticated = FirebaseUtil.isUserAuthenticated();
            
            Intent intent;
            if (isAuthenticated) {
                // Usuario ya está logueado, ir a HomeActivity
                intent = new Intent(this, HomeActivity.class);
            } else {
                // No hay usuario logueado, ir a LoginActivity
                intent = new Intent(this, LoginActivity.class);
            }
            
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar usuario: " + e.getMessage());
            // Si hay un error con Firebase Auth, ir directamente a la actividad Home
            startActivity(new Intent(this, HomeActivity.class));
        } finally {
            finish(); // Cerrar esta actividad para que no se pueda volver a ella con el botón atrás
        }
    }
}