package com.pinwood.app.utils;

import android.content.Context;
import android.util.Log;

/**
 * Clase utilitaria para abstraer las dependencias de Firebase.
 * Utiliza reflexión para acceder a las clases de Firebase sin necesitar importaciones directas.
 */
public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";

    /**
     * Inicializa Firebase y configura Firestore para persistencia offline.
     *
     * @param context Contexto de la aplicación
     * @return true si la inicialización fue exitosa, false en caso contrario
     */
    public static boolean initializeFirebase(Context context) {
        try {
            // Inicializar Firebase
            Class<?> firebaseAppClass = Class.forName("com.google.firebase.FirebaseApp");
            firebaseAppClass.getMethod("initializeApp", Context.class).invoke(null, context);
            
            // Configurar Firestore
            Class<?> firestoreClass = Class.forName("com.google.firebase.firestore.FirebaseFirestore");
            Object firestoreInstance = firestoreClass.getMethod("getInstance").invoke(null);
            
            Class<?> settingsClass = Class.forName("com.google.firebase.firestore.FirebaseFirestoreSettings$Builder");
            Object settingsBuilder = settingsClass.newInstance();
            
            // Configurar persistencia offline
            settingsBuilder = settingsClass.getMethod("setPersistenceEnabled", boolean.class)
                .invoke(settingsBuilder, true);
            
            // Buscar el campo CACHE_SIZE_UNLIMITED usando reflexión
            Class<?> firestoreSettingsClass = Class.forName("com.google.firebase.firestore.FirebaseFirestoreSettings");
            Object cacheSizeUnlimited = firestoreSettingsClass.getField("CACHE_SIZE_UNLIMITED").get(null);
            
            settingsBuilder = settingsClass.getMethod("setCacheSizeBytes", long.class)
                .invoke(settingsBuilder, cacheSizeUnlimited);
            
            Object settings = settingsClass.getMethod("build").invoke(settingsBuilder);
            
            firestoreClass.getMethod("setFirestoreSettings", firestoreSettingsClass)
                .invoke(firestoreInstance, settings);
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firebase: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la instancia de FirebaseAuth.
     *
     * @return Objeto FirebaseAuth o null si hay un error
     */
    public static Object getFirebaseAuth() {
        try {
            Class<?> firebaseAuthClass = Class.forName("com.google.firebase.auth.FirebaseAuth");
            return firebaseAuthClass.getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener FirebaseAuth: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el usuario actual.
     *
     * @return Objeto FirebaseUser o null si no hay usuario autenticado o hay un error
     */
    public static Object getCurrentUser() {
        try {
            Object firebaseAuth = getFirebaseAuth();
            if (firebaseAuth != null) {
                return firebaseAuth.getClass().getMethod("getCurrentUser").invoke(firebaseAuth);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuario actual: " + e.getMessage());
        }
        return null;
    }

    /**
     * Verifica si hay un usuario autenticado.
     *
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public static boolean isUserAuthenticated() {
        return getCurrentUser() != null;
    }
}