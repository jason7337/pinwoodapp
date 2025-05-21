# Roadmap completo para "Pinwood App": Tienda de muebles con realidad aumentada

Este roadmap te guiará paso a paso en el desarrollo de una aplicación Android nativa para venta de muebles con realidad aumentada usando Java, Firebase y ARCore.

## 1. Configuración inicial del proyecto

### Dependencias para build.gradle (nivel de proyecto)

```groovy
// Top-level build file
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.6.0'
        classpath 'com.google.gms:google-services:4.4.2'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

### Dependencias para build.gradle (nivel de app)

```groovy
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'
}

android {
    namespace 'com.pinwood.app'
    compileSdk 34
    
    defaultConfig {
        applicationId "com.pinwood.app"
        minSdk 24  // ARCore requiere mínimo API 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    buildFeatures {
        viewBinding true
    }
}

dependencies {
    // Firebase BoM para gestionar versiones compatibles
    implementation platform('com.google.firebase:firebase-bom:33.13.0')
    
    // Firebase (sin especificar versiones gracias al BoM)
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.firebase:firebase-analytics'
    
    // ARCore
    implementation 'com.google.ar:core:1.48.0'
    implementation 'io.github.sceneview:sceneform:1.0.0'
    
    // Soporte de Android
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // Biblioteca para manejar imágenes
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

### Configuración del archivo AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permisos necesarios -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Permisos de almacenamiento -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

    <!-- Necesario para AR -->
    <uses-feature android:name="android.hardware.camera.ar" android:required="true" />
    <uses-feature android:glEsVersion="0x00020000" android:required="true" />

    <application
        android:name=".PinwoodApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.PinwoodApp"
        tools:targetApi="34">

        <!-- Configuración para ARCore -->
        <meta-data
            android:name="com.google.ar.core"
            android:value="required" />

        <!-- Actividades -->
        <activity
            android:name=".ui.splash.SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.PinwoodApp.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".ui.home.HomeActivity"
            android:exported="false" />

        <activity
            android:name=".ui.product.ar.ARActivity"
            android:exported="false"
            android:screenOrientation="locked"
            android:configChanges="orientation|screenSize" 
            android:theme="@style/Theme.PinwoodApp.NoActionBar" />

        <activity
            android:name=".ui.product.detail.ProductDetailActivity"
            android:exported="false" />

        <activity
            android:name=".ui.user.login.LoginActivity"
            android:exported="false"
            android:windowSoftInputMode="adjustResize" />

        <activity
            android:name=".ui.user.register.RegisterActivity"
            android:exported="false"
            android:windowSoftInputMode="adjustResize" />

        <activity
            android:name=".ui.cart.CartActivity"
            android:exported="false" />

        <activity
            android:name=".ui.checkout.CheckoutActivity"
            android:exported="false" />

    </application>

</manifest>
```

### Estructura de directorios recomendada

```
com.pinwood.app/
├── data/
│   ├── local/
│   │   ├── database/        # Implementación Room para caché
│   │   └── preferences/     # Gestión de preferencias
│   ├── remote/
│   │   └── firebase/        # Implementaciones de Firebase
│   │       ├── auth/        # Autenticación
│   │       ├── firestore/   # Operaciones con Firestore
│   │       └── storage/     # Operaciones con Storage
│   ├── model/
│   │   ├── product/         # Modelos de productos
│   │   ├── user/            # Modelos de usuarios
│   │   └── order/           # Modelos de pedidos
│   └── repository/          # Repositorios
├── ui/
│   ├── common/              # Componentes reutilizables
│   ├── home/                # Pantalla principal
│   ├── product/             # Pantallas de productos
│   │   ├── list/            # Lista de productos
│   │   ├── detail/          # Detalle de producto
│   │   └── ar/              # Funcionalidad AR
│   ├── cart/                # Carrito de compra
│   ├── checkout/            # Finalizar compra
│   ├── user/                # Gestión de usuario
│   │   ├── login/           # Inicio de sesión
│   │   ├── register/        # Registro
│   │   └── profile/         # Perfil
│   └── settings/            # Configuración
├── utils/                   # Utilidades
└── PinwoodApplication.java  # Clase de aplicación
```

## 2. Arquitectura de la aplicación

### Patrón arquitectónico: MVVM (Model-View-ViewModel)

Este patrón es ideal para aplicaciones como Pinwood App porque:
- Separa la UI (View) de la lógica de negocio (ViewModel) y datos (Model)
- Facilita las pruebas unitarias
- Los ViewModels sobreviven a cambios de configuración
- Compatible con LiveData y Firebase

### Diseño de Activities y Fragments

#### Activities principales:
1. **SplashActivity**: Pantalla inicial de carga
2. **HomeActivity**: Actividad principal con navegación
3. **LoginActivity**: Gestión de autenticación
4. **RegisterActivity**: Registro de usuarios
5. **ProductDetailActivity**: Detalles del producto
6. **ARActivity**: Experiencia de realidad aumentada
7. **CartActivity**: Gestión del carrito
8. **CheckoutActivity**: Proceso de pago

#### Fragments principales:
1. **HomeFragment**: Pantalla principal con categorías y destacados
2. **CategoryFragment**: Lista de categorías
3. **ProductListFragment**: Lista de productos por categoría
4. **ProductDetailFragment**: Detalle completo del producto
5. **ARFragment**: Visualización AR dentro de ARActivity
6. **CartFragment**: Contenido del carrito
7. **CheckoutFragment**: Formulario de checkout
8. **ProfileFragment**: Información de usuario

### Modelos de datos para Firebase Firestore

#### Usuario (User)
```java
public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private Address address;
    private String profileImageUrl;
    private Date createdAt;
    
    // Constructor, getters y setters
}

