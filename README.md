# GameHubMobile

Aplicación Android desarrollada con **Kotlin** y **Jetpack Compose** en Android Studio.

Actualmente el proyecto implementa una base funcional centrada en la **autenticación de usuarios**, incluyendo:

- inicio de sesión
- registro
- persistencia local de sesión
- navegación entre pantallas
- cierre de sesión

## Tecnologías principales

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit + Gson
- OkHttp
- Room
- Coroutines
- Coil

## Estructura general

El proyecto sigue una separación sencilla por capas:

- `ui/`: pantallas, navegación, estado visual y tema
- `data/remote/`: acceso a la API
- `data/local/`: persistencia local con Room
- `data/repository/`: lógica de autenticación y sesión
- `di/`: creación manual de dependencias

## Requisitos

- Android Studio
- JDK 17
- `minSdk = 24`
- Un backend accesible desde la URL configurada en la app

## Configuración de la API

La URL base del servidor se define en `app/build.gradle.kts` mediante `API_BASE_URL`.

En el estado actual del proyecto, esa URL se usa al construir Retrofit dentro de `di/AppContainer.kt`.

## Ejecución rápida

1. Abre el proyecto en Android Studio.
2. Espera a que Gradle sincronice.
3. Revisa la URL configurada para la API.
4. Ejecuta la app en un dispositivo físico o emulador.

## Estado del proyecto

La app está en una fase inicial pero ya tiene una arquitectura base limpia para seguir ampliando funcionalidades.

La pantalla principal actual es todavía provisional, por lo que el foco real del proyecto está en la autenticación, la sesión y la comunicación con backend.

