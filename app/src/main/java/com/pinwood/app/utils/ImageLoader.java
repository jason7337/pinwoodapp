package com.pinwood.app.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.pinwood.app.R;

/**
 * Clase utilitaria para cargar imágenes, abstrayendo la biblioteca subyacente.
 * Implementa un patrón de "fallback" para manejar casos donde Glide no está disponible.
 */
public class ImageLoader {

    // Interfaz para proporcionar una estrategia alternativa de carga de imágenes
    private interface ImageLoaderStrategy {
        void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderResId);
    }

    // Estrategia principal usando Glide
    private static class GlideLoaderStrategy implements ImageLoaderStrategy {
        @Override
        public void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderResId) {
            try {
                // Intentar usar Glide
                com.bumptech.glide.Glide.with(context)
                    .load(imageUrl)
                    .placeholder(placeholderResId)
                    .error(placeholderResId)
                    .centerCrop()
                    .into(imageView);
            } catch (Exception e) {
                // Si falla, usar la estrategia de respaldo
                new FallbackLoaderStrategy().loadImage(context, imageUrl, imageView, placeholderResId);
            }
        }
    }

    // Estrategia de respaldo básica en caso de que Glide no esté disponible
    private static class FallbackLoaderStrategy implements ImageLoaderStrategy {
        @Override
        public void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderResId) {
            try {
                // Intento básico de carga usando Uri
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Uri imageUri = Uri.parse(imageUrl);
                    imageView.setImageURI(imageUri);
                } else {
                    imageView.setImageResource(placeholderResId);
                }
            } catch (Exception e) {
                // Si todo falla, mostrar el placeholder
                imageView.setImageResource(placeholderResId);
            }
        }
    }

    // La estrategia predeterminada
    private static ImageLoaderStrategy defaultStrategy = new GlideLoaderStrategy();

    /**
     * Carga una imagen desde una URL en un ImageView.
     *
     * @param context          Contexto de la aplicación
     * @param imageUrl         URL de la imagen a cargar
     * @param imageView        ImageView donde se mostrará la imagen
     * @param placeholderResId Recurso a mostrar mientras la imagen se está cargando
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderResId) {
        try {
            defaultStrategy.loadImage(context, imageUrl, imageView, placeholderResId);
        } catch (Throwable t) {
            // Último recurso en caso de problemas graves
            imageView.setImageResource(placeholderResId);
        }
    }

    /**
     * Carga una imagen desde una URL en un ImageView con los valores predeterminados.
     *
     * @param context   Contexto de la aplicación
     * @param imageUrl  URL de la imagen a cargar
     * @param imageView ImageView donde se mostrará la imagen
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.ic_launcher_foreground);
    }

    /**
     * Configurar una estrategia personalizada para carga de imágenes (útil para pruebas)
     */
    public static void setStrategy(ImageLoaderStrategy strategy) {
        if (strategy != null) {
            defaultStrategy = strategy;
        }
    }

    /**
     * Resetear a la estrategia por defecto
     */
    public static void resetToDefaultStrategy() {
        defaultStrategy = new GlideLoaderStrategy();
    }
}