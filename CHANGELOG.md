# Changelog - AhorrApp

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2024-12-XX (vBeta)

### Agregado
- **Migración de Base de Datos Robusta**
  - Implementación de `MIGRATION_6_7` para evitar saltos de versión.
  - Desactivación de `fallbackToDestructiveMigration` para proteger datos reales del usuario.
  - Logs de depuración en migraciones de base de datos.

- **Unificación de Fuente de Datos**
  - Migración completa del **Dashboard** de SQLite puro a **Room Database**.
  - Sincronización en tiempo real de gráficos y totales entre todas las pantallas.

- **Optimización de Arquitectura**
  - Implementación completa de **Hilt** en `HomeFragment`, `SummaryFragment` y `DashboardFragment`.
  - Limpieza de `ViewModelFactory` manuales en favor de inyección de dependencias.
  - Estandarización de `TransactionViewModel` para manejo de periodos y totales por categoría.

### Corregido
- Error que causaba que la aplicación apareciera vacía después de una actualización de base de datos.
- Desincronización entre los datos del Dashboard y la lista de transacciones.
- Problema de cierre inesperado al no encontrar sesión activa o datos iniciales.

## [1.0.0] - 2024-12-XX

### Agregado
- **Sistema de Autenticación**
  - Registro de usuarios con validación
  - Inicio de sesión con credenciales
  - Gestión de sesiones con SessionManager

- **Dashboard Principal**
  - Vista general del balance actual
  - Resumen de ingresos y gastos del mes
  - Tarjetas de metas de ahorro con progreso
  - Tarjetas de límites de categoría

- **Gestión de Transacciones**
  - Agregar transacciones (ingresos y gastos)
  - Categorización automática con iconos
  - Lista de transacciones ordenadas por fecha
  - Edición y eliminación de transacciones

- **Metas de Ahorro**
  - Creación de metas personalizadas
  - Seguimiento de progreso con barras visuales
  - Integración automática con el balance general

- **Pagos Programados**
  - Configuración de pagos recurrentes
  - Sistema de alarmas con AlarmManager
  - Notificaciones automáticas

- **Límites de Categoría**
  - Establecimiento de límites por categoría
  - Seguimiento automático de gastos
  - Alertas visuales cuando se exceden límites

---

**Versión**: 1.0.1 (vBeta)  
**Estado**: Estable y Segura para Datos Reales
