# GameHubMobile

Cliente Android de **GameHub** desarrollado con **Kotlin** y **Jetpack Compose**.

Este repositorio recoge una aplicación móvil orientada a la gestión de acceso, consulta de catálogo y creación de reservas en un entorno de juegos de mesa. El proyecto mantiene un enfoque práctico de desarrollo, pero con una organización y documentación suficientemente formal como para servir también como base técnica de memoria, entrega académica o portfolio profesional.

## 1. Resumen del proyecto

En su estado actual, la app permite:

- autenticación contra backend
- registro de usuarios estándar
- persistencia local de sesión
- navegación protegida según rol (`USER` / `ADMIN`)
- consulta de catálogo de juegos
- visualización de detalle de juego
- creación de reservas mediante wizard por pasos
- consulta de reservas de usuario y administración
- gestión de perfil y cierre de sesión

## 2. Alcance funcional actual

### Funcionalidades operativas

- **Login** contra backend
- **Registro** de usuario estándar (`USER`)
- **Persistencia de sesión** con Room
- **Session gate** inicial para restaurar sesión
- **Navegación por rol**
- **Shell principal con pestañas** adaptadas al tipo de usuario
- **Dashboard** con resumen rápido del catálogo
- **Catálogo de juegos**
- **Detalle de juego**
- **Wizard de reserva** por pasos:
  - fecha
  - turno
  - mesa libre
  - juego opcional
  - confirmación
- **Consulta de reservas**:
  - mis reservas
  - reservas globales de administración
- **Perfil y logout**

### Áreas provisionales o pendientes

- `ManagementScreen`: accesos administrativos base para futuros CRUD
- `UsersScreen`: placeholder para gestión detallada de usuarios/roles
- la fecha del wizard sigue siendo textual (`yyyy-MM-dd`) en la UI actual
- la cobertura `androidTest` sigue siendo menor que la cobertura unitaria

## 3. Navegación y experiencia de uso

La navegación principal está centralizada en `AppNavHost.kt`.

### Flujo general

1. `SessionGate` comprueba si existe una sesión persistida.
2. Si no hay sesión, el usuario es redirigido a `login`.
3. Si existe sesión, la app resuelve el grafo protegido según el rol:
   - `UserGraph`
   - `AdminGraph`
4. Ambos grafos cargan `MainShellRoute`, que actúa como shell principal con pestañas.

### Pestañas por rol

**Usuario estándar (`USER`)**

- Inicio
- Reservar
- Mis reservas
- Catálogo
- Perfil

**Administrador (`ADMIN`)**

- Inicio
- Reservas
- Gestión
- Usuarios
- Perfil

## 4. Arquitectura del proyecto

La app sigue una organización por capas ligera, con composición manual de dependencias desde `AppContainer.kt`.

### Estructura general

- `app/src/main/java/.../ui/`
  - pantallas Compose
  - navegación
  - estado visual
  - componentes reutilizables
- `app/src/main/java/.../data/remote/`
  - APIs Retrofit
  - DTOs
  - data sources remotos
- `app/src/main/java/.../data/local/`
  - Room
  - cache local
  - persistencia de sesión
- `app/src/main/java/.../data/repository/`
  - coordinación entre red, cache y dominio
  - validaciones y transformación de datos
- `app/src/main/java/.../domain/`
  - contratos del negocio de reservas
- `app/src/main/java/.../di/`
  - construcción manual de Retrofit, OkHttp, Room y repositorios

### Decisiones técnicas destacables

- **Room** se utiliza para sesión persistida y cache del catálogo.
- **Retrofit + Gson** gestionan la comunicación con el backend.
- **OkHttp** añade automáticamente el token JWT mediante interceptor.
- **GameRepository** aplica estrategia **network-first con fallback a cache local**.
- **ReservationRepository** centraliza validación y envío del flujo de reservas.

## 5. Stack tecnológico

- **Kotlin 2.3.20**
- **Android Gradle Plugin 9.1.0**
- **Gradle 9.3.1**
- **Jetpack Compose** (BOM `2026.03.00`)
- **Material 3**
- **Navigation Compose**
- **ViewModel + Coroutines**
- **Retrofit 3 + Gson**
- **OkHttp 5**
- **Room 2.8.4**
- **Coil**
- **JUnit 4 + kotlinx-coroutines-test**

## 6. Requisitos de entorno

- **Android Studio** reciente
- **JDK 17** compatible
  - recomendado: usar el **JBR de Android Studio**
- `compileSdk = 36`
- `targetSdk = 35`
- `minSdk = 24`
- SDK y platform tools instalados desde Android Studio
- un backend accesible desde la URL configurada en la app

## 7. Configuración del backend

La URL base de la API se define en `app/build.gradle.kts` mediante `BuildConfig.API_BASE_URL`.
La URL se utiliza en `AppContainer.kt` para construir la instancia de Retrofit.

### Notas de integración

- si ejecutas la app en **emulador** o **dispositivo físico**, puede ser necesario adaptar la IP según la red local
- las llamadas protegidas incluyen automáticamente la cabecera `Authorization: Bearer <token>`
- los endpoints `auth/login` y `auth/register` no reciben el token por interceptor

## 8. API esperada del backend

> Esta sección documenta el **contrato actual esperado por el cliente Android**. No pretende ser una especificación cerrada, pero sí una referencia fiel a cómo la app consume hoy el backend.

### 8.1 Convenciones generales

- base URL configurable mediante `API_BASE_URL`
- payloads y respuestas en **JSON**
- autenticación mediante **JWT Bearer Token**
- los recursos protegidos deben aceptar:

```http
Authorization: Bearer <token>
```

---

### 8.2 Autenticación

#### `POST /auth/login`

Autentica al usuario y devuelve el contexto de sesión.

**Request body esperado**

```json
{
  "nombre": "andres",
  "password": "1234"
}
```

**Response body esperado**

```json
{
  "token": "jwt-token",
  "nombre": "andres",
  "rol": "USER"
}
```

**Notas**

- `rol` se usa para resolver la navegación protegida
- la app persiste localmente `token`, `nombre` y `rol`

#### `POST /auth/register`

Registra un nuevo usuario estándar.

**Request body esperado**

```json
{
  "nombre": "nuevoUsuario",
  "password": "1234",
  "rol": "USER"
}
```

**Response body esperado**

```json
{
  "id": 15,
  "nombre": "nuevoUsuario",
  "rol": "USER"
}
```

**Notas**

- el cliente actual registra siempre con rol `USER`
- tras un registro correcto, la app redirige al login

---

### 8.3 Catálogo de juegos

#### `GET /juegos`

Devuelve el catálogo completo de juegos.

**Response body esperado**

```json
[
  {
    "id": 1,
    "nombre": "Catan",
    "numJugadores": "3-4",
    "categoria": {
      "id": 2,
      "nombre": "Estrategia"
    },
    "disponible": true,
    "descripcion": "Juego de comercio y expansión.",
    "observaciones": "Edición clásica",
    "rutaImagen": "/img/catan.png"
  }
]
```

#### `GET /juegos/categoria/{nom}`

Devuelve juegos filtrados por nombre de categoría.

**Ejemplo**

```http
GET /juegos/categoria/Estrategia
```

#### `GET /juegos/disponibles`

Devuelve únicamente juegos marcados como disponibles.

#### `GET /juegos/{id}`

Devuelve el detalle de un juego concreto.

**Ejemplo**

```http
GET /juegos/10
```

**Notas de contrato para juegos**

- el cliente espera al menos estos campos:
  - `id`
  - `nombre`
  - `numJugadores`
  - `categoria.id`
  - `categoria.nombre`
  - `disponible`
- los campos `descripcion`, `observaciones` y `rutaImagen` pueden ser nulos

---

### 8.4 Reservas

#### `GET /reservas/mis-reservas`

Devuelve las reservas del usuario autenticado.

**Response body esperado**

```json
[
  {
    "id": 21,
    "fecha": "2026-04-25",
    "estado": "CONFIRMADA",
    "mesa": { "numero": 4 },
    "turno": { "id": 2, "nombre": "Tarde" },
    "juego": { "id": 8, "nombre": "Catan" },
    "usuario": { "nombre": "andres" }
  }
]
```

#### `GET /reservas`

Devuelve el listado global de reservas en contexto administrativo.

#### `GET /turnos`

Devuelve los turnos disponibles para reservar.

**Response body esperado**

```json
[
  { "id": 1, "nombre": "Mañana" },
  { "id": 2, "nombre": "Tarde" }
]
```

#### `GET /mesas/operativas`

Devuelve mesas operativas.

**Response body esperado**

```json
[
  { "numero": 1 },
  { "numero": 2 },
  { "numero": 3 }
]
```

#### `GET /mesas/libres?fecha=yyyy-MM-dd&turnoId={id}`

Devuelve las mesas libres para una fecha y turno concretos.

**Ejemplo**

```http
GET /mesas/libres?fecha=2026-04-25&turnoId=2
```

**Response body esperado**

```json
[
  { "numero": 2 },
  { "numero": 6 }
]
```

#### `POST /reservas` como usuario estándar

Crea una reserva para el usuario autenticado.

**Request body esperado**

```json
{
  "fecha": "2026-04-25",
  "mesa": { "numero": 6 },
  "turno": { "id": 2 },
  "juego": { "nombre": "Catan" }
}
```

**Notas**

- `juego` es opcional
- la referencia del juego se envía actualmente por **nombre**, no por `id`

#### `POST /reservas` como administrador

Crea una reserva indicando explícitamente el usuario destinatario.

**Request body esperado**

```json
{
  "fecha": "2026-04-25",
  "mesa": { "numero": 6 },
  "turno": { "id": 2 },
  "juego": { "nombre": "Catan" },
  "usuario": { "nombre": "maria" }
}
```

**Notas**

- en contexto `ADMIN`, el campo `usuario` es obligatorio
- el cliente valida el formato de fecha como `yyyy-MM-dd`
- el cliente también valida turno y mesa antes del envío

---

### 8.5 Códigos de error esperables por el cliente

Aunque el backend puede extenderlos, el cliente ya contempla principalmente estos casos:

- `400`: datos no válidos
- `401`: sesión caducada o no autenticada
- `403`: falta de permisos
- `404`: recurso no encontrado / sin resultados
- `409`: conflicto de disponibilidad al crear una reserva

## 9. Estado funcional resumido

GameHubMobile se encuentra en una fase **funcional intermedia**:

- la base técnica está consolidada
- los flujos principales de usuario ya existen
- la navegación por rol está integrada
- aún quedan áreas administrativas y mejoras de UX por completar

En conjunto, el proyecto puede presentarse tanto como una app Android real en evolución como una base técnica seria para futuras ampliaciones.

