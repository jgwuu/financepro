package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GoalTerm
import com.example.data.model.PlanningGoal
import com.example.ui.components.*
import com.example.ui.viewmodel.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<PlanningGoal?>(null) }
    var goalToAddFunds by remember { mutableStateOf<PlanningGoal?>(null) }
    var showAchievementsDialog by remember { mutableStateOf(false) }

    // Achievements calculation based strictly on active goals
    val achievements = remember(goals) {
        listOf(
            Triple("Ahorrador Iniciado", "Creaste tu primer objetivo de planificación.", goals.isNotEmpty()),
            Triple("Planificador Pro", "Creaste al menos 3 metas de ahorro o inversión.", goals.size >= 3),
            Triple("Gran Acumulador", "Tienes una meta ambiciosa de $1,000 o más.", goals.any { it.targetAmount >= 1000.0 }),
            Triple("Diversificador", "Tienes metas de diferentes plazos (corto/largo).", goals.map { it.term }.distinct().size >= 2),
            Triple("Meta Conquistada", "Completaste al menos una meta de ahorro al 100%.", goals.any { it.targetAmount > 0 && it.currentAmount >= it.targetAmount })
        )
    }
    val completedCount = achievements.count { it.third }

    // Tab control for Term classification
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Corto Plazo", "Mediano Plazo", "Largo Plazo")
    val terms = listOf(GoalTerm.SHORT_TERM, GoalTerm.MEDIUM_TERM, GoalTerm.LONG_TERM)

    val filteredGoals = remember(goals, selectedTab) {
        goals.filter { it.term == terms[selectedTab] }
    }

    val errorColor = MaterialTheme.colorScheme.error
    val warningColor = Color(0xFFED6C02)
    val alerts = remember(goals, errorColor, warningColor) {
        val currentMillis = System.currentTimeMillis()
        goals.mapNotNull { goal ->
            if (goal.currentAmount >= goal.targetAmount) return@mapNotNull null
            
            val daysRemaining = (goal.targetDate - currentMillis) / (1000L * 60 * 60 * 24)
            val totalDays = (goal.targetDate - goal.createdAt) / (1000L * 60 * 60 * 24)
            val elapsedDays = (currentMillis - goal.createdAt) / (1000L * 60 * 60 * 24)
            
            val expectedProgress = elapsedDays.toDouble() / totalDays.coerceAtLeast(1L).toDouble()
            val expectedAmount = goal.targetAmount * expectedProgress.coerceIn(0.0, 1.0)
            
            when {
                daysRemaining in 0..30 -> {
                    "¡La fecha límite para '${goal.title}' está cerca ($daysRemaining días) y aún falta ahorrar!" to errorColor
                }
                goal.currentAmount < (expectedAmount - (goal.targetAmount * 0.05)) -> {
                    "Estás un poco atrasado en '${goal.title}'. Deberías llevar ahorrado alrededor de ${formatCurrency(expectedAmount)}." to warningColor
                }
                else -> null
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Compact Achievements Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Planificación",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Establece y monitorea tus metas de ahorro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Small modern Star surface trigger
                Surface(
                    modifier = Modifier.clickable { showAchievementsDialog = true },
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$completedCount/5 Logros",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Alerts Section
            if (alerts.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alerts) { (message, color) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alerta",
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Term Tabs Selector
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goals list
            if (filteredGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Sin metas",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No tienes metas en esta sección",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when (selectedTab) {
                                0 -> "Las metas de corto plazo te ayudan a ahorrar para objetivos inmediatos (como regalos o viajes cortos)."
                                1 -> "Las metas de mediano plazo son ideales para planes de 1 a 3 años (como un computador nuevo o un viaje)."
                                else -> "Las metas de largo plazo son para sueños mayores de 3 años (comprar casa, auto o jubilación)."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Añadir Nueva Meta")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGoals, key = { it.id }) { goal ->
                        GoalItemCard(
                            goal = goal,
                            onEditClick = { goalToEdit = goal },
                            onDeleteClick = { viewModel.deleteGoal(goal) },
                            onAddFundsClick = { goalToAddFunds = goal }
                        )
                    }
                }
            }
        }

        // Add Goal FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("add_goal_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Meta")
        }

        // Add Goal Dialog
        if (showAddDialog) {
            GoalFormDialog(
                defaultTerm = terms[selectedTab],
                onDismiss = { showAddDialog = false },
                onSave = { title, description, target, current, date, freq, genInt, intRate, intType, intFreq ->
                    viewModel.addGoal(title, description, target, current, date, freq, genInt, intRate, intType, intFreq)
                    showAddDialog = false
                }
            )
        }

        // Edit Goal Dialog
        if (goalToEdit != null) {
            GoalFormDialog(
                goal = goalToEdit,
                onDismiss = { goalToEdit = null },
                onSave = { title, description, target, current, date, freq, genInt, intRate, intType, intFreq ->
                    val updated = goalToEdit!!.copy(
                        title = title,
                        description = description,
                        targetAmount = target,
                        currentAmount = current,
                        targetDate = date,
                        depositFrequency = freq,
                        generatesInterest = genInt,
                        interestRate = intRate,
                        interestType = intType,
                        interestFrequency = intFreq
                    )
                    // (We also want to re-classify term since date might have changed)
                    val monthsDiff = (date - System.currentTimeMillis()) / (1000L * 60 * 60 * 24 * 30.44)
                    val newTerm = when {
                        monthsDiff <= 12 -> GoalTerm.SHORT_TERM
                        monthsDiff <= 36 -> GoalTerm.MEDIUM_TERM
                        else -> GoalTerm.LONG_TERM
                    }
                    viewModel.updateGoal(updated.copy(term = newTerm))
                    goalToEdit = null
                }
            )
        }

        // Add Funds Dialog
        if (goalToAddFunds != null) {
            AddFundsDialog(
                goal = goalToAddFunds!!,
                onDismiss = { goalToAddFunds = null },
                onSave = { addedAmount ->
                    val updated = goalToAddFunds!!.copy(
                        currentAmount = goalToAddFunds!!.currentAmount + addedAmount
                    )
                    viewModel.updateGoal(updated)
                    // Optionally record it as a transaction to reflect savings if user desires?
                    // To keep it simple, we just update the goal amount.
                    goalToAddFunds = null
                }
            )
        }

        // Achievements Dialog
        if (showAchievementsDialog) {
            AlertDialog(
                onDismissRequest = { showAchievementsDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Logros de la Hucha", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¡Alcanza hitos financieros para desbloquear insignias y mejorar tu salud financiera!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider()

                        achievements.forEach { (title, desc, isUnlocked) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isUnlocked) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isUnlocked) 1f else 0.6f)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showAchievementsDialog = false }) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}

@Composable
fun GoalItemCard(
    goal: PlanningGoal,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddFundsClick: () -> Unit
) {
    val ratio = if (goal.targetAmount > 0) goal.currentAmount / goal.targetAmount else 0.0
    val percentage = (ratio * 100).coerceAtMost(100.0).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Goal Icon Indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (goal.description.isNotEmpty()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Edit & Delete
                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Savings progression numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ahorrado: ${formatCurrency(goal.currentAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Meta: ${formatCurrency(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { ratio.coerceAtMost(1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (ratio >= 1.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Límite: ${formatDate(goal.targetDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$percentage% completado",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (ratio >= 1.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            }

            // Save / Add funds action row
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddFundsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Registrar Ahorro / Fondos", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormDialog(
    goal: PlanningGoal? = null,
    defaultTerm: GoalTerm = GoalTerm.SHORT_TERM,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Long, com.example.data.model.DepositFrequency, Boolean, Double, com.example.data.model.InterestType, com.example.data.model.DepositFrequency) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(goal?.title ?: "") }
    var description by remember { mutableStateOf(goal?.description ?: "") }
    var targetAmountStr by remember { mutableStateOf(goal?.let { formatInitialAmount(it.targetAmount) } ?: "") }
    var currentAmountStr by remember { mutableStateOf(goal?.let { formatInitialAmount(it.currentAmount) } ?: "") }
    var targetDateInMillis by remember { mutableStateOf(goal?.targetDate ?: (System.currentTimeMillis() + 31536000000L)) }
    
    var depositFrequency by remember { mutableStateOf(goal?.depositFrequency ?: com.example.data.model.DepositFrequency.MONTHLY) }
    var generatesInterest by remember { mutableStateOf(goal?.generatesInterest ?: false) }
    var interestRateStr by remember { mutableStateOf(goal?.let { if(it.interestRate > 0) it.interestRate.toString() else "" } ?: "") }
    var interestType by remember { mutableStateOf(goal?.interestType ?: com.example.data.model.InterestType.COMPOUND) }
    var interestFrequency by remember { mutableStateOf(goal?.interestFrequency ?: com.example.data.model.DepositFrequency.YEARLY) }

    var showYearDropdown by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearsRange = remember { (currentYear..currentYear + 15).toList() }
    val selectedYear = remember(targetDateInMillis) {
        Calendar.getInstance().apply { timeInMillis = targetDateInMillis }.get(Calendar.YEAR)
    }
    
    var showDepositFreqDropdown by remember { mutableStateOf(false) }
    var showInterestFreqDropdown by remember { mutableStateOf(false) }
    var showInterestTypeDropdown by remember { mutableStateOf(false) }

    // Calcular cuotas sugeridas (monto mínimo a depositar por transacción)
    val suggestedDeposit = remember(targetAmountStr, currentAmountStr, targetDateInMillis, depositFrequency) {
        val target = cleanAmountForParsing(targetAmountStr).toDoubleOrNull() ?: 0.0
        val current = cleanAmountForParsing(currentAmountStr).toDoubleOrNull() ?: 0.0
        val remaining = (target - current).coerceAtLeast(0.0)
        
        val daysDiff = ((targetDateInMillis - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).coerceAtLeast(1)
        val frequencyInDays = when (depositFrequency) {
            com.example.data.model.DepositFrequency.DAILY -> 1.0
            com.example.data.model.DepositFrequency.WEEKLY -> 7.0
            com.example.data.model.DepositFrequency.BIWEEKLY -> 15.0
            com.example.data.model.DepositFrequency.MONTHLY -> 30.44
            com.example.data.model.DepositFrequency.YEARLY -> 365.25
        }
        val periods = (daysDiff / frequencyInDays).coerceAtLeast(1.0)
        remaining / periods
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (goal == null) "Nueva Meta de Ahorro" else "Editar Meta",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nombre de la Meta") },
                        placeholder = { Text("Ej: Fondo de Emergencias") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        placeholder = { Text("Ej: Dinero para emergencias") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Amounts
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetAmountStr,
                            onValueChange = { targetAmountStr = formatInputAmount(it) },
                            label = { Text("Monto Objetivo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            visualTransformation = com.example.ui.components.CurrencyVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = currentAmountStr,
                            onValueChange = { currentAmountStr = formatInputAmount(it) },
                            label = { Text("Ahorrado Actual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            visualTransformation = com.example.ui.components.CurrencyVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Deposit Frequency
                item {
                    Box {
                        OutlinedTextField(
                            value = depositFrequency.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frecuencia de Ahorro") },
                            trailingIcon = {
                                IconButton(onClick = { showDepositFreqDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir Frecuencia")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDepositFreqDropdown = true },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = showDepositFreqDropdown,
                            onDismissRequest = { showDepositFreqDropdown = false }
                        ) {
                            com.example.data.model.DepositFrequency.values().forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq.displayName) },
                                    onClick = {
                                        depositFrequency = freq
                                        showDepositFreqDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Date
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1.1f)) {
                            OutlinedTextField(
                                value = selectedYear.toString(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Año Límite") },
                                trailingIcon = {
                                    IconButton(onClick = { showYearDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { showYearDropdown = true },
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false }
                            ) {
                                yearsRange.forEach { yearVal ->
                                    DropdownMenuItem(
                                        text = { Text(yearVal.toString()) },
                                        onClick = {
                                            val cal = Calendar.getInstance().apply { timeInMillis = targetDateInMillis }
                                            cal.set(Calendar.YEAR, yearVal)
                                            targetDateInMillis = cal.timeInMillis
                                            showYearDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        val calendar = Calendar.getInstance().apply { timeInMillis = targetDateInMillis }
                        OutlinedTextField(
                            value = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Día y Mes") },
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
                                            targetDateInMillis = selectedCal.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Elegir Fecha Detallada")
                                }
                            },
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
                
                // Suggested Deposit Info
                if (suggestedDeposit > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Debes ahorrar ${formatCurrency(suggestedDeposit)} de manera ${depositFrequency.displayName.lowercase()} para llegar a tu meta a tiempo.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Interests Switch
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("¿Este ahorro generará intereses?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = generatesInterest,
                            onCheckedChange = { generatesInterest = it }
                        )
                    }
                }
                
                if (generatesInterest) {
                    item {
                        OutlinedTextField(
                            value = interestRateStr,
                            onValueChange = { interestRateStr = it },
                            label = { Text("Tasa de Interés (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = interestFrequency.displayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Periodicidad") },
                                    trailingIcon = {
                                        IconButton(onClick = { showInterestFreqDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { showInterestFreqDropdown = true },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                DropdownMenu(
                                    expanded = showInterestFreqDropdown,
                                    onDismissRequest = { showInterestFreqDropdown = false }
                                ) {
                                    com.example.data.model.DepositFrequency.values().forEach { freq ->
                                        DropdownMenuItem(
                                            text = { Text(freq.displayName) },
                                            onClick = {
                                                interestFrequency = freq
                                                showInterestFreqDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = interestType.displayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo Interés") },
                                    trailingIcon = {
                                        IconButton(onClick = { showInterestTypeDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { showInterestTypeDropdown = true },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                DropdownMenu(
                                    expanded = showInterestTypeDropdown,
                                    onDismissRequest = { showInterestTypeDropdown = false }
                                ) {
                                    com.example.data.model.InterestType.values().forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.displayName) },
                                            onClick = {
                                                interestType = type
                                                showInterestTypeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = cleanAmountForParsing(targetAmountStr).toDoubleOrNull() ?: 0.0
                    val current = cleanAmountForParsing(currentAmountStr).toDoubleOrNull() ?: 0.0
                    val rate = interestRateStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && target > 0) {
                        onSave(title, description, target, current, targetDateInMillis, depositFrequency, generatesInterest, rate, interestType, interestFrequency)
                    }
                },
                enabled = title.isNotEmpty() && cleanAmountForParsing(targetAmountStr).toDoubleOrNull() != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddFundsDialog(
    goal: PlanningGoal,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var fundsStr by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) } // true = Agregar (+), false = Quitar (-)

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
                Text(
                    text = "Gestionar Fondos: ${goal.title}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Ajusta los fondos ahorrados para esta meta. Elige si deseas agregar saldo extra o retirar fondos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Segmented Toggle for Add vs Remove
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAddition) Color(0xFF2E7D32) else Color.Transparent)
                            .clickable { isAddition = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Agregar",
                            color = if (isAddition) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isAddition) MaterialTheme.colorScheme.error else Color.Transparent)
                            .clickable { isAddition = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "- Retirar",
                            color = if (!isAddition) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Amount Textfield
                OutlinedTextField(
                    value = fundsStr,
                    onValueChange = { fundsStr = formatInputAmount(it) },
                    label = { Text("Monto a " + (if (isAddition) "Agregar ($)" else "Retirar ($)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    visualTransformation = com.example.ui.components.CurrencyVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Computed new balance message
                val enteredAmount = cleanAmountForParsing(fundsStr).toDoubleOrNull() ?: 0.0
                val originalAmount = goal.currentAmount
                val targetAmount = goal.targetAmount
                val projectedAmount = if (isAddition) (originalAmount + enteredAmount) else (originalAmount - enteredAmount).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ahorro Actual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatCurrency(originalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Nuevo Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formatCurrency(projectedAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (projectedAmount >= targetAmount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
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
                            val funds = cleanAmountForParsing(fundsStr).toDoubleOrNull() ?: 0.0
                            if (funds > 0) {
                                onSave(if (isAddition) funds else -funds)
                            }
                        },
                        enabled = (cleanAmountForParsing(fundsStr).toDoubleOrNull() ?: 0.0) > 0.0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAddition) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}
