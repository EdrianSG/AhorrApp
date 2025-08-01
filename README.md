# AhorrApp - Aplicación de Gestión Financiera Personal

## Descripción

AhorrApp es una aplicación móvil Android desarrollada en Kotlin que permite a los usuarios gestionar sus finanzas personales de manera eficiente. La aplicación incluye funcionalidades para el seguimiento de transacciones, establecimiento de metas de ahorro, pagos programados y límites de gasto por categorías.

## Características Principales

### 📊 Dashboard
- Vista general de balance actual
- Resumen de ingresos y gastos
- Progreso de metas de ahorro
- Límites de gasto por categorías

### 💰 Gestión de Transacciones
- Registro de ingresos y gastos
- Categorización automática
- Historial completo de transacciones
- Vista de calendario para transacciones

### 🎯 Metas de Ahorro
- Creación de metas personalizadas
- Seguimiento del progreso
- Depósitos automáticos a metas
- Integración con el balance general

### ⏰ Pagos Programados
- Configuración de pagos recurrentes
- Notificaciones automáticas
- Ejecución automática de transacciones
- Gestión de intervalos de repetición

### 📈 Límites de Gasto
- Establecimiento de límites por categoría
- Seguimiento del progreso de gasto
- Alertas visuales cuando se exceden límites
- Actualización automática con transacciones

### 🔐 Autenticación
- Sistema de registro e inicio de sesión
- Gestión de sesiones de usuario
- Datos específicos por usuario

## Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Base de Datos**: Room Database
- **Inyección de Dependencias**: Hilt
- **Navegación**: Navigation Component
- **UI**: Material Design Components
- **Corrutinas**: Kotlin Coroutines
- **Notificaciones**: AlarmManager

## Estructura del Proyecto

```
app/src/main/java/com/example/ahorrrapp/
├── adapter/                 # Adaptadores para RecyclerViews
├── data/                    # Capa de datos
│   ├── dao/                 # Data Access Objects
│   ├── model/               # Entidades de base de datos
│   ├── repository/          # Repositorios
│   └── util/                # Utilidades de datos
├── di/                      # Módulos de inyección de dependencias
├── receiver/                # Broadcast Receivers
├── service/                 # Servicios en segundo plano
├── ui/                      # Fragmentos de UI
│   ├── auth/                # Autenticación
│   ├── calendar/            # Vista de calendario
│   └── scheduled/           # Pagos programados
├── utils/                   # Utilidades generales
└── viewmodel/               # ViewModels
```

## Instalación y Configuración

### Requisitos Previos
- Android Studio Arctic Fox o superior
- Android SDK API 24+
- Gradle 7.0+

### Pasos de Instalación

1. Clona el repositorio:
```bash
git clone [URL_DEL_REPOSITORIO]
cd AhorrApp
```

2. Abre el proyecto en Android Studio

3. Sincroniza las dependencias de Gradle

4. Ejecuta la aplicación en un dispositivo o emulador

## Configuración de la Base de Datos

La aplicación utiliza Room Database con las siguientes entidades principales:

- **User**: Información de usuarios
- **Transaction**: Transacciones financieras
- **SavingsGoal**: Metas de ahorro
- **ScheduledPayment**: Pagos programados
- **CategoryLimit**: Límites de gasto por categoría

## Funcionalidades Técnicas

### Sistema de Notificaciones
- AlarmManager para pagos programados
- BroadcastReceiver para manejo de alarmas
- Notificaciones automáticas

### Gestión de Estado
- ViewModels para manejo de estado de UI
- LiveData para observación de cambios
- Corrutinas para operaciones asíncronas

### Persistencia de Datos
- Room Database para almacenamiento local
- DAOs para acceso a datos
- Repositorios para lógica de negocio

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Contacto

Para preguntas o soporte, contacta al equipo de desarrollo.

---

**Versión**: 1.0.0  
**Última actualización**: Diciembre 2024 