public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    // Constructor, getters y setters
}
```

#### Producto (Product)
```java
public class Product {
    private String productId;
    private String name;
    private String description;
    private double price;
    private String category;
    private Dimensions dimensions;
    private List<String> imageUrls;
    private List<ArModel> arModels;
    private int availableStock;
    private List<String> tags;
    
    // Constructor, getters y setters
}

public class Dimensions {
    private double width;
    private double height;
    private double depth;
    private String unit; // "cm", "in", etc.
    
    // Constructor, getters y setters
}

public class ArModel {
    private String url;
    private String format; // "glb", "gltf", etc.
    
    // Constructor, getters y setters
}
```

#### Carrito (CartItem)
```java
public class CartItem {
    private String productId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;
    
    // Constructor, getters y setters
}
```

#### Pedido (Order)
```java
public class Order {
    private String orderId;
    private String userId;
    private String status;
    private Date timestamp;
    private double subtotal;
    private double shippingCost;
    private double tax;
    private double total;
    private List<OrderItem> items;
    private ShippingInfo shipping;
    private PaymentInfo payment;
    
    // Constructor, getters y setters
}
```

### Estructura de la base de datos en Firestore

```
firestore/
│
├── users/
│   └── {userId}/
│       ├── profile: { name, email, phone, address, ... }
│       ├── cart/
│       │   └── {productId}: { productId, name, price, quantity, ... }
│       └── favorites/
│           └── {productId}: { productId, timestamp }
│
├── products/
│   └── {productId}/
│       ├── details: { name, description, price, category, dimensions, ... }
│       ├── images: [ {url, alt}, ... ]
│       ├── ar_models: [ {url, format}, ... ]
│       ├── stock: { available, reserved }
│       └── reviews/
│           └── {reviewId}: { userId, userName, rating, comment, timestamp }
│
├── categories/
│   └── {categoryId}/
│       ├── name: "Sala", "Comedor", "Dormitorio", ...
│       └── products: [ productId1, productId2, ... ]
│
└── orders/
    └── {orderId}/
        ├── userId: "user123"
        ├── status: "pending", "processing", "shipped", "delivered", ...
        ├── timestamp: timestamp
        ├── total: 1299.99
        ├── items: [ { productId, name, price, quantity, ... } ]
        ├── shipping: { address, method, cost, ... }
        └── payment: { method, status, ... }
