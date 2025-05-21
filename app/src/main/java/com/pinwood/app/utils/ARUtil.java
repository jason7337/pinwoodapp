package com.pinwood.app.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Clase utilitaria para abstraer las dependencias de ARCore.
 */
public class ARUtil {
    private static final String TAG = "ARUtil";

    /**
     * Verifica la disponibilidad de ARCore en el dispositivo.
     *
     * @param context Contexto de la aplicación
     * @return true si ARCore está disponible, false en caso contrario
     */
    public static boolean checkARCoreAvailability(Context context) {
        try {
            // Cargar las clases de ARCore usando reflexión
            Class<?> arCoreApkClass = Class.forName("com.google.ar.core.ArCoreApk");
            Object arCoreApkInstance = arCoreApkClass.getMethod("getInstance").invoke(null);
            
            // Obtener la disponibilidad
            Class<?> availabilityClass = Class.forName("com.google.ar.core.ArCoreApk$Availability");
            Object availability = arCoreApkClass.getMethod("checkAvailability", Context.class)
                .invoke(arCoreApkInstance, context);
            
            // Verificar si es transiente
            Boolean isTransient = (Boolean) availabilityClass.getMethod("isTransient").invoke(availability);
            if (isTransient) {
                Log.d(TAG, "La disponibilidad de AR es transitoria");
            }
            
            // Verificar si es compatible
            Boolean isSupported = (Boolean) availabilityClass.getMethod("isSupported").invoke(availability);
            if (!isSupported) {
                Toast.makeText(context, "Este dispositivo no es compatible con AR", Toast.LENGTH_LONG).show();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar ARCore: " + e.getMessage());
            return false;
        }
    }
}