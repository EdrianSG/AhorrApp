# Guía de Desarrollo - AhorrApp

## Introducción

Esta guía está dirigida a desarrolladores que quieran contribuir al proyecto AhorrApp o entender su arquitectura y patrones de desarrollo.

## Configuración del Entorno de Desarrollo

### Requisitos del Sistema

- **Android Studio**: Arctic Fox (2020.3.1) o superior
- **JDK**: Versión 11 o superior
- **Android SDK**: API 24 (Android 7.0) o superior
- **Gradle**: 7.0 o superior
- **Kotlin**: 1.6.0 o superior

### Configuración Inicial

1. **Clonar el repositorio**:
```bash
git clone [URL_DEL_REPOSITORIO]
cd AhorrApp
```

2. **Configurar Android Studio**:
   - Abrir Android Studio
   - Importar proyecto desde `build.gradle`
   - Sincronizar dependencias de Gradle

3. **Configurar dispositivo/emulador**:
   - API 24 o superior
   - Google Play Services (recomendado)

## Arquitectura del Proyecto

### Patrón MVVM

La aplicación sigue estrictamente el patrón MVVM:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      View       │    │   ViewModel     │    │      Model      │
│                 │    │                 │    │                 │
│ - Fragments     │◄──►│ - LiveData      │◄──►│ - Room Database │
│ - Activities    │    │ - Coroutines    │    │ - Repositories  │
│ - Adapters      │    │ - State         │    │ - DAOs          │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Estructura de Paquetes

```
com.example.ahorrrapp/
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

## Convenciones de Código

### Nomenclatura

#### Clases
- **Activities**: `MainActivity`
- **Fragments**: `HomeFragment`, `DashboardFragment`
- **ViewModels**: `TransactionViewModel`, `AuthViewModel`
- **DAOs**: `TransactionDao`, `UserDao`
- **Repositories**: `TransactionRepository`, `UserRepository`
- **Adapters**: `TransactionAdapter`, `CategoryAdapter`

#### Variables y Funciones
- **Variables privadas**: `_transactions`, `_isLoading`
- **Variables públicas**: `transactions`, `isLoading`
- **Funciones**: `loadTransactions()`, `addTransaction()`
- **Constantes**: `MAX_AMOUNT`, `DEFAULT_CATEGORY`

#### Archivos de Layout
- **Activities**: `activity_main.xml`
- **Fragments**: `fragment_home.xml`, `fragment_dashboard.xml`
- **Items**: `item_transaction.xml`, `item_category.xml`
- **Dialogs**: `dialog_add_transaction.xml`

### Estilo de Código

#### Kotlin
```kotlin
// Usar camelCase para variables y funciones
val transactionAmount: Double = 100.0
fun calculateBalance(): Double { ... }

// Usar PascalCase para clases
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() { ... }

// Usar UPPER_SNAKE_CASE para constantes
companion object {
    const val MAX_AMOUNT = 999999.99
    const val DEFAULT_CATEGORY = "Otros"
}
```

#### XML
```xml
<!-- Usar snake_case para IDs -->
<TextView
    android:id="@+id/transaction_amount"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />

<!-- Usar nombres descriptivos -->
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab_add_transaction"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

## Base de Datos

### Room Database

#### Configuración
```kotlin
@Database(
    entities = [
        User::class,
        Transaction::class,
        SavingsGoal::class,
        ScheduledPayment::class,
        CategoryLimit::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class FinanzasDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun scheduledPaymentDao(): ScheduledPaymentDao
    abstract fun categoryLimitDao(): CategoryLimitDao
}
```

#### Entidades
```kotlin
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val description: String?,
    val category: String,
    val type: String,
    @TypeConverters(DateConverter::class)
    val date: Date
)
```

#### DAOs
```kotlin
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY date DESC")
    suspend fun getTransactionsByUser(userId: Int): List<Transaction>
    
    @Insert
    suspend fun insert(transaction: Transaction): Long
    
    @Update
    suspend fun update(transaction: Transaction)
    
    @Delete
    suspend fun delete(transaction: Transaction)
}
```

