# Master Martini — App móvil (DSY1105)

App Android para clientes de **Master Martini Chile** (insumos de pastelería y panadería): contenidos para aprender, catálogo con cotizaciones, servicios (eventos, asesorías y cursos) y seguimiento de pedidos.
MVP académico con datos de ejemplo, construido a partir del mockup del equipo.

**Stack:** Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow · Room · DataStore
**Requisitos:** Android Studio Ladybug (2024.2) o superior · JDK 17 · minSdk 26 · targetSdk 35

## Cómo abrirla

1. `git clone` del repo → *File › Open* en Android Studio → esperar el *Gradle Sync*.
2. Ejecutar en un emulador con **Android 13 o superior** (para probar el permiso de notificaciones).
3. Crear una cuenta en la pantalla de registro (los datos quedan guardados en el teléfono).

Tests unitarios de validaciones: `./gradlew test` (o clic derecho en `ValidatorsTest` › *Run*).

## Pantallas

| Pantalla | Qué hace |
|---|---|
| Registro | Formulario completo (nombre, correo, teléfono, contraseña, tipo y tamaño del negocio, intereses, consentimiento) |
| Login | Correo + contraseña; la sesión queda guardada |
| Inicio | Saludo personalizado, video destacado, recetas según el tipo de negocio, próximo evento, botón **Aa** (texto grande) |
| Aprende | Búsqueda (ignora tildes), pestañas Videos/Recetas/Cursos, filtro por categoría → detalle con pasos, insumos y **compartir** |
| Catálogo | Búsqueda y categorías; hoja inferior para elegir formato y cantidad; barra de cotización |
| Enviar cotización | Formulario de despacho (contacto, dirección, comuna, fecha, comentarios) → notificación + aviso por **WhatsApp** |
| Servicios | Eventos (inscripción con confirmación), asesorías (reserva) y cursos con progreso |
| Reservar asesoría | Formulario con fecha, horario, modalidad, dirección (solo si es en el local), tema y descripción |
| Mi cuenta | Seguimiento de pedido, repetir pedido, cotizaciones enviadas, reservas, **llamar / WhatsApp** a ventas, texto grande, alto contraste, notificaciones, cerrar sesión |
| Editar perfil | Formulario para actualizar los datos del negocio |

## Cómo cumple la rúbrica

| Indicador | Dónde se evidencia |
|---|---|
| **IE 2.1.1** Interfaz y navegación | Material 3 con la paleta del mockup (`ui/theme`), barra inferior de 5 pestañas, `NavHost` con rutas centralizadas (`ui/navigation/AppNavigation.kt`), back stack correcto, estado de cada pestaña conservado. Accesibilidad: texto grande, alto contraste, `contentDescription` y encabezados semánticos |
| **IE 2.1.2** Formularios con validación visual | 6 formularios: registro, login, agregar a cotización, enviar cotización, reservar asesoría, editar perfil. Cada campo muestra ícono descriptivo, ✓ verde si es válido, ⚠ rojo + mensaje bajo el campo si no lo es (`ui/components/FormComponents.kt`) |
| **IE 2.2.1** Validación centralizada y desacoplada | Reglas puras en `domain/validation/Validators.kt` (sin Android, con tests en `src/test`). Los ViewModels usan esas reglas y exponen `StateFlow`; las pantallas solo dibujan el estado |
| **IE 2.2.2** Animaciones | Transiciones entre pantallas, barra inferior que entra/sale, íconos que crecen al seleccionarse, errores que se expanden, sacudida al enviar un formulario inválido, botón "Agregado" animado, barra de cotización deslizante con contador, progreso de cursos que se llena, paso actual del pedido que late, tarjetas que se hunden al tocarlas, cambio de colores suave al activar alto contraste |
| **IE 2.3.1** Modularidad y persistencia local | Capas `data` (Room + DataStore + repositorios) · `domain` (validación) · `ui` (pantallas, componentes, ViewModels) · `device` (recursos nativos). Room guarda usuarios (contraseña con hash + sal), cotizaciones y reservas; DataStore guarda sesión y preferencias |
| **IE 2.4.1** Recursos nativos (2) | **1. Notificaciones** (`device/NotificationHelper.kt`): canal propio, permiso `POST_NOTIFICATIONS` pedido en contexto, la app sigue funcionando si se rechaza. **2. Teléfono / WhatsApp / Compartir** (`device/ContactHelper.kt`): `ACTION_DIAL` (sin permiso peligroso), WhatsApp con respaldo a wa.me, hoja nativa de compartir; `<queries>` declarado en el Manifest |

## Estructura

```
app/src/main/java/cl/duoc/mastermartini/
├── MainActivity.kt, MasterMartiniApp.kt, AppContainer.kt   ← arranque e inyección de dependencias
├── data/
│   ├── local/        Room (entidades, DAOs, BD) y DataStore (sesión y ajustes)
│   ├── model/        Modelos de dominio
│   └── repository/   UserRepository, QuoteRepository, ReservationRepository, ContentRepository
├── domain/validation/ Validators + FieldState (lógica de validación centralizada)
├── device/           Notificaciones, permiso, teléfono/WhatsApp/compartir
└── ui/
    ├── theme/        Colores, tipografía, alto contraste y texto grande
    ├── components/   Campos con validación, chips, botones, tarjetas, selector de fecha
    ├── navigation/   Rutas, NavHost y barra inferior
    └── screens/      auth · home · learn · catalog · services · account (pantalla + ViewModel)
```

## Para adaptar a Master Martini más adelante

- Los contenidos salen de `FakeContentRepository`: para conectarlo a una API real basta otra clase que implemente `ContentRepository` y cambiarla en `AppContainer`.
- Teléfono de ventas: `salesPhone()` en el mismo repositorio.
- Colores de marca: `ui/theme/Theme.kt`. Logo: `res/drawable/ic_launcher_foreground.xml` y `BrandHeader`.
