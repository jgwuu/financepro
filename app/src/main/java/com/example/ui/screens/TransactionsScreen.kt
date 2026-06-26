package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.viewmodel.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    // Search and Filters
    var searchText by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<TransactionType?>(null) } // null = All
    var selectedFilterCategory by remember { mutableStateOf<String?>(null) }

    val filteredTransactions = remember(transactions, searchText, filterType, selectedFilterCategory) {
        transactions.filter { transaction ->
            val matchesSearch = transaction.description.contains(searchText, ignoreCase = true) ||
                    transaction.category.contains(searchText, ignoreCase = true) ||
                    transaction.notes.contains(searchText, ignoreCase = true)
            val matchesType = filterType == null || transaction.type == filterType
            val matchesCategory = selectedFilterCategory == null || transaction.category == selectedFilterCategory

            matchesSearch && matchesType && matchesCategory
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Title & Summary info
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Movimientos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Registra y gestiona todos tus movimientos financieros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search Bar & Filter Chips Row
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_bar"),
                placeholder = { Text("Buscar por descripción, nota...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Horizontal Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { filterType = null },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = filterType == TransactionType.EXPENSE,
                    onClick = { filterType = TransactionType.EXPENSE },
                    label = { Text("Gastos") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFC62828)
                        )
                    }
                )
                FilterChip(
                    selected = filterType == TransactionType.INCOME,
                    onClick = { filterType = TransactionType.INCOME },
                    label = { Text("Ingresos") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    }
                )
            }

            // List of Transactions
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No resultados",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No se encontraron transacciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchText.isNotEmpty() || filterType != null || selectedFilterCategory != null) {
                            Button(onClick = {
                                searchText = ""
                                filterType = null
                                selectedFilterCategory = null
                            }) {
                                Text("Limpiar Filtros")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { transaction ->
                        TransactionItemCard(
                            transaction = transaction,
                            budget = budgets.find { it.categoryName.equals(transaction.category, ignoreCase = true) },
                            onEditClick = { transactionToEdit = transaction },
                            onDeleteClick = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }

        // Add Transaction Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("add_transaction_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Movimiento")
        }

        // Add Dialog
        if (showAddDialog) {
            TransactionFormDialog(
                budgets = budgets,
                goals = goals,
                onDismiss = { showAddDialog = false },
                onSave = { amount, type, category, description, payment, date, notes ->
                    viewModel.addTransaction(amount, type, category, description, payment, date, notes)
                    showAddDialog = false
                },
                onSaveGoalFunds = { goal, amount, isAddition ->
                    val updated = goal.copy(
                        currentAmount = goal.currentAmount + (if (isAddition) amount else -amount)
                    )
                    viewModel.updateGoal(updated)
                    viewModel.addTransaction(
                        amount = amount,
                        type = if (isAddition) TransactionType.EXPENSE else TransactionType.INCOME,
                        category = "Ahorro: ${goal.title}",
                        description = if (isAddition) "Depósito a meta" else "Retiro de meta",
                        paymentMethod = "Efectivo",
                        date = System.currentTimeMillis(),
                        notes = ""
                    )
                    showAddDialog = false
                },
                onAddNewCategory = { name, limit, icon, color ->
                    viewModel.addBudget(name, limit, icon, color)
                }
            )
        }

        // Edit Dialog
        if (transactionToEdit != null) {
            TransactionFormDialog(
                transaction = transactionToEdit,
                budgets = budgets,
                goals = goals,
                onDismiss = { transactionToEdit = null },
                onSave = { amount, type, category, description, payment, date, notes ->
                    val updated = transactionToEdit!!.copy(
                        amount = amount,
                        type = type,
                        category = category,
                        description = description,
                        paymentMethod = payment,
                        date = date,
                        notes = notes
                    )
                    viewModel.updateTransaction(updated)
                    transactionToEdit = null
                },
                onSaveGoalFunds = { _, _, _ -> }, // Not used in edit mode
                onAddNewCategory = { name, limit, icon, color ->
                    viewModel.addBudget(name, limit, icon, color)
                }
            )
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: Transaction,
    budget: CategoryBudget?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val icon = getCategoryIcon(transaction.category)
    val color = budget?.colorHex?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.secondary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifEmpty { transaction.category },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${transaction.category} • ${transaction.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.notes.isNotEmpty()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Amount, Date and Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = (if (isExpense) "-" else "+") + formatCurrency(transaction.amount),
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatDate(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    transaction: Transaction? = null,
    budgets: List<CategoryBudget>,
    goals: List<com.example.data.model.PlanningGoal>? = null,
    onDismiss: () -> Unit,
    onSave: (Double, TransactionType, String, String, String, Long, String) -> Unit,
    onSaveGoalFunds: ((com.example.data.model.PlanningGoal, Double, Boolean) -> Unit)? = null,
    onAddNewCategory: (String, Double, String, String) -> Unit
) {
    val context = LocalContext.current

    var amountStr by remember { mutableStateOf(transaction?.let { formatInitialAmount(it.amount) } ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(transaction?.category ?: budgets.firstOrNull()?.categoryName ?: "Otros") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var paymentMethod by remember { mutableStateOf(transaction?.paymentMethod ?: "Efectivo") }
    var dateInMillis by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(transaction?.notes ?: "") }

    var linkToGoal by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<com.example.data.model.PlanningGoal?>(goals?.firstOrNull()) }

    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val paymentMethods = listOf("Efectivo", "Tarjeta Crédito", "Tarjeta Débito", "Transferencia", "Otro")

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = if (transaction == null) "Nuevo Movimiento" else "Editar Movimiento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Segmented control style for EXPENSE/INCOME
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExpense = type == TransactionType.EXPENSE
                    val isIncome = type == TransactionType.INCOME
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isExpense) MaterialTheme.colorScheme.error else Color.Transparent)
                            .clickable { type = TransactionType.EXPENSE }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gasto",
                            color = if (isExpense) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isIncome) Color(0xFF2E7D32) else Color.Transparent)
                            .clickable { type = TransactionType.INCOME }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingreso",
                            color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Amount and Date Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = formatInputAmount(it) },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        visualTransformation = com.example.ui.components.CurrencyVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    val calendar = Calendar.getInstance().apply { timeInMillis = dateInMillis }
                    OutlinedTextField(
                        value = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        trailingIcon = {
                            IconButton(onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        dateInMillis = selectedCal.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Elegir Fecha")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (goals != null && goals.isNotEmpty() && transaction == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { linkToGoal = !linkToGoal }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Vincular a Meta de Ahorro", style = MaterialTheme.typography.bodyMedium)
                        androidx.compose.material3.Switch(
                            checked = linkToGoal,
                            onCheckedChange = { linkToGoal = it }
                        )
                    }
                }

                if (linkToGoal && goals != null && transaction == null) {
                    // Goal Selection
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedGoal?.title ?: "Seleccionar Meta",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Meta de Ahorro") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            goals.forEach { goal ->
                                DropdownMenuItem(
                                    text = { Text(goal.title) },
                                    onClick = {
                                        selectedGoal = goal
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = if (type == TransactionType.EXPENSE) 
                            "Este monto se sumará a la meta seleccionada."
                        else 
                            "Este monto se restará de la meta seleccionada.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        placeholder = { Text("Ej: Supermercado...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Category and Payment Method Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoría") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    budgets.forEach { budget ->
                                        DropdownMenuItem(
                                            text = { Text(budget.categoryName) },
                                            onClick = {
                                                category = budget.categoryName
                                                expanded = false
                                            }
                                        )
                                    }
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Text("Nueva...", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            expanded = false
                                            showNewCategoryDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        // Payment Method Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = paymentMethod,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Método") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    paymentMethods.forEach { method ->
                                        DropdownMenuItem(
                                            text = { Text(method) },
                                            onClick = {
                                                paymentMethod = method
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val amount = cleanAmountForParsing(amountStr).toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                if (linkToGoal && transaction == null) {
                                    selectedGoal?.let {
                                        val isAddition = (type == TransactionType.EXPENSE)
                                        onSaveGoalFunds?.invoke(it, amount, isAddition)
                                    }
                                } else {
                                    onSave(amount, type, category, description, paymentMethod, dateInMillis, notes)
                                }
                            }
                        },
                        enabled = cleanAmountForParsing(amountStr).toDoubleOrNull() != null && (!linkToGoal || selectedGoal != null),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }

    // Nested Category Dialog for adding missing parameters easily
    if (showNewCategoryDialog) {
        CategoryFormDialog(
            onDismiss = { showNewCategoryDialog = false },
            onSave = { name, limit, icon, color ->
                onAddNewCategory(name, limit, icon, color)
                category = name
                showNewCategoryDialog = false
            }
        )
    }
}