### Migraciones

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Agregar nueva columna
        database.execSQL("ALTER TABLE transactions ADD COLUMN category TEXT NOT NULL DEFAULT 'Otros'")
    }
}
```

## Inyección de Dependencias (Hilt)

### Configuración del Módulo

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinanzasDatabase {
        return Room.databaseBuilder(
            context,
            FinanzasDatabase::class.java,
            "finanzas_database"
        ).build()
    }
    
    @Provides
    fun provideTransactionDao(database: FinanzasDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }
}
```

### Inyección en ViewModels

```kotlin
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    // Implementación
}
```

### Inyección en Fragments

```kotlin
@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private val viewModel: TransactionViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Usar viewModel
    }
}
```

## ViewModels

### Estructura Básica

```kotlin
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadTransactions(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getTransactionsByUser(userId)
                _transactions.value = result
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.addTransaction(transaction)
                loadTransactions(transaction.userId)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
```

### Manejo de Estados

```kotlin
sealed class TransactionState {
    object Loading : TransactionState()
    data class Success(val transactions: List<Transaction>) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

private val _state = MutableLiveData<TransactionState>()
val state: LiveData<TransactionState> = _state
```

## Repositorios

### Estructura Básica

```kotlin
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    
    suspend fun getTransactionsByUser(userId: Int): List<Transaction> {
        return withContext(Dispatchers.IO) {
            transactionDao.getTransactionsByUser(userId)
        }
    }
    
    suspend fun addTransaction(transaction: Transaction): Long {
        return withContext(Dispatchers.IO) {
            transactionDao.insert(transaction)
        }
    }
    
    suspend fun updateTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.update(transaction)
        }
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.delete(transaction)
        }
    }
}
```

## UI Components

### Fragments

```kotlin
@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TransactionViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            // Actualizar UI
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Mostrar/ocultar loading
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            // Navegar a agregar transacción
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### Adapters

```kotlin
class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val onItemLongClick: (Transaction) -> Boolean
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(transaction: Transaction) {
            binding.apply {
                transactionAmount.text = CurrencyUtils.formatCurrency(transaction.amount)
                transactionDescription.text = transaction.description
                transactionCategory.text = transaction.category
                transactionDate.text = DateConverter.formatDate(transaction.date)
                
                // Configurar colores según tipo
                val color = if (transaction.type == "INGRESO") {
                    ContextCompat.getColor(root.context, R.color.green)
                } else {
                    ContextCompat.getColor(root.context, R.color.red)
                }
                transactionAmount.setTextColor(color)
                
                root.setOnClickListener { onItemClick(transaction) }
                root.setOnLongClickListener { onItemLongClick(transaction) }
            }
        }
    }
}
```

## Navegación

### Navigation Component

```xml
<!-- nav_graph.xml -->
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/homeFragment">

    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.ahorrrapp.HomeFragment"
        android:label="Home" />

    <fragment
        android:id="@+id/dashboardFragment"
        android:name="com.example.ahorrrapp.DashboardFragment"
        android:label="Dashboard" />

    <action
        android:id="@+id/action_homeFragment_to_dashboardFragment"
        app:destination="@id/dashboardFragment" />

</navigation>
```

### Navegación en Código

```kotlin
// En Fragment
findNavController().navigate(R.id.action_homeFragment_to_dashboardFragment)

// Con argumentos
val action = HomeFragmentDirections.actionHomeFragmentToDashboardFragment(userId)
findNavController().navigate(action)
```

## Testing

### Pruebas Unitarias

```kotlin
@RunWith(MockitoJUnitRunner::class)
class TransactionViewModelTest {
    
    @Mock
    private lateinit var repository: TransactionRepository
    
    @Mock
    private lateinit var savedStateHandle: SavedStateHandle
    
    private lateinit var viewModel: TransactionViewModel
    
    @Before
    fun setup() {
        viewModel = TransactionViewModel(repository)
    }
    
