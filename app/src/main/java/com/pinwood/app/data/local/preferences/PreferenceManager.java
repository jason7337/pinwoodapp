package com.pinwood.app.data.local.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.pinwood.app.utils.Constants;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;
    
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Guardar información de usuario
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_USER_ID, userId);
        editor.apply();
    }
    
    public String getUserId() {
        return sharedPreferences.getString(Constants.PREF_USER_ID, null);
    }
    
    public void saveUserName(String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_USER_NAME, userName);
        editor.apply();
    }
    
    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, null);
    }
    
    public void saveUserEmail(String userEmail) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_USER_EMAIL, userEmail);
        editor.apply();
    }
    
    public String getUserEmail() {
        return sharedPreferences.getString(Constants.PREF_USER_EMAIL, null);
    }
    
    // Guardar URL de imagen de perfil
    public void saveProfileImageUrl(String profileImageUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_PROFILE_IMAGE_URL, profileImageUrl);
        editor.apply();
    }
    
    public String getProfileImageUrl() {
        return sharedPreferences.getString(Constants.PREF_PROFILE_IMAGE_URL, null);
    }
    
    // Guardar número de teléfono
    public void saveUserPhone(String userPhone) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_USER_PHONE, userPhone);
        editor.apply();
    }
    
    public String getUserPhone() {
        return sharedPreferences.getString(Constants.PREF_USER_PHONE, null);
    }
    
    // Limpiar información de usuario (logout)
    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_PROFILE_IMAGE_URL);
        editor.remove(Constants.PREF_USER_PHONE);
        editor.apply();
    }
    
    // Verificar si el usuario está logueado
    public boolean isUserLoggedIn() {
        return getUserId() != null;
    }
}