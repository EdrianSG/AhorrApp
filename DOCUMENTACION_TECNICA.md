# Documentación Técnica - AhorrApp

## Arquitectura del Sistema

### Patrón MVVM (Model-View-ViewModel)

La aplicación sigue el patrón MVVM para separar la lógica de negocio de la interfaz de usuario:

- **Model**: Entidades de Room Database y Repositorios
- **View**: Fragmentos y Activities
- **ViewModel**: Manejo de estado y lógica de negocio

### Diagrama de Arquitectura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      View       │    │   ViewModel     │    │      Model      │
│                 │    │                 │    │                 │
│ - Fragments     │◄──►│ - LiveData      │◄──►│ - Room Database │
│ - Activities    │    │ - Coroutines    │    │ - Repositories  │
│ - Adapters      │    │ - State         │    │ - DAOs          │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Base de Datos

### Esquema de Base de Datos

#### Tabla: users
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email TEXT,
    created_at INTEGER NOT NULL
);
```

#### Tabla: transactions
```sql
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    description TEXT,
    category TEXT NOT NULL,
    type TEXT NOT NULL,
    date INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

#### Tabla: savings_goals
```sql
CREATE TABLE savings_goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    target_amount REAL NOT NULL,
    current_amount REAL NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

#### Tabla: scheduled_payments
```sql
CREATE TABLE scheduled_payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    repeat_interval TEXT NOT NULL,
    next_payment_date INTEGER NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

#### Tabla: category_limits
```sql
CREATE TABLE category_limits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    category TEXT NOT NULL,
    limit_amount REAL NOT NULL,
    spent_amount REAL NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

### Migraciones

La base de datos incluye migraciones para:
- Creación inicial de tablas
- Adición de nuevas columnas
- Modificación de esquemas existentes

## Componentes Principales

### 1. Sistema de Autenticación

#### Clases Principales:
- `AuthViewModel`: Maneja la lógica de autenticación
- `UserDao`: Acceso a datos de usuarios
- `UserRepository`: Lógica de negocio para usuarios
- `SessionManager`: Gestión de sesiones

#### Flujo de Autenticación:
1. Usuario ingresa credenciales
2. `AuthViewModel` valida con `UserRepository`
3. `SessionManager` almacena la sesión
4. Navegación al dashboard principal

### 2. Gestión de Transacciones

#### Clases Principales:
- `TransactionViewModel`: Manejo de transacciones
- `TransactionDao`: Acceso a datos de transacciones
- `TransactionRepository`: Lógica de negocio
- `TransactionAdapter`: Visualización en listas

#### Tipos de Transacciones:
- **INGRESO**: Aumenta el balance
- **GASTO**: Disminuye el balance

#### Integración con Otros Módulos:
- Actualiza límites de categoría automáticamente
- Crea transacciones automáticas para metas de ahorro
- Ejecuta pagos programados

### 3. Metas de Ahorro

#### Clases Principales:
- `SavingsGoalViewModel`: Gestión de metas
- `SavingsGoalDao`: Acceso a datos
- `SavingsGoalRepository`: Lógica de negocio
- `SavingsGoalAdapter`: Visualización

#### Funcionalidades:
- Creación de metas personalizadas
- Seguimiento de progreso
- Depósitos automáticos
- Integración con balance general

### 4. Pagos Programados

#### Clases Principales:
- `ScheduledPaymentViewModel`: Gestión de pagos
- `ScheduledPaymentDao`: Acceso a datos
- `ScheduledPaymentRepository`: Lógica de negocio
- `PaymentAlarmReceiver`: Manejo de alarmas

#### Sistema de Alarmas:
- `AlarmManager` para programar pagos
- `BroadcastReceiver` para ejecutar pagos
- Notificaciones automáticas
- Ejecución de transacciones automáticas

### 5. Límites de Categoría

#### Clases Principales:
- `CategoryLimitViewModel`: Gestión de límites
- `CategoryLimitDao`: Acceso a datos
- `CategoryLimitRepository`: Lógica de negocio
- `CategoryLimitAdapter`: Visualización

#### Funcionalidades:
- Establecimiento de límites por categoría
- Seguimiento automático de gastos
- Alertas visuales
- Actualización con transacciones

## Inyección de Dependencias (Hilt)

### Módulos de Inyección

#### AppModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Providers para base de datos
    // Providers para DAOs
    // Providers para repositorios
    // Providers para ViewModels
}
```

