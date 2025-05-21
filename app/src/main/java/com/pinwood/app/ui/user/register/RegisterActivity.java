package com.pinwood.app.ui.user.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinwood.app.R;
import com.pinwood.app.data.local.preferences.PreferenceManager;
import com.pinwood.app.data.model.user.User;
import com.pinwood.app.ui.home.HomeActivity;
import com.pinwood.app.ui.user.login.LoginActivity;
import com.pinwood.app.utils.Constants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginPrompt;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        
        // Inicializar vistas
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginPrompt = findViewById(R.id.tv_login_prompt);
        
        // Configurar listeners
        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginPrompt.setOnClickListener(v -> goToLogin());
    }
    
    private void registerUser() {
        // Obtener valores de los campos
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Validar campos
        if (TextUtils.isEmpty(name)) {
            etName.setError("El nombre es obligatorio");
            return;
        }
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El correo electrónico es obligatorio");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es obligatoria");
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }
        
        // Crear usuario en Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtener el ID del usuario creado
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        
                        // Crear documento del usuario en Firestore
                        createUserInFirestore(userId, name, email);
                    } else {
                        // Manejar error en la creación del usuario
                        Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void createUserInFirestore(String userId, String name, String email) {
        // Crear objeto de usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("createdAt", new Date());
        
        // Guardar en Firestore
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Guardar datos en preferencias
                    preferenceManager.saveUserId(userId);
                    preferenceManager.saveUserName(name);
                    preferenceManager.saveUserEmail(email);
                    
                    // Ir a la pantalla principal
                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    // Manejar error en la creación del documento
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void goToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}