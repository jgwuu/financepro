package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.viewmodel.FinanceViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onProfileClick: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val totalGoalsSaved = remember(goals) { goals.sumOf { it.currentAmount } }
    var showAddTransactionDialog by remember { mutableStateOf(false) }

    val recentTransactions = remember(transactions) {
        transactions.take(4)
    }

    if (showAddTransactionDialog) {
        TransactionFormDialog(
            budgets = budgets,
            goals = goals,
            onDismiss = { showAddTransactionDialog = false },
            onSave = { amount, type, category, description, paymentMethod, date, notes ->
                viewModel.addTransaction(amount, type, category, description, paymentMethod, date, notes)
                showAddTransactionDialog = false
            },
            onSaveGoalFunds = { goal, amount, isAddition ->
                val updated = goal.copy(
                    currentAmount = goal.currentAmount + (if (isAddition) amount else -amount)
                )
                viewModel.updateGoal(updated)
                // Optionally add a transaction for this
                viewModel.addTransaction(
                    amount = amount,
                    type = if (isAddition) TransactionType.EXPENSE else TransactionType.INCOME,
                    category = "Ahorro: ${goal.title}",
                    description = if (isAddition) "Depósito a meta" else "Retiro de meta",
                    paymentMethod = "Efectivo",
                    date = System.currentTimeMillis(),
                    notes = ""
                )
                showAddTransactionDialog = false
            },
            onAddNewCategory = { name, limit, icon, color ->
                viewModel.addBudget(name, limit, icon, color)
            }
        )
    }

    // Calculate current month statistics
    val currentMonthTransactions = remember(transactions) {
        transactions.filter { isCurrentMonth(it.date) }
    }

    val totalIncome = remember(currentMonthTransactions) {
        currentMonthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }

    val totalExpenses = remember(currentMonthTransactions) {
        currentMonthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    val balance = totalIncome - totalExpenses

    // Expenses by category
    val expensesByCategory = remember(currentMonthTransactions) {
        currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    // Build chart slices mapped to colors from budgets
    val chartSlices = remember(expensesByCategory, budgets) {
        expensesByCategory.map { (category, sum) ->
            val budget = budgets.find { it.categoryName.equals(category, ignoreCase = true) }
            val color = budget?.colorHex?.let { parseHexColor(it) } ?: Color.Gray
            ChartSlice(category, sum, color)
        }.sortedByDescending { it.value }
    }

    // Alert for budgets near/over limit
    val budgetAlerts = remember(expensesByCategory, budgets) {
        budgets.mapNotNull { budget ->
            val expense = expensesByCategory[budget.categoryName] ?: 0.0
            val ratio = if (budget.monthlyLimit > 0) expense / budget.monthlyLimit else 0.0
            if (ratio >= 0.8) {
                Triple(budget, expense, ratio)
            } else {
                null
            }
        }.sortedByDescending { it.third }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTransactionDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                text = { Text("Nuevo Movimiento") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("dashboard_add_transaction_fab")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
        // Welcome and Header
        item {
            val userName by viewModel.userName.collectAsStateWithLifecycle()
            val userAvatarId by viewModel.userAvatarId.collectAsStateWithLifecycle()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userName.isNotBlank()) "¡Hola, $userName! 👋" else "¡Hola! 👋",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Mi Resumen Financiero",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    onClick = onProfileClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    modifier = Modifier.testTag("dashboard_profile_avatar")
                ) {
                    AvatarImage(
                        avatarId = userAvatarId,
                        size = 52.dp,
                        borderWidth = 2.dp,
                        borderColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Balance Mensual",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatCurrency(balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (balance >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFD32F2F)
                        )
                    }

                    // Piggy Bank / Savings Display inside the Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Ahorrado en Metas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = formatCurrency(totalGoalsSaved),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Income
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFE8F5E9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Ingresos",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Ingresos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formatCurrency(totalIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Expenses
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFFEBEE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Gastos",
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Gastos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formatCurrency(totalExpenses),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Financial Tips Section - MOVED UP
        item {
            val financialTips = listOf(
                "Regla 50/30/20: Destina el 50% de tus ingresos a necesidades básicas, el 30% a deseos o entretenimiento, y ahorra obligatoriamente el 20%.",
                "Evita Gastos Hormiga: Cafés diarios, suscripciones que no usas o snacks rápidos representan hasta un 15% de tus egresos mensuales.",
                "Fondo de Emergencias: Intenta acumular entre 3 y 6 meses de tus gastos corrientes en una cuenta segura y líquida.",
                "Págate a ti primero: Apenas recibas tu salario, transfiere tu meta de ahorro a una cuenta separada antes de empezar a gastar.",
                "Revisa tus Presupuestos: Si te excedes en comida, reajusta las metas en transportes u ocio para mantener el equilibrio del mes.",
                "Duplica el tiempo antes de comprar: Cuando desees comprar algo no esencial, espera 48 horas. Muchas veces el impulso desaparecerá.",
                "Invierte en Educación Financiera: El mejor rendimiento se obtiene invirtiendo en tu propia comprensión de los mercados y las finanzas.",
                "Compara antes de comprar: No te quedes con la primera opción, siempre busca y compara precios en diferentes tiendas o plataformas.",
                "Usa listas de compras: Ir al supermercado con una lista evita que compres cosas innecesarias por impulso.",
                "Cancela suscripciones sin uso: Revisa tus estados de cuenta y elimina pagos recurrentes de servicios que ya no utilizas.",
                "Define metas financieras claras: Establecer objetivos te motiva a ahorrar con un propósito bien definido.",
                "Diversifica tus ingresos: No dependas de una sola fuente; busca oportunidades para generar ingresos extra o pasivos.",
                "Cuidado con el uso de tarjetas de crédito: Son herramientas útiles, no dinero extra. Paga el saldo total cada mes.",
                "Aplica la regla de las 24 horas: Para gastos menores no planificados, espera 24 horas para evitar compras impulsivas.",
                "Negocia tus deudas: Si tienes deudas con intereses altos, intenta negociar con el banco para consolidarlas a una menor tasa.",
                "Automatiza tus ahorros: Configura tu cuenta para que un porcentaje de tus ingresos se transfiera automáticamente a tu ahorro.",
                "Reutiliza y repara: Antes de comprar algo nuevo, pregúntate si puedes reparar lo que tienes o darle un nuevo uso.",
                "Haz un presupuesto base cero: Asigna cada peso de tu ingreso a una categoría (gastos, ahorro, inversión) hasta que quede en cero.",
                "Revisa tus seguros: Asegúrate de tener la cobertura necesaria. Un buen seguro previene catástrofes financieras.",
                "Evita deudas para consumo: No pidas préstamos ni uses crédito para pagar vacaciones o ropa. El crédito es para invertir.",
                "Aprende a decir 'No': No te sientas obligado a salir o gastar si no se ajusta a tu presupuesto actual.",
                "Compra calidad, no cantidad: A veces lo barato sale caro. Invertir en artículos duraderos te ahorra dinero a largo plazo.",
                "Prioriza tus deudas: Utiliza el método de la bola de nieve o la avalancha para salir de deudas lo más rápido posible.",
                "Registra cada gasto: Llevar un control diario te hace más consciente de a dónde va tu dinero y te ayuda a ajustar hábitos.",
                "No prestes dinero que no estés dispuesto a perder: Prestar a familiares o amigos puede dañar relaciones y tus finanzas.",
                "Aléjate de las inversiones 'mágicas': Si una inversión promete altos rendimientos sin riesgo, probablemente sea un fraude.",
                "Aprovecha recompensas de tarjetas: Sácale provecho a los puntos o cashback, pero recuerda pagar siempre a tiempo.",
                "Compra fuera de temporada: Ropa de invierno en primavera o artículos de verano en otoño suelen ser mucho más económicos.",
                "Planifica tus comidas: Cocinar en casa y planificar menús semanales reduce drásticamente los gastos en alimentación.",
                "Revisa tu historial crediticio: Hazlo al menos una vez al año para detectar errores o prevenir fraudes a tu nombre.",
                "Celebra tus logros financieros: Cuando alcances una meta, date una pequeña y calculada recompensa para mantener la motivación."
            )
            
            val calendar = java.util.Calendar.getInstance()
            val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currentTipIndex = (dayOfMonth - 1).coerceIn(0, financialTips.size - 1)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Consejo del Día",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = financialTips[currentTipIndex],
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Collapsible Quick Currency Converter
        item {
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_converter_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Conversor Rápido de Divisas",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Colapsar" else "Expandir"
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var amountText by remember { mutableStateOf("100") }
                            var fromCur by remember { mutableStateOf("USD") }
                            var toCur by remember { mutableStateOf("EUR") }

                            val currencies = listOf("USD", "EUR", "MXN", "COP", "ARS", "GBP")
                            val rates = mapOf(
                                "USD" to 1.0,
                                "EUR" to 0.93,
                                "MXN" to 18.12,
                                "COP" to 4150.0,
                                "ARS" to 910.0,
                                "GBP" to 0.79
                            )

                            val doubleAmount = cleanAmountForParsing(amountText).toDoubleOrNull() ?: 0.0
                            val fromRate = rates[fromCur] ?: 1.0
                            val toRate = rates[toCur] ?: 1.0
                            val result = if (fromRate > 0) (doubleAmount / fromRate) * toRate else 0.0

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = formatInputAmount(it) },
                                label = { Text("Monto a convertir") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = com.example.ui.components.CurrencyVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // From Currency Selector
                                var showFromMenu by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(
                                        onClick = { showFromMenu = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("De: $fromCur")
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = showFromMenu,
                                        onDismissRequest = { showFromMenu = false }
                                    ) {
                                        currencies.forEach { cur ->
                                            DropdownMenuItem(
                                                text = { Text(cur) },
                                                onClick = {
                                                    fromCur = cur
                                                    showFromMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Swap button
                                IconButton(
                                    onClick = {
                                        val temp = fromCur
                                        fromCur = toCur
                                        toCur = temp
                                    }
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Intercambiar")
                                }

                                // To Currency Selector
                                var showToMenu by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(
                                        onClick = { showToMenu = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("A: $toCur")
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = showToMenu,
                                        onDismissRequest = { showToMenu = false }
                                    ) {
                                        currencies.forEach { cur ->
                                            DropdownMenuItem(
                                                text = { Text(cur) },
                                                onClick = {
                                                    toCur = cur
                                                    showToMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Result
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Resultado de la Conversión",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "$doubleAmount $fromCur = ${String.format(Locale.US, "%,.2f", result)} $toCur",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Charts Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distribución de Gastos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onNavigateToBudgets) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Ver presupuestos")
                        }
                    }

                    DonutChart(
                        slices = chartSlices,
                        totalText = formatCurrency(totalExpenses),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transacciones Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToTransactions) {
                    Text("Ver Todo")
                }
            }
        }

        // Recent Transactions List

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Sin transacciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No hay transacciones registradas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { transaction ->
                val isExpense = transaction.type == TransactionType.EXPENSE
                val icon = getCategoryIcon(transaction.category)
                val budget = budgets.find { it.categoryName.equals(transaction.category, ignoreCase = true) }
                val color = budget?.colorHex?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.primary

                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { /* Detail modal maybe */ },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = transaction.category,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = transaction.description.ifEmpty { transaction.category },
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    supportingContent = {
                        Column {
                            Text(
                                text = transaction.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (transaction.notes.isNotEmpty()) {
                                Text(
                                    text = transaction.notes,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = (if (isExpense) "-" else "+") + formatCurrency(transaction.amount),
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = formatDate(transaction.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            }
            }
        }
    }
}

private fun isCurrentMonth(timestamp: Long): Boolean {
    val cal = Calendar.getInstance()
    val transCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal.get(Calendar.YEAR) == transCal.get(Calendar.YEAR) &&
           cal.get(Calendar.MONTH) == transCal.get(Calendar.MONTH)
}