```

## 3. Implementación de realidad aumentada

### Configuración específica de ARCore

Para integrar ARCore en la aplicación, necesitamos configurar el módulo build.gradle:

```groovy
dependencies {
    // ARCore
    implementation 'com.google.ar:core:1.48.0'
    implementation 'io.github.sceneview:sceneform:1.0.0'
}
```

### Verificación de compatibilidad ARCore

```java
public class ARActivity extends AppCompatActivity {
    private boolean mUserRequestedInstall = true;
    private Session mSession;
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Verificación de compatibilidad
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        // ARCore está instalado y es compatible
                        mSession = new Session(this);
                        break;
                    case INSTALL_REQUESTED:
                        // ARCore necesita instalación
                        mUserRequestedInstall = false;
                        return;
                }
            }
        } catch (UnavailableDeviceNotCompatibleException e) {
            // Dispositivo no compatible
            Toast.makeText(this, "Este dispositivo no es compatible con ARCore", Toast.LENGTH_LONG).show();
            finish();
            return;
        } catch (Exception e) {
            // Otros errores
            return;
        }
        
        // Continuar configuración AR...
    }
}
```

### Carga y renderizado de modelos 3D

```java
public class ARFurnitureActivity extends AppCompatActivity {
    private ArFragment arFragment;
    private ModelRenderable furnitureRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_furniture);
        
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        
        // Configurar listener para taps en superficies
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (furnitureRenderable == null) {
                return;
            }
            
            // Crear ancla y nodo
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            
            // Crear nodo transformable para manipular el mueble
            TransformableNode furniture = new TransformableNode(arFragment.getTransformationSystem());
            furniture.setParent(anchorNode);
            furniture.setRenderable(furnitureRenderable);
            furniture.select();
        });
        
        // Cargar modelo 3D
        String modelUrl = getIntent().getStringExtra("MODEL_URL");
        loadFurnitureModel(modelUrl);
    }
    
    private void loadFurnitureModel(String modelUrl) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelUrl))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept(renderable -> {
                furnitureRenderable = renderable;
            })
            .exceptionally(throwable -> {
                Toast.makeText(this, "No se pudo cargar el modelo 3D", Toast.LENGTH_SHORT).show();
                return null;
            });
    }
}
```

### Integración de AR con el catálogo de productos

```java
// En ProductDetailActivity o Fragment
private void launchARExperience() {
    // Verificar compatibilidad con ARCore
    ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
    if (availability.isSupported()) {
        Intent intent = new Intent(this, ARActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId());
        intent.putExtra("MODEL_URL", product.getArModels().get(0).getUrl());
        startActivity(intent);
    } else {
        Toast.makeText(this, "ARCore no es compatible con este dispositivo", Toast.LENGTH_LONG).show();
    }
}
```

### Funcionalidades para cambio de colores y variantes

```java
public class ColorManager {
    private TransformableNode furnitureNode;
    private ModelRenderable baseRenderable;
    
    public ColorManager(TransformableNode node, ModelRenderable renderable) {
        this.furnitureNode = node;
        this.baseRenderable = renderable;
    }
    
    public void changeColor(Color newColor) {
        if (baseRenderable == null) return;
        
        MaterialFactory.makeOpaqueWithColor(furnitureNode.getContext(), newColor)
            .thenAccept(material -> {
                // Crear copia del renderable para no afectar otros muebles
                ModelRenderable coloredRenderable = baseRenderable.makeCopy();
                coloredRenderable.setMaterial(material);
                furnitureNode.setRenderable(coloredRenderable);
            });
    }
    