    @Test
    fun `loadTransactions should update LiveData with transactions`() = runTest {
        // Given
        val userId = 1
        val transactions = listOf(
            Transaction(userId = userId, amount = 100.0, category = "Comida", type = "GASTO", date = Date())
        )
        whenever(repository.getTransactionsByUser(userId)).thenReturn(transactions)
        
        // When
        viewModel.loadTransactions(userId)
        
        // Then
        assertEquals(transactions, viewModel.transactions.value)
    }
}
```

### Pruebas de Integración

```kotlin
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    
    private lateinit var database: FinanzasDatabase
    private lateinit var transactionDao: TransactionDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinanzasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        transactionDao = database.transactionDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndReadTransaction() = runTest {
        // Given
        val transaction = Transaction(
            userId = 1,
            amount = 100.0,
            category = "Comida",
            type = "GASTO",
            date = Date()
        )
        
        // When
        val id = transactionDao.insert(transaction)
        val transactions = transactionDao.getTransactionsByUser(1)
        
        // Then
        assertEquals(1, transactions.size)
        assertEquals(transaction.amount, transactions[0].amount)
    }
}
```

## Logging y Debugging

### Configuración de Logs

```kotlin
companion object {
    private const val TAG = "TransactionViewModel"
}

fun loadTransactions(userId: Int) {
    Log.d(TAG, "Loading transactions for user: $userId")
    viewModelScope.launch {
        try {
            val result = repository.getTransactionsByUser(userId)
            Log.d(TAG, "Loaded ${result.size} transactions")
            _transactions.value = result
        } catch (e: Exception) {
            Log.e(TAG, "Error loading transactions", e)
        }
    }
}
```

### Debugging

```kotlin
// En desarrollo
if (BuildConfig.DEBUG) {
    Log.d(TAG, "Debug information: $data")
}

// Información de depuración
Log.d(TAG, "Transaction: amount=${transaction.amount}, category=${transaction.category}")
```

## Optimización de Rendimiento

### Lazy Loading

```kotlin
// Cargar datos solo cuando sea necesario
private var _isDataLoaded = false

fun loadDataIfNeeded() {
    if (!_isDataLoaded) {
        loadTransactions(userId)
        _isDataLoaded = true
    }
}
```

### Paginación

```kotlin
@Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY date DESC LIMIT :limit OFFSET :offset")
suspend fun getTransactionsByUserPaginated(userId: Int, limit: Int, offset: Int): List<Transaction>
```

### Caching

```kotlin
// Cache en memoria
private val _cachedTransactions = mutableMapOf<Int, List<Transaction>>()

fun getTransactionsByUser(userId: Int): List<Transaction> {
    return _cachedTransactions[userId] ?: emptyList()
}
```

## Seguridad

### Validación de Entrada

```kotlin
fun validateTransaction(transaction: Transaction): ValidationResult {
    return when {
        transaction.amount <= 0 -> ValidationResult.Error("El monto debe ser mayor a 0")
        transaction.description.isNullOrBlank() -> ValidationResult.Error("La descripción es requerida")
        transaction.category.isBlank() -> ValidationResult.Error("La categoría es requerida")
        else -> ValidationResult.Success
    }
}
```

### Sanitización de Datos

```kotlin
fun sanitizeInput(input: String): String {
    return input.trim().replace(Regex("[<>\"']"), "")
}
```

## Deployment

### Configuración de Build

```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.ahorrrapp"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }
    
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### ProGuard Rules

```proguard
# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
```

## Contribución

### Flujo de Trabajo

1. **Fork** el repositorio
2. **Crea** una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. **Desarrolla** tu feature siguiendo las convenciones
4. **Escribe** pruebas para tu código
5. **Commit** tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
6. **Push** a la rama (`git push origin feature/nueva-funcionalidad`)
7. **Crea** un Pull Request

### Checklist de Pull Request

- [ ] Código sigue las convenciones establecidas
- [ ] Pruebas unitarias pasan
- [ ] Pruebas de integración pasan
- [ ] Documentación actualizada
- [ ] No hay warnings de compilación
- [ ] Código revisado por el equipo

---

**Versión de la Guía**: 1.0  
**Última actualización**: Diciembre 2024 