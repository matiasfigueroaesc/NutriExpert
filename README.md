# NutriExpert 🌱

App Android para planificar la **nutrición vegetal y el fertirriego** de predios agrícolas. Corresponde al módulo 3 del prototipo [NutriExpert](https://nutriexpert.netlify.app/) y se desarrolla para un cliente real como proyecto de la asignatura de Desarrollo de Aplicaciones Móviles (Duoc UC).

La app es **offline-first**: todo funciona sin internet y los datos se guardan en el teléfono.

## Equipo

| Integrante         | Responsabilidad principal |
|--------------------|---|
| Matías Figueroa    | Predios (Room + ViewModel), lista y formulario de predio, animaciones |
| Estefanía Ruiz        | Crear cuenta, recuperar contraseña, configuración de Room, validación con error e ícono por campo |
| Fabián Rubio Parra | Recursos nativos (GPS y cámara/galería), README y tablero de tareas |

## Funcionalidades (etapa 1)

- **Cuenta de usuario:** creación de cuenta e inicio de sesión reales (guardados en el teléfono), cierre de sesión y recuperación de contraseña (requiere servidor).
- **Sectores y análisis:** cada predio se divide en sectores con su cultivo, y cada sector guarda sus análisis de suelo y agua.
- **Plan nutricional:** asistente de 4 pasos para calcular los fertilizantes de un sector.
- **Predios:** crear, listar y editar predios agrícolas.
  - **Ubicación GPS:** botón "Usar mi ubicación" que guarda la latitud y longitud del predio.
  - **Foto del predio:** tomar una foto con la cámara o elegirla de la galería, con vista previa.
- **Formularios validados:** cada campo muestra su propio mensaje de error e ícono; la validación está separada de la pantalla.
- **Animaciones** en la navegación y en los elementos de la interfaz.
- **Persistencia local** con Room (funciona sin conexión).
- En desarrollo dentro de la etapa 1: análisis, plan de nutrición, calendario de riego y reporte.

> Quedan para la etapa 2: asesor, integración con WhatsApp, panel de administración y diagnóstico por foto.

## Recursos nativos y permisos

| Recurso | Permiso | Uso en la app |
|---|---|---|
| Ubicación (GPS) | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Registrar la latitud y longitud del predio |
| Cámara | `CAMERA` | Tomar la foto del predio |
| Galería | No requiere permiso (Photo Picker de Android) | Elegir una foto existente |

Los permisos se piden en tiempo de ejecución, solo cuando el usuario toca el botón correspondiente. Si los rechaza, la app explica por qué los necesita y ofrece abrir los Ajustes.

## Seguridad y autenticación

La app funciona sin internet, así que las cuentas se guardan en el teléfono (Room):

- **Contraseñas con hash:** nunca se guarda la contraseña. Se guarda un hash PBKDF2 con sal aleatoria por usuario y 100.000 iteraciones (`seguridad/Contrasenas.kt`). La verificación compara en tiempo constante.
- **Contraseñas exigentes:** mínimo 8 caracteres, con letras y números.
- **Sesión:** se guarda solo el id del usuario en preferencias privadas (`seguridad/SesionManager.kt`). Si hay sesión, la app abre directo en "Mis predios"; al cerrar sesión se limpia la pila de pantallas.
- **Datos separados por usuario:** cada predio tiene dueño (`usuarioId`) y todas las consultas filtran por él, así un usuario no ve ni borra predios de otro.
- **Bloqueo por intentos:** tras 5 intentos fallidos el login se bloquea 30 segundos. El mensaje de error no revela si el correo existe.
- **Sin copias de seguridad:** la base de datos y la sesión se excluyen de las copias en la nube y del traspaso a otro teléfono.

> **API / backend:** todavía no está conectada (no hay Retrofit ni permiso de internet). `AuthRepository` y los repositorios son el punto donde se agregará la llamada a la API sin cambiar pantallas ni ViewModels.

## Tecnologías

- **Kotlin** + **Jetpack Compose** (Material 3)
- Arquitectura **MVVM**: Screen → ViewModel → Repositorio → Room
- **Room** como base de datos local
- Android Gradle Plugin **9.3.1**
- Paquete `cl.duoc.nutriexpert` · minSdk 24

## Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/matiasfigueroaesc/NutriExpert.git
   ```
2. Abrir la carpeta en **Android Studio** (File → Open).
3. Esperar a que termine la sincronización de Gradle.
   ⚠️ Si Android Studio ofrece el *AGP Upgrade Assistant*, **no lo aceptes**: el proyecto usa AGP 9.3.1.
4. Conectar un teléfono Android (con depuración USB activada) o iniciar un emulador con Android 7.0 (API 24) o superior.
5. Presionar **Run ▶** (`app`).

**Para probar GPS y cámara:**
- En un teléfono real, simplemente acepta los permisos cuando la app los pida.
- En el emulador, la ubicación se simula desde *Extended controls (⋯) → Location*, y la cámara usa una escena virtual.

## Estructura del proyecto

```
app/src/main/java/cl/duoc/nutriexpert/
├── data/          # Entidades, DAOs, base de datos Room y repositorios
├── domain/        # Modelos y motor de cálculo del plan nutricional
├── nativo/        # Ayudantes de GPS (UbicacionHelper) y cámara/galería (FotoHelper)
├── seguridad/     # Hash de contraseñas y sesión del usuario
├── ui/
│   ├── components/  # Componentes reutilizables (barra superior, botones, campos, ubicación, foto)
│   ├── plan/        # Asistente del plan nutricional
│   └── views/       # Pantallas de la app
├── utils/         # Validaciones de formularios
└── viewmodel/     # ViewModels
```

## Forma de trabajo

- `main`: versión estable · `develop`: integración · `feature/...`: una rama por tarea.
- Cada rama se une a `develop` mediante Pull Request.
- Commits en español con prefijo `feat:` o `fix:`.
- Tareas organizadas en Trello, una tarjeta por tarea con su responsable.
- Tablero de tareas: [Trello NutriExpert](https://trello.com/b/PKewiNe3)
