package com.pinwood.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.pinwood.app.utils.ARUtil;
import com.pinwood.app.utils.FirebaseUtil;
import com.pinwood.app.utils.ImageLoader;

public class PinwoodApplication extends Application {
    private static final String TAG = "PinwoodApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inicializar Firebase
        FirebaseUtil.initializeFirebase(this);
        
        // Verificar disponibilidad de ARCore
        ARUtil.checkARCoreAvailability(this);
        
        // Configurar Glide para caché de imágenes
        configureGlide();
    }
    
    private void configureGlide() {
        try {
            // Intenta configurar Glide directamente - usando nombres completos para evitar problemas de importación
            Class<?> glideClass = Class.forName("com.bumptech.glide.Glide");
            Class<?> requestOptionsClass = Class.forName("com.bumptech.glide.request.RequestOptions");
            Class<?> diskCacheStrategyClass = Class.forName("com.bumptech.glide.load.engine.DiskCacheStrategy");
            Class<?> glideBuilderClass = Class.forName("com.bumptech.glide.GlideBuilder");
            
            // Aquí usamos reflexión para crear y configurar Glide, lo que lo hace más resistente a errores
            Object diskCacheStrategy = diskCacheStrategyClass.getField("ALL").get(null);
            Object requestOptions = requestOptionsClass.newInstance();
            
            // Establece las opciones
            requestOptionsClass.getMethod("diskCacheStrategy", diskCacheStrategyClass)
                .invoke(requestOptions, diskCacheStrategy);
            requestOptionsClass.getMethod("skipMemoryCache", boolean.class)
                .invoke(requestOptions, false);
                
            // Crea el builder
            Object builder = glideBuilderClass.newInstance();
            glideBuilderClass.getMethod("setDefaultRequestOptions", requestOptionsClass)
                .invoke(builder, requestOptions);
                
            // Inicializa Glide
            glideClass.getMethod("init", Application.class, glideBuilderClass)
                .invoke(null, this, builder);
                
        } catch (Exception e) {
            Log.e(TAG, "No se pudo configurar Glide: " + e.getMessage());
            // No es crítico si falla, la aplicación puede funcionar sin Glide
        }
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        
        try {
            // Limpiar caché de Glide para liberar memoria cuando sea necesario
            Class<?> glideClass = Class.forName("com.bumptech.glide.Glide");
            Object glideInstance = glideClass.getMethod("get", Context.class).invoke(null, this);
            glideInstance.getClass().getMethod("clearMemory").invoke(glideInstance);
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar memoria de Glide: " + e.getMessage());
        }
    }
}