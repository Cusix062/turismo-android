# Turismo Android App

App de turismo con exploracion inteligente, mapa interactivo y gestion de favoritos.

## Arquitectura

```
Android App (Kotlin + Jetpack Compose)  <--->  Backend (Node.js + Express)
```

## Tecnologias

- **Android**: Kotlin, Jetpack Compose, MVVM, Retrofit, Coil, OSMDroid
- **Backend**: Node.js, Express
- **Mapas**: OSMDroid (OpenStreetMap) - 100% gratis, sin API keys
- **CI/CD**: Docker + Jenkins

## Estructura

```
turismo-android/
├── app/src/main/java/com/turismo/app/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── ApiClient.kt
│   │   ├── Models.kt
│   │   └── TurismoApi.kt
│   └── ui/
│       ├── HomeScreen.kt        # Home con busqueda, populares, nuevos
│       ├── MapScreen.kt         # Mapa OSMDroid interactivo
│       ├── LugaresScreen.kt     # Lista de lugares
│       ├── FavoritosScreen.kt   # Favoritos del usuario
│       ├── TurismoRoot.kt       # Navegacion bottom bar
│       ├── TurismoViewModel.kt  # Estado y logica
│       └── theme/Theme.kt       # Tema personalizado
├── backend/
│   ├── server.js                # API REST con Express
│   ├── Dockerfile               # Imagen Docker
│   └── package.json
├── docker-compose.yml           # Orquestacion
├── Jenkinsfile                  # Pipeline CI/CD
└── build.gradle.kts
```

## Configuracion

### Backend (local)

```bash
cd backend
npm install
npm start
```

El backend corre en `http://0.0.0.0:3000`

### Backend (Docker)

```bash
docker-compose up -d --build
```

### Configurar IP del backend

En `app/build.gradle.kts`, cambia la IP segun tu red:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://TU_IP:3000/\"")
```

- **Emulador**: `10.0.2.2`
- **Dispositivo fisico**: IP LAN de tu PC (ej. `192.168.1.x`)

## Endpoints API

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/lugares` | Todos los lugares |
| GET | `/api/lugares/:id` | Detalle de un lugar |
| GET | `/api/lugares/search?q=` | Busqueda |
| GET | `/api/lugares/populares` | Top 5 populares |
| GET | `/api/lugares/nuevos` | Top 5 nuevos |
| GET | `/api/lugares/categoria/:cat` | Filtro por categoria |
| GET | `/api/usuarios/:id/favoritos` | Favoritos del usuario |
| POST | `/api/usuarios/:id/favoritos` | Agregar favorito |
| DELETE | `/api/usuarios/:id/favoritos/:lugarId` | Eliminar favorito |

## CI/CD con Jenkins

1. Instala Jenkins con el plugin **Pipeline** y **GitHub**
2. Crea un nuevo proyecto tipo **Pipeline**
3. Configura la URL del repo de GitHub
4. El `Jenkinsfile` se ejecuta automaticamente

### Stages del pipeline:

1. **Checkout** - Obtiene el codigo
2. **Lint Backend** - Verifica dependencias Node.js
3. **Build Backend Docker** - Construye la imagen
4. **Lint Android** - Ejecuta lint de Android
5. **Build Android APK** - Genera la APK debug
6. **Unit Tests** - Ejecuta tests unitarios
7. **Deploy Backend** - Despliega con docker-compose (solo main)

## Funcionalidades

- Busqueda con autocompletado
- Sugerencias segun ubicacion actual
- Lugares populares cerca de ti
- Nuevos lugares agregados
- Mapa interactivo con marcadores por categoria
- Centrar en mi ubicacion
- Filtro de categorias en mapa
- Gestion de favoritos (agregar/eliminar)
- 15 lugares turisticos de Mexico precargados
