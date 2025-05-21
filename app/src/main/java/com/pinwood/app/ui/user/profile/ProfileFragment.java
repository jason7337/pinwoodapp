package com.pinwood.app.ui.user.profile;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pinwood.app.R;
import com.pinwood.app.data.local.preferences.PreferenceManager;
import com.pinwood.app.data.model.user.User;
import com.pinwood.app.ui.user.login.LoginActivity;
import com.pinwood.app.utils.Constants;
import com.pinwood.app.utils.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private FloatingActionButton fabChangePhoto;
    private TextInputEditText etName, etEmail, etPhone;
    private Button btnSaveProfile, btnDeleteAccount;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private PreferenceManager preferenceManager;
    
    private Uri imageUri;
    private String currentPhotoPath;
    private boolean photoChanged = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeComponents(view);
        setupListeners();
        loadUserData();
        return view;
    }
    
    private void initializeComponents(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        fabChangePhoto = view.findViewById(R.id.fab_change_photo);
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        
        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        
        // Inicializar PreferenceManager
        preferenceManager = new PreferenceManager(requireContext());
    }
    
    private void setupListeners() {
        fabChangePhoto.setOnClickListener(v -> showImagePickerDialog());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }
    
    private void loadUserData() {
        String userId = preferenceManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Error al cargar datos de usuario", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Cargar datos desde preferencias
        etName.setText(preferenceManager.getUserName());
        etEmail.setText(preferenceManager.getUserEmail());
        etPhone.setText(preferenceManager.getUserPhone());
        
        // Cargar imagen de perfil si existe
        String profileImageUrl = preferenceManager.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            ImageLoader.loadImage(requireContext(), profileImageUrl, profileImage, R.drawable.ic_profile);
        }
        
        // Obtener datos actualizados desde Firestore
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = User.fromMap(documentSnapshot.getData());
                        
                        // Actualizar datos
                        if (user.getName() != null) {
                            etName.setText(user.getName());
                            preferenceManager.saveUserName(user.getName());
                        }
                        
                        if (user.getEmail() != null) {
                            etEmail.setText(user.getEmail());
                            preferenceManager.saveUserEmail(user.getEmail());
                        }
                        
                        if (user.getPhone() != null) {
                            etPhone.setText(user.getPhone());
                            preferenceManager.saveUserPhone(user.getPhone());
                        }
                        
                        if (user.getProfileImageUrl() != null) {
                            ImageLoader.loadImage(requireContext(), user.getProfileImageUrl(), 
                                    profileImage, R.drawable.ic_profile);
                            preferenceManager.saveProfileImageUrl(user.getProfileImageUrl());
                        }
                    }
                })
                .addOnFailureListener(e -> 
                        Toast.makeText(requireContext(), "Error al cargar datos: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show());
    }
    
    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        if (TextUtils.isEmpty(name)) {
            etName.setError("El nombre es obligatorio");
            return;
        }
        
        // Obtener ID del usuario actual
        String userId = preferenceManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Error al guardar datos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mostrar indicador de carga
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Guardando...");
        
        // Si la foto fue cambiada, subirla primero
        if (photoChanged && imageUri != null) {
            uploadProfileImage(userId, name, phone);
        } else {
            // Si no hay cambio de foto, actualizar solo los datos
            updateUserData(userId, name, phone, null);
        }
    }
    
    private void uploadProfileImage(String userId, String name, String phone) {
        String fileExtension = getFileExtension(imageUri);
        if (fileExtension == null) {
            Toast.makeText(requireContext(), "Error con el formato de la imagen", Toast.LENGTH_SHORT).show();
            btnSaveProfile.setEnabled(true);
            btnSaveProfile.setText(R.string.save_changes);
            return;
        }
        
        StorageReference fileReference = storageReference
                .child(Constants.STORAGE_PROFILE_IMAGES)
                .child(userId + "." + fileExtension);
        
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateUserData(userId, name, phone, imageUrl);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error al obtener URL: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            btnSaveProfile.setEnabled(true);
                            btnSaveProfile.setText(R.string.save_changes);
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al subir imagen: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText(R.string.save_changes);
                });
    }
    
    private void updateUserData(String userId, String name, String phone, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        
        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
        }
        
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar preferencias
                    preferenceManager.saveUserName(name);
                    preferenceManager.saveUserPhone(phone);
                    
                    if (imageUrl != null) {
                        preferenceManager.saveProfileImageUrl(imageUrl);
                    }
                    
                    Toast.makeText(requireContext(), "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    photoChanged = false;
                    
                    // Actualizar UI
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText(R.string.save_changes);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al actualizar perfil: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText(R.string.save_changes);
                });
    }
    
    private void showImagePickerDialog() {
        String[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery)};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), 
                    new String[]{Manifest.permission.CAMERA}, 
                    Constants.REQUEST_CODE_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }
    
    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    Constants.REQUEST_CODE_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            }
            
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
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
        File storageDir = requireActivity().getExternalFilesDir(null);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_CAMERA) {
                if (currentPhotoPath != null) {
                    File file = new File(currentPhotoPath);
                    imageUri = Uri.fromFile(file);
                    profileImage.setImageURI(imageUri);
                    photoChanged = true;
                }
            } else if (requestCode == Constants.REQUEST_CODE_GALLERY && data != null) {
                imageUri = data.getData();
                profileImage.setImageURI(imageUri);
                photoChanged = true;
            }
        }
    }
    
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar cuenta")
                .setMessage(getString(R.string.delete_account_warning))
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> deleteAccount())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
    
    private void deleteAccount() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Error: No se pudo obtener el usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = user.getUid();
        
        // 1. Eliminar documento del usuario en Firestore
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Eliminar imágenes del usuario en Storage
                    StorageReference profileRef = storageReference.child(Constants.STORAGE_PROFILE_IMAGES)
                            .child(userId + ".*");
                    
                    // 3. Eliminar el usuario de Authentication
                    user.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                // 4. Limpiar preferencias locales
                                preferenceManager.clearUserData();
                                
                                Toast.makeText(requireContext(), getString(R.string.account_deleted), 
                                        Toast.LENGTH_SHORT).show();
                                
                                // 5. Redirigir a la pantalla de login
                                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                requireActivity().finish();
                            })
                            .addOnFailureListener(e -> 
                                    Toast.makeText(requireContext(), getString(R.string.error_deleting_account) + 
                                            ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> 
                        Toast.makeText(requireContext(), getString(R.string.error_deleting_account) + 
                                ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
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