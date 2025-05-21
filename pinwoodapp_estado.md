# Guía de prompts para Claude Code - Estado de implementación

## Sprint 1: Configuración Inicial

#### ✅ Prompt 1: Crear estructura inicial del proyecto - IMPLEMENTADO
```
Por favor, crea la estructura básica del proyecto Android "Pinwood App" con la siguiente especificación:

- La clase Application principal (PinwoodApplication.java)
- El archivo build.gradle a nivel de app con las dependencias necesarias para Firebase y ARCore
- Un Activity inicial vacío (MainActivity.java) con su layout correspondiente
- La clase utilitaria (Constants.java) para almacenar constantes del proyecto

La aplicación es una tienda de muebles con realidad aumentada que usa Firebase como backend.
```

#### ✅ Prompt 2: Configurar AndroidManifest.xml - IMPLEMENTADO
```
Genera el archivo AndroidManifest.xml completo para la aplicación "Pinwood App" con los siguientes requerimientos:

- Permisos para internet, cámara y almacenamiento
- Configuración para ARCore (requiere camera.ar)
- Definición de las actividades principales (MainActivity, LoginActivity, RegisterActivity, HomeActivity, ProductDetailActivity, ARActivity, CartActivity, CheckoutActivity)
- La actividad principal debe ser SplashActivity con su intent-filter correspondiente

La aplicación es una tienda de muebles con AR que usa Java.
```

#### ✅ Prompt 3: Implementar PinwoodApplication - IMPLEMENTADO
```
Crea la clase PinwoodApplication que extiende de Application para inicializar Firebase y ARCore al inicio de la aplicación. Debe incluir:

- Inicialización de Firebase
- Configuración de persistencia de Firestore
- Verificación de disponibilidad de ARCore
- Inicialización de la caché de imágenes con Glide
```

## Sprint 2: Autenticación

#### ✅ Prompt 4: Implementar LoginActivity - IMPLEMENTADO
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

#### ✅ Prompt 5: Implementar RegisterActivity - IMPLEMENTADO
```
Crea la RegisterActivity completa con las siguientes características:

- Layout con campos para nombre, email, password y confirmación de password
- Validación de todos los campos (email válido, coincidencia de passwords, etc.)
- Integración con Firebase Authentication para crear nuevos usuarios
- Creación del documento del usuario en Firestore después del registro exitoso
- Manejo de errores y respuestas visuales al usuario
```

#### ✅ Prompt 6: Implementar clases de modelo User y Address - IMPLEMENTADO
```
Crea las clases de modelo User y Address para la aplicación de venta de muebles. Incluye:

- Todos los campos necesarios (userId, name, email, phone, etc. para User)
- Campos de dirección (street, city, state, zipCode, country) para Address
- Constructor, getters y setters
- Métodos toMap() y fromMap() para conversión a/desde Firestore

Usa Java, no Kotlin.
```

## Sprint 3: Home y Catálogo

#### ⚠️ Prompt 7: Implementar HomeActivity y HomeFragment - PARCIALMENTE IMPLEMENTADO
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
**Estado:** Implementado parcialmente. Existe la estructura básica de HomeActivity y HomeFragment, pero falta:
- Implementación completa de adaptadores para RecyclerViews
- Carousel para categorías
- Integración con Firestore
- Implementación de ViewModels (patrón MVVM)

#### ❌ Prompt 8: Implementar ProductRepository - PENDIENTE
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

#### ❌ Prompt 9: Implementar ProductListFragment y Adapter - PENDIENTE
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

## Sprint 4: Detalle de Producto y AR

#### ❌ Prompt 10: Implementar ProductDetailActivity - PENDIENTE
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

#### ❌ Prompt 11: Implementar ARActivity básica - PENDIENTE
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

#### ❌ Prompt 12: Implementar carga y renderización de modelos 3D - PENDIENTE
```
Crea las clases necesarias para cargar y renderizar modelos 3D de muebles en la experiencia AR:

- ModelLoader para cargar modelos desde Firebase Storage o assets
- Renderer que maneja el ciclo de vida del renderizado
- TransformationManager para manejar gestos de escala, rotación y traslación
- MaterialManager para cambiar colores y texturas
- CaptureManager para tomar capturas de pantalla de la experiencia AR

Todo implementado con Java usando ARCore.
```

## Sprint 5: Carrito y Checkout

#### ❌ Prompt 13: Implementar CartFragment y CartManager - PENDIENTE
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

#### ❌ Prompt 14: Implementar CheckoutActivity - PENDIENTE
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

## Sprint 6: Mejoras y Optimización

#### ❌ Prompt 15: Implementar medición de dimensiones en AR - PENDIENTE
```
Crea la funcionalidad para medir dimensiones en el espacio real usando ARCore:

- Clase MeasurementManager que permite medir distancias
- UI para marcar puntos de inicio y fin
- Visualización de la medida en metros/centímetros
- Representación visual de la línea de medición
- Botón para guardar medidas y asociarlas a un producto
```

#### ❌ Prompt 16: Optimizar carga de modelos 3D - PENDIENTE
```
Implementa un sistema optimizado para la carga y caché de modelos 3D:

- ModelManager para gestionar la descarga y almacenamiento local de modelos
- Caché para evitar descargar modelos ya usados
- Sistema de precarga de modelos en segundo plano
- Compresión y optimización de modelos
- Monitoreo de rendimiento y liberación de memoria

Optimiza el código existente para mejorar el rendimiento de AR.
```

## Resumen del estado actual:

- **Prompts implementados**: 6 (Prompts 1-6)
- **Prompts parcialmente implementados**: 1 (Prompt 7)
- **Prompts pendientes**: 9 (Prompts 8-16)

El proyecto tiene implementada la estructura básica, configuración con Firebase, sistema de autenticación completo y modelos de datos principales. La navegación básica con HomeActivity está parcialmente implementada.

Falta desarrollar toda la funcionalidad principal:
- Catálogo de productos
- Experiencia de realidad aumentada (AR)
- Sistema de carrito y checkout
- Funcionalidades avanzadas como medición de dimensiones en AR