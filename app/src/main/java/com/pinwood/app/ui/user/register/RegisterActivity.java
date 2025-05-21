package com.pinwood.app.ui.user.register;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pinwood.app.R;
import com.pinwood.app.data.local.preferences.PreferenceManager;
import com.pinwood.app.data.model.user.User;
import com.pinwood.app.ui.home.HomeActivity;
import com.pinwood.app.ui.user.login.LoginActivity;
import com.pinwood.app.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private FloatingActionButton fabAddPhoto;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword, etPhone;
    private Button btnRegister;
    private TextView tvLoginPrompt;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private PreferenceManager preferenceManager;
    
    private Uri imageUri;
    private String currentPhotoPath;
    private boolean hasProfileImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Inicializar componentes
        initializeComponents();
        
        // Configurar listeners
        setupListeners();
    }
    
    private void initializeComponents() {
        profileImage = findViewById(R.id.profile_image);
        fabAddPhoto = findViewById(R.id.fab_add_photo);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginPrompt = findViewById(R.id.tv_login_prompt);
        
        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        preferenceManager = new PreferenceManager(this);
    }
    
    private void setupListeners() {
        fabAddPhoto.setOnClickListener(v -> showImagePickerDialog());
        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginPrompt.setOnClickListener(v -> goToLogin());
    }
    
    private void showImagePickerDialog() {
        String[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery)};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_profile_photo));
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Tomar foto con cámara
                if (checkCameraPermission()) {
                    openCamera();
                }
            } else {
                // Seleccionar de la galería
                if (checkStoragePermission()) {
                    openGallery();
                }
            }
        });
        builder.show();
    }
    
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    Constants.REQUEST_CODE_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }
    
    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    Constants.REQUEST_CODE_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            }
            
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.pinwood.app.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_CAMERA);
            }
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Constants.REQUEST_CODE_GALLERY);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_CAMERA) {
                if (currentPhotoPath != null) {
                    File file = new File(currentPhotoPath);
                    imageUri = Uri.fromFile(file);
                    profileImage.setImageURI(imageUri);
                    hasProfileImage = true;
                }
            } else if (requestCode == Constants.REQUEST_CODE_GALLERY && data != null) {
                imageUri = data.getData();
                profileImage.setImageURI(imageUri);
                hasProfileImage = true;
            }
        }
    }
    
    private void registerUser() {
        // Obtener valores de los campos
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
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
        
        // Deshabilitar botón y mostrar progreso
        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");
        
        // Crear usuario en Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtener el ID del usuario creado
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        
                        // Si hay imagen de perfil, subirla primero
                        if (hasProfileImage && imageUri != null) {
                            uploadProfileImage(userId, name, email, phone);
                        } else {
                            // Si no hay imagen, crear el documento directamente
                            createUserInFirestore(userId, name, email, phone, null);
                        }
                    } else {
                        // Manejar error en la creación del usuario
                        btnRegister.setEnabled(true);
                        btnRegister.setText("CREAR CUENTA");
                        Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void uploadProfileImage(String userId, String name, String email, String phone) {
        String fileExtension = getFileExtension(imageUri);
        if (fileExtension == null) {
            // Si no se puede determinar la extensión, crear usuario sin imagen
            createUserInFirestore(userId, name, email, phone, null);
            return;
        }
        
        StorageReference fileReference = storageReference
                .child(Constants.STORAGE_PROFILE_IMAGES)
                .child(userId + "." + fileExtension);
        
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            createUserInFirestore(userId, name, email, phone, imageUrl);
                        })
                        .addOnFailureListener(e -> {
                            // Si hay error al obtener URL, crear usuario sin imagen
                            createUserInFirestore(userId, name, email, phone, null);
                        }))
                .addOnFailureListener(e -> {
                    // Si hay error al subir, crear usuario sin imagen
                    createUserInFirestore(userId, name, email, phone, null);
                });
    }
    
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    
    private void createUserInFirestore(String userId, String name, String email, String phone, String profileImageUrl) {
        // Crear objeto de usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("createdAt", new Date());
        
        if (phone != null && !phone.isEmpty()) {
            userData.put("phone", phone);
        }
        
        if (profileImageUrl != null) {
            userData.put("profileImageUrl", profileImageUrl);
        }
        
        // Guardar en Firestore
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Guardar datos en preferencias
                    preferenceManager.saveUserId(userId);
                    preferenceManager.saveUserName(name);
                    preferenceManager.saveUserEmail(email);
                    
                    if (phone != null && !phone.isEmpty()) {
                        preferenceManager.saveUserPhone(phone);
                    }
                    
                    if (profileImageUrl != null) {
                        preferenceManager.saveProfileImageUrl(profileImageUrl);
                    }
                    
                    // Ir a la pantalla principal
                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    // Manejar error en la creación del documento
                    btnRegister.setEnabled(true);
                    btnRegister.setText("CREAR CUENTA");
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
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
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        } else if (requestCode == Constants.REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }
}