package com.pinwood.app.ui.user.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinwood.app.R;
import com.pinwood.app.data.local.preferences.PreferenceManager;
import com.pinwood.app.ui.home.HomeActivity;
import com.pinwood.app.ui.user.register.RegisterActivity;
import com.pinwood.app.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        
        // Inicializar vistas
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register_prompt);
        
        // Configurar listeners
        btnLogin.setOnClickListener(v -> loginUser());
        tvForgotPassword.setOnClickListener(v -> forgotPassword());
        tvRegister.setOnClickListener(v -> goToRegister());
    }
    
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validar campos
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El correo electrónico es obligatorio");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es obligatoria");
            return;
        }
        
        // Iniciar sesión con Firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Guardar información del usuario en preferencias
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        preferenceManager.saveUserId(userId);
                        preferenceManager.saveUserEmail(email);
                        
                        // Obtener información adicional del usuario desde Firestore
                        getUserData(userId);
                    } else {
                        // Manejar errores de autenticación
                        handleFirebaseAuthError(task.getException());
                    }
                });
    }
    
    private void getUserData(String userId) {
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Guardar nombre de usuario en preferencias
                        String userName = documentSnapshot.getString("name");
                        if (userName != null) {
                            preferenceManager.saveUserName(userName);
                        }
                    }
                    
                    // Ir a la pantalla principal
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    // En caso de error, igualmente ir a la pantalla principal
                    navigateToHome();
                });
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void forgotPassword() {
        // Implementar funcionalidad de recuperación de contraseña
        String email = etEmail.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingresa tu correo electrónico para recuperar tu contraseña");
            return;
        }
        
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void goToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
    
    private void handleFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(LoginActivity.this, "Credenciales inválidas. Revisa tu email y contraseña.", Toast.LENGTH_LONG).show();
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(LoginActivity.this, "Usuario no encontrado. Regístrate primero.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(LoginActivity.this, "Error de autenticación: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}