    // Cambiar textura (tipo de madera, tela, etc.)
    public void changeTexture(Context context, Uri textureUri) {
        if (baseRenderable == null) return;
        
        Texture.builder()
            .setSource(context, textureUri)
            .build()
            .thenAccept(texture -> {
                ModelRenderable texturedRenderable = baseRenderable.makeCopy();
                Material material = texturedRenderable.getMaterial();
                material.setTexture("baseColor", texture);
                furnitureNode.setRenderable(texturedRenderable);
            });
    }
}
```

### Captura de pantalla de la experiencia AR

```java
public void captureARScene() {
    ArSceneView view = arFragment.getArSceneView();
    
    // Crear bitmap del tamaño de la vista
    final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), 
            Bitmap.Config.ARGB_8888);
            
    // Handler para la captura
    final HandlerThread handlerThread = new HandlerThread("PixelCopier");
    handlerThread.start();
    
    // Copiar vista al bitmap
    PixelCopy.request(view, bitmap, (copyResult) -> {
        if (copyResult == PixelCopy.SUCCESS) {
            try {
                // Guardar imagen en galería
                String fileName = "Pinwood_" + System.currentTimeMillis() + ".jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, 
                        Environment.DIRECTORY_PICTURES + "/Pinwood");
                
                Uri imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        
                if (imageUri != null) {
                    OutputStream out = getContentResolver().openOutputStream(imageUri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    if (out != null) {
                        out.close();
                    }
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Imagen guardada en galería", 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, 
                        "Error guardando imagen: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, 
                    "Error capturando pantalla", Toast.LENGTH_SHORT).show());
        }
        handlerThread.quitSafely();
    }, new Handler(handlerThread.getLooper()));
}
```

## 4. Assets y recursos necesarios

### Fuentes para modelos 3D de muebles

#### Recursos gratuitos:
- **Sketchfab** (biblioteca libre): https://sketchfab.com/feed
- **Google Poly** (archivo): https://poly.pizza/
- **TurboSquid** (sección gratuita): https://www.turbosquid.com/Search/3D-Models/free
- **Free3D** (sección de muebles): https://free3d.com/3d-models/furniture

#### Recursos de pago:
- **CGTrader**: https://www.cgtrader.com/3d-models/furniture
- **TurboSquid Premium**: https://www.turbosquid.com/3d-models/furniture 
- **Hum3D** (especializado en muebles): https://hum3d.com/3d-models/furniture/

### Formatos y especificaciones recomendadas

- **Formatos preferidos**:
  - **glTF/glb**: Formato más recomendado actualmente
  - **OBJ+MTL**: Buena alternativa

- **Especificaciones técnicas**:
  - **Polígonos**: Entre 10,000-50,000 para equilibrar calidad y rendimiento
  - **Texturas**: Resolución de 1024x1024 a 2048x2048 px (JPG o PNG)
  - **Tamaño del archivo**: Menor a 10MB para carga rápida
  - **Escala**: Utilizar escala real en metros
  - **Origen**: Centrado en la base del objeto

### Herramientas para optimizar modelos 3D

- **Blender** (gratuito): Para editar, optimizar y exportar modelos
- **Instant Meshes**: Para reducir polígonos manteniendo la forma
- **glTF Tools**: Convertidores y optimizadores de formato
- **Online 3D Converter**: Para conversiones rápidas entre formatos

### Recursos de UI

- **Material Design Icons**: Iconos consistentes para la interfaz
- **Iconos e imágenes**: Disponibles en sitios como Freepik, Flaticon
- **Unsplash**: Para imágenes de ejemplo de productos
- **Adobe XD/Figma**: Para diseño de interfaces

## 5. Guía de prompts para Claude Code

### Sprint 1: Configuración Inicial

#### Prompt 1: Crear estructura inicial del proyecto
```
Por favor, crea la estructura básica del proyecto Android "Pinwood App" con la siguiente especificación:

- La clase Application principal (PinwoodApplication.java)
- El archivo build.gradle a nivel de app con las dependencias necesarias para Firebase y ARCore
- Un Activity inicial vacío (MainActivity.java) con su layout correspondiente
- La clase utilitaria (Constants.java) para almacenar constantes del proyecto

La aplicación es una tienda de muebles con realidad aumentada que usa Firebase como backend.
```

#### Prompt 2: Configurar AndroidManifest.xml
```
Genera el archivo AndroidManifest.xml completo para la aplicación "Pinwood App" con los siguientes requerimientos:

- Permisos para internet, cámara y almacenamiento
- Configuración para ARCore (requiere camera.ar)
- Definición de las actividades principales (MainActivity, LoginActivity, RegisterActivity, HomeActivity, ProductDetailActivity, ARActivity, CartActivity, CheckoutActivity)
- La actividad principal debe ser SplashActivity con su intent-filter correspondiente

La aplicación es una tienda de muebles con AR que usa Java.
```

#### Prompt 3: Implementar PinwoodApplication
```
Crea la clase PinwoodApplication que extiende de Application para inicializar Firebase y ARCore al inicio de la aplicación. Debe incluir:

- Inicialización de Firebase
- Configuración de persistencia de Firestore
- Verificación de disponibilidad de ARCore
- Inicialización de la caché de imágenes con Glide
```

### Sprint 2: Autenticación

#### Prompt 4: Implementar LoginActivity
```
Crea la LoginActivity completa con las siguientes características:

- Layout con campos para email y password, botón de login y opción de registro
- Validación de campos
- Integración con Firebase Authentication
- Manejo de errores de autenticación
- Navegación a HomeActivity cuando el login es exitoso
- Opción "Olvidé mi contraseña"

Todo con Java, sin usar Kotlin.
```

#### Prompt 5: Implementar RegisterActivity
```
Crea la RegisterActivity completa con las siguientes características:

- Layout con campos para nombre, email, password y confirmación de password
- Validación de todos los campos (email válido, coincidencia de passwords, etc.)
- Integración con Firebase Authentication para crear nuevos usuarios
- Creación del documento del usuario en Firestore después del registro exitoso
- Manejo de errores y respuestas visuales al usuario
```

#### Prompt 6: Implementar clases de modelo User y Address
```
Crea las clases de modelo User y Address para la aplicación de venta de muebles. Incluye:

- Todos los campos necesarios (userId, name, email, phone, etc. para User)
- Campos de dirección (street, city, state, zipCode, country) para Address
- Constructor, getters y setters
- Métodos toMap() y fromMap() para conversión a/desde Firestore

Usa Java, no Kotlin.
```

### Sprint 3: Home y Catálogo

#### Prompt 7: Implementar HomeActivity y HomeFragment
```
Crea la HomeActivity y HomeFragment principales para mostrar el catálogo de muebles:

- BottomNavigationView con opciones para Home, Categories, Cart y Profile
- RecyclerView para mostrar productos destacados en un grid
- Carousel para categorías destacadas
- Banner promocional en la parte superior
- Integración con Firestore para cargar datos reales
- ViewModels para manejar los datos

La estructura debe seguir el patrón MVVM.
```

#### Prompt 8: Implementar ProductRepository
```
Crea la clase ProductRepository completa que gestiona todos los productos de la aplicación:

- Métodos para obtener productos por categoría
- Método para obtener un producto específico por ID
- Método para obtener productos destacados
- Métodos para gestionar el caché local
- Integración con Firestore para datos remotos
- Manejo de excepciones y errores

Usa el patrón Repository y LiveData para notificar cambios.
```

#### Prompt 9: Implementar ProductListFragment y Adapter
```
Crea el ProductListFragment y su adaptador (ProductAdapter) para mostrar la lista de productos por categoría:

- RecyclerView con GridLayoutManager (2 columnas)
- CardView para cada elemento con imagen, nombre, precio y botón de añadir al carrito
- Filtrado y ordenación de productos
- Manejo de clics para navegar al detalle
- Carga de imágenes con Glide
- Estados de carga, error y vacío

Sigue el patrón MVVM e implementa ViewBinding.
```

### Sprint 4: Detalle de Producto y AR

#### Prompt 10: Implementar ProductDetailActivity
```
Crea la ProductDetailActivity completa que muestra el detalle de un producto con las siguientes características:

- ViewPager para galería de imágenes del producto
- Nombre, descripción, precio y especificaciones del mueble
- Botón "Ver en AR" que lanza la experiencia de realidad aumentada
- Botón para añadir al carrito
- Integración con Firestore para cargar datos del producto
- Diseño Material Design con transiciones adecuadas

Usa el patrón MVVM e implementa ViewBinding.
```

#### Prompt 11: Implementar ARActivity básica
```
Crea la ARActivity básica para visualizar muebles en realidad aumentada con ARCore:

- Configuración inicial de ARCore y verificación de compatibilidad
- Implementación de ArFragment para manejo de la cámara y detección de superficies
- Código para cargar el modelo 3D del mueble (formato glb)
- Colocación del mueble al tocar una superficie detectada
- Gestos para escalar y rotar el mueble
- Botones de UI para cambiar color/textura y tomar captura de pantalla

Usa Java y las dependencias de ARCore/Sceneform.
```

#### Prompt 12: Implementar carga y renderización de modelos 3D
```
Crea las clases necesarias para cargar y renderizar modelos 3D de muebles en la experiencia AR:

- ModelLoader para cargar modelos desde Firebase Storage o assets
- Renderer que maneja el ciclo de vida del renderizado
- TransformationManager para manejar gestos de escala, rotación y traslación
- MaterialManager para cambiar colores y texturas
- CaptureManager para tomar capturas de pantalla de la experiencia AR

Todo implementado con Java usando ARCore.
```

### Sprint 5: Carrito y Checkout

#### Prompt 13: Implementar CartFragment y CartManager
```
Crea el CartFragment y CartManager completos para manejar el carrito de compras:

- RecyclerView con lista de productos en el carrito
- Funcionalidad para aumentar/disminuir cantidades
- Botón para eliminar productos del carrito
- Cálculo del subtotal, impuestos y total
- Persistencia del carrito en Firestore
- Botón para proceder al checkout

Sigue el patrón MVVM y usa ViewBinding.
```

#### Prompt 14: Implementar CheckoutActivity
```
Crea la CheckoutActivity completa para finalizar la compra:

- Formulario de datos de envío con validación
- Resumen del pedido con lista de productos
- Opciones de método de pago (simuladas)
- Botón para confirmar pedido que guarda la orden en Firestore
- Pantalla de confirmación de pedido
- Manejo de errores durante el proceso

Sigue el patrón MVVM e integra con Firebase.
```

### Sprint 6: Mejoras y Optimización

#### Prompt 15: Implementar medición de dimensiones en AR
```
Crea la funcionalidad para medir dimensiones en el espacio real usando ARCore:

- Clase MeasurementManager que permite medir distancias
- UI para marcar puntos de inicio y fin
- Visualización de la medida en metros/centímetros
- Representación visual de la línea de medición
- Botón para guardar medidas y asociarlas a un producto
```

#### Prompt 16: Optimizar carga de modelos 3D
```
Implementa un sistema optimizado para la carga y caché de modelos 3D:

- ModelManager para gestionar la descarga y almacenamiento local de modelos
- Caché para evitar descargar modelos ya usados
- Sistema de precarga de modelos en segundo plano
- Compresión y optimización de modelos
- Monitoreo de rendimiento y liberación de memoria

Optimiza el código existente para mejorar el rendimiento de AR.
```

## 6. Configuración de Firebase

### Paso a paso para configurar Firebase Console

1. **Crear proyecto en Firebase**:
   - Visita https://console.firebase.google.com
   - Haz clic en "Agregar proyecto"
   - Nombra el proyecto "Pinwood App"
   - Habilita Google Analytics
   - Haz clic en "Crear proyecto"

2. **Registrar la aplicación Android**:
   - En la consola de Firebase, haz clic en el ícono de Android
   - Ingresa el paquete: `com.pinwood.app`
   - Opcional: añade un apodo
   - Registra el SHA-1 de tu certificado de firma (desarrollo)
   ```bash
   cd android && ./gradlew signingReport
   ```
   - Descarga el archivo `google-services.json` y colócalo en el directorio `app/`

3. **Activar servicios necesarios**:

   a. **Authentication**:
   - En el menú lateral, selecciona "Authentication"
   - Haz clic en "Comenzar"
   - Habilita el proveedor "Correo electrónico/contraseña"
   - Guarda los cambios

   b. **Firestore Database**:
   - En el menú lateral, selecciona "Firestore Database"
   - Haz clic en "Crear base de datos"
   - Comienza en modo de prueba
   - Selecciona la ubicación más cercana

   c. **Storage**:
   - En el menú lateral, selecciona "Storage"
   - Haz clic en "Comenzar"
   - Comienza en modo de prueba
   - Selecciona la ubicación más cercana

### Reglas de seguridad para Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Función para comprobar autenticación
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Función para comprobar que es el propio usuario
    function isUser(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Función para comprobar administrador
    function isAdmin() {
      return isAuthenticated() && exists(/databases/$(database)/documents/admins/$(request.auth.uid));
    }

    // Reglas para usuarios
    match /users/{userId} {
      allow read, write: if isUser(userId) || isAdmin();
      
      // Reglas para carrito
      match /cart/{itemId} {
        allow read, write: if isUser(userId);
      }
      
      // Reglas para favoritos
      match /favorites/{productId} {
        allow read, write: if isUser(userId);
      }
    }
    
    // Reglas para productos
    match /products/{productId} {
      // Cualquiera puede leer productos
      allow read: if true;
      // Solo admins pueden modificar
      allow write: if isAdmin();
      
      // Reglas para reseñas
      match /reviews/{reviewId} {
        allow read: if true;
        allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
        allow update, delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
      }
    }
    
    // Reglas para categorías
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if isAdmin();
    }
    
    // Reglas para pedidos
    match /orders/{orderId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update: if isAdmin();
      allow delete: if false;
    }
  }
}
```

### Reglas de seguridad para Storage

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Función para comprobar autenticación
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Función para comprobar que es el propio usuario
    function isUser(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Función para comprobar administrador
    function isAdmin() {
      return isAuthenticated() && 
        firestore.exists(/databases/(default)/documents/admins/$(request.auth.uid));
    }
    
    // Reglas para archivos de productos
    match /products/{productId}/{allPaths=**} {
      // Cualquiera puede leer archivos de productos
      allow read: if true;
      // Solo admins pueden modificar
      allow write: if isAdmin();
    }
    
    // Reglas para archivos de usuarios
    match /users/{userId}/{allPaths=**} {
      allow read: if isUser(userId) || isAdmin();
      allow write: if isUser(userId);
    }
    
    // Reglas para archivos de categorías
    match /categories/{categoryId}/{allPaths=**} {
      allow read: if true;
      allow write: if isAdmin();
    }
  }
}
```

### Estructura recomendada para Storage

```
storage/
│
├── products/
│   └── {productId}/
│       ├── images/
│       │   ├── main.jpg
│       │   ├── thumbnail.jpg
│       │   ├── detail_1.jpg
│       │   └── detail_2.jpg
│       └── ar_models/
│           ├── model.glb
│           └── model.usdz (para iOS)
│
├── users/
│   └── {userId}/
│       └── profile_picture.jpg
│
└── categories/
    └── {categoryId}/
        ├── icon.png
        └── banner.jpg
```

## 7. Mejores prácticas y consideraciones

### Manejo de errores y casos edge

1. **Verificación de conectividad**:
   ```java
   public boolean isNetworkAvailable() {
       ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
       return activeNetworkInfo != null && activeNetworkInfo.isConnected();
   }
   ```

2. **Manejo de errores Firebase**:
   ```java
   private void handleFirebaseAuthError(Exception exception) {
       if (exception instanceof FirebaseAuthInvalidCredentialsException) {
           showError("Credenciales inválidas. Revisa tu email y contraseña.");
       } else if (exception instanceof FirebaseAuthInvalidUserException) {
           showError("Usuario no encontrado. Regístrate primero.");
       } else if (exception instanceof FirebaseNetworkException) {
           showError("Error de red. Verifica tu conexión a Internet.");
       } else {
           showError("Error de autenticación: " + exception.getMessage());
       }
   }
   ```

3. **Manejo de permisos de cámara**:
   ```java
   private void requestCameraPermission() {
       if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
       } else {
           startARSession();
       }
   }
   
   @Override
   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
       if (requestCode == CAMERA_PERMISSION_REQUEST) {
           if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
               startARSession();
           } else {
               Toast.makeText(this, "La cámara es necesaria para AR", Toast.LENGTH_LONG).show();
               finish();
           }
       }
   }
   ```

4. **Verificación de dispositivo compatible con AR**:
   ```java
   private boolean checkARSupport() {
       ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
       if (availability.isSupported()) {
           return true;
       } else {
           Toast.makeText(this, "Este dispositivo no es compatible con AR", Toast.LENGTH_LONG).show();
           return false;
       }
   }
   ```

### Optimización de performance para AR

1. **Reducción de polígonos en modelos 3D**:
   - Mantener los modelos entre 10,000-50,000 polígonos
   - Utilizar LOD (Level of Detail) para mostrar modelos más simples a distancia

2. **Optimización de texturas**:
   - Comprimir texturas y reducir resolución (máximo 2048x2048)
   - Utilizar formatos ASTC o KTX para texturas en Android

3. **Manejo de carga asíncrona**:
   ```java
   private void loadModelsInBackground() {
       ExecutorService executor = Executors.newSingleThreadExecutor();
       Handler handler = new Handler(Looper.getMainLooper());
       
       executor.execute(() -> {
           // Cargar modelos en background
           ModelRenderable renderable = loadModel();
           
           // Actualizar UI en hilo principal
           handler.post(() -> {
               modelRenderable = renderable;
               updateLoadingUI(false);
           });
       });
   }
   ```

4. **Gestión de recursos**:
   ```java
   @Override
   protected void onDestroy() {
       super.onDestroy();
       
       // Liberar recursos de AR
       if (arSession != null) {
           arSession.close();
           arSession = null;
       }
       
       // Liberar renderables
       if (modelRenderable != null) {
           modelRenderable.destroy();
           modelRenderable = null;
       }
   }
   ```

### UX/UI recommendations para apps de e-commerce con AR

1. **Instrucciones claras para AR**:
   - Mostrar guía visual para escanear superficies
   - Explicar gestos para manipular muebles (pinch, rotate)
   - Proporcionar feedback visual durante el escaneo

2. **Transiciones fluidas**:
   - Implementar transiciones suaves entre catálogo y experiencia AR
   - Mostrar loading animado mientras se cargan modelos
   - Mantener coherencia visual entre catálogo y AR

3. **Compartir experiencia AR**:
   - Permitir capturas de pantalla y compartir en redes sociales
   - Incluir branding en capturas compartidas
   - Opción para volver al producto después de compartir

4. **Facilitar comparaciones**:
   - Permitir visualizar varios muebles simultáneamente
   - Incluir herramienta de medición para verificar dimensiones
   - Mostrar especificaciones junto al modelo AR

### Testing considerations

1. **Pruebas en múltiples dispositivos**:
   - Probar en dispositivos de gama alta, media y baja
   - Verificar compatibilidad con versiones de ARCore
   - Comprobar rendimiento en dispositivos antiguos

2. **Pruebas de usabilidad**:
   - Probar con usuarios reales de diferentes edades
   - Evaluar la facilidad para encontrar y usar funciones AR
   - Recopilar feedback sobre posicionamiento y manipulación

3. **Pruebas de rendimiento**:
   - Monitorear uso de memoria y CPU durante experiencia AR
   - Verificar consumo de batería en sesiones prolongadas
   - Comprobar tiempos de carga de modelos 3D

4. **Pruebas de conectividad**:
   - Probar en condiciones de conexión limitada
   - Verificar comportamiento offline
   - Comprobar carga de modelos en diferentes velocidades de conexión

---

Este roadmap completo te proporciona todo lo necesario para desarrollar "Pinwood App" con Java, Firebase y ARCore. Sigue la secuencia de desarrollo recomendada y utiliza los prompts de Claude Code para implementar cada componente de manera eficiente.