# Aplicación Pinwood

## Descripción
Pinwood App es una aplicación de comercio electrónico para Android especializada en muebles con capacidades de realidad aumentada (RA). Permite a los usuarios navegar por productos de mobiliario, visualizarlos en su propio espacio mediante RA y completar compras a través de un sistema integrado de pago.

## Características
- Autenticación de usuarios (inicio de sesión, registro)
- Catálogo de productos con categorías y búsqueda
- Páginas detalladas de productos con especificaciones
- Visualización de muebles en realidad aumentada (RA)
- Manipulación interactiva de modelos 3D (rotar, escalar)
- Carrito de compras y proceso de pago
- Gestión de perfil de usuario

## Tecnologías
- **Lenguaje**: Java
- **Plataforma**: Android
- **Arquitectura**: MVVM (Modelo-Vista-VistaModelo)
- **Backend**: Firebase (Autenticación, Firestore, Almacenamiento)
- **RA**: ARCore, Sceneform
- **Bibliotecas**: Glide, AndroidX, componentes Material Design

## Requisitos
- Android 7.0 (API 24) o superior
- Dispositivo compatible con Google Play Services for AR (ARCore)
- Acceso a cámara para funcionalidad de RA
- Conexión a Internet
- 100MB de espacio de almacenamiento disponible

## Instalación
1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Configurar Firebase:
   - Crear un nuevo proyecto en Firebase
   - Registrar la aplicación Android con el nombre de paquete "com.pinwood.app"
   - Descargar google-services.json al directorio de la aplicación
   - Habilitar servicios de Autenticación, Base de datos Firestore y Almacenamiento
4. Compilar y ejecutar la aplicación en un dispositivo compatible con ARCore

## Estructura del Proyecto
```
com.pinwood.app/
├── data/
│   ├── local/ - Base de datos local y preferencias
│   ├── remote/ - Integración con Firebase
│   ├── model/ - Modelos de datos
│   └── repository/ - Capa de acceso a datos
├── ui/
│   ├── common/ - Componentes UI compartidos
│   ├── home/ - Pantalla principal y navegación
│   ├── product/ - Listado y detalles de productos
│   │   └── ar/ - Funcionalidad de RA
│   ├── cart/ - Gestión del carrito de compras
│   ├── checkout/ - Finalización de pedidos
│   ├── user/ - Autenticación y perfil
│   └── settings/ - Configuración de la aplicación
├── utils/ - Clases auxiliares
└── PinwoodApplication.java - Punto de entrada de la aplicación
```

## Estado de Desarrollo
La aplicación se encuentra actualmente en desarrollo activo. Se han completado la autenticación básica, la estructura de navegación y la pantalla de inicio. La implementación de RA, el catálogo de productos y los procesos de pago están en desarrollo.

## Licencia
[Incluir información de licencia aquí]

## Contacto
[Incluir información de contacto aquí]