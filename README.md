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

- **Cuenta de usuario:** inicio de sesión, creación de cuenta y recuperación de contraseña.
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
├── nativo/        # Ayudantes de GPS (UbicacionHelper) y cámara/galería (FotoHelper)
├── ui/
│   ├── components/  # Componentes reutilizables (campos con ícono, ubicación, foto)
│   └── views/       # Pantallas de la app
└── viewmodel/     # ViewModels
```

## Forma de trabajo

- `main`: versión estable · `develop`: integración · `feature/...`: una rama por tarea.
- Cada rama se une a `develop` mediante Pull Request.
- Commits en español con prefijo `feat:` o `fix:`.
- Tareas organizadas en Trello, una tarjeta por tarea con su responsable.
- Tablero de tareas: [Trello NutriExpert](https://trello.com/b/PKewiNe3)