### Dependencias Inyectadas

- **Database**: `FinanzasDatabase`
- **DAOs**: Todos los Data Access Objects
- **Repositories**: Todos los repositorios
- **ViewModels**: Con sus factories correspondientes

## Gestión de Estado

### LiveData y StateFlow

La aplicación utiliza LiveData para la observación de cambios en la UI:

```kotlin
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions
    
    fun loadTransactions(userId: Int) {
        viewModelScope.launch {
            val result = repository.getTransactionsByUser(userId)
            _transactions.value = result
        }
    }
}
```

### Corrutinas

Todas las operaciones de base de datos se ejecutan en corrutinas:

```kotlin
suspend fun addTransaction(transaction: Transaction): Long {
    return withContext(Dispatchers.IO) {
        transactionDao.insert(transaction)
    }
}
```

## Navegación

### Navigation Component

La aplicación utiliza Navigation Component para la navegación entre pantallas:

#### Nav Graph (`nav_graph.xml`)
- Login/Register → Dashboard
- Dashboard → Transacciones
- Dashboard → Metas de Ahorro
- Dashboard → Pagos Programados
- Dashboard → Configuración

### Bottom Navigation

Navegación principal entre secciones:
- Dashboard
- Transacciones
- Calendario
- Resumen
- Configuración

## Sistema de Notificaciones

### AlarmManager

```kotlin
class PaymentAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Ejecutar pago programado
        // Crear transacción automática
        // Programar siguiente pago
    }
}
```

### Configuración de Alarmas

- **Tipo**: AlarmManager.RTC_WAKEUP
- **Persistencia**: PendingIntent con FLAG_UPDATE_CURRENT
- **Repetición**: Basada en intervalos configurados

## Utilidades

### DateConverter
Conversión entre fechas y timestamps para Room Database.

### CurrencyUtils
Formateo de moneda para la interfaz de usuario.

### SessionManager
Gestión de sesiones de usuario con SharedPreferences.

## Testing

### Pruebas Unitarias
- ViewModels
- Repositories
- DAOs
- Utilidades

### Pruebas de Integración
- Base de datos
- Navegación
- Inyección de dependencias

## Configuración de Build

### Gradle Dependencies

```gradle
dependencies {
    // Room
    implementation "androidx.room:room-runtime:2.5.0"
    implementation "androidx.room:room-ktx:2.5.0"
    kapt "androidx.room:room-compiler:2.5.0"
    
    // Hilt
    implementation "com.google.dagger:hilt-android:2.44"
    kapt "com.google.dagger:hilt-compiler:2.44"
    
    // Navigation
    implementation "androidx.navigation:navigation-fragment-ktx:2.5.3"
    implementation "androidx.navigation:navigation-ui-ktx:2.5.3"
    
    // Material Design
    implementation "com.google.android.material:material:1.8.0"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"
}
```

## Consideraciones de Rendimiento

### Optimizaciones Implementadas

1. **Lazy Loading**: Carga de datos bajo demanda
2. **Paginación**: Para listas grandes de transacciones
3. **Caching**: Datos en memoria para acceso rápido
4. **Background Processing**: Operaciones pesadas en segundo plano

### Monitoreo de Rendimiento

- Uso de Android Profiler
- Análisis de memoria
- Optimización de consultas de base de datos

## Seguridad

### Almacenamiento Seguro
- Contraseñas hasheadas
- Datos sensibles en SharedPreferences encriptados
- Validación de entrada de usuario

### Validaciones
- Entrada de usuario sanitizada
- Validación de tipos de datos
- Manejo de errores robusto

## Mantenimiento

### Logs y Debugging
- Logs estructurados para debugging
- Manejo de errores con try-catch
- Información de depuración en desarrollo

### Actualizaciones
- Migraciones de base de datos
- Actualización de dependencias
- Compatibilidad con nuevas versiones de Android 