package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AvatarImage
import com.example.ui.components.AvatarOptions
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatInputAmount
import com.example.ui.components.cleanAmountForParsing
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var nameInput by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableIntStateOf(0) }
    var selectedCurrencyCode by remember { mutableStateOf("USD") }
    var selectedCurrencySymbol by remember { mutableStateOf("$") }
    var initialSavingsInput by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Icon / Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = "Wallet Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = "¡Bienvenido a Finanzas!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Tu app de finanzas personales. Introduce tus datos de una sola vez para empezar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Text input card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. ¿Cómo te llamas?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("Escribe tu nombre o apodo...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // Avatar selection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. Elige tu foto de perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Display list of 10 avatars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AvatarImage(
                            avatarId = selectedAvatarId,
                            size = 80.dp,
                            borderWidth = 3.dp,
                            borderColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Grid of choices
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val chunked = AvatarOptions.chunked(5)
                        chunked.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowItems.forEach { avatar ->
                                    val isSelected = selectedAvatarId == avatar.id
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                            .clickable { selectedAvatarId = avatar.id }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AvatarImage(
                                            avatarId = avatar.id,
                                            size = 46.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Currency selection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Elige tu moneda de uso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Esta moneda se usará para mostrar todos tus balances, gastos y metas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val currencyList = listOf(
                        Triple("USD", "$", "Dólar ($)"),
                        Triple("EUR", "€", "Euro (€)"),
                        Triple("MXN", "$", "Peso Mexicano ($)"),
                        Triple("COP", "$", "Peso Colombiano ($)"),
                        Triple("ARS", "$", "Peso Argentino ($)"),
                        Triple("GBP", "£", "Libra (£)")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val chunkedCurrencies = currencyList.chunked(3)
                        chunkedCurrencies.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { (code, symbol, label) ->
                                    val isSelected = selectedCurrencyCode == code
                                    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)

                                    OutlinedCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedCurrencyCode = code
                                                selectedCurrencySymbol = symbol
                                            },
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = containerColor,
                                            contentColor = contentColor
                                        ),
                                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = symbol,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = code,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Initial Savings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "4. ¿Tienes ahorros actuales? (Opcional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Introduce una cantidad si quieres empezar con dinero guardado en tu hucha/alcancía.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = initialSavingsInput,
                        onValueChange = { initialSavingsInput = formatInputAmount(it) },
                        placeholder = { Text("Ejemplo: 500") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = com.example.ui.components.CurrencyVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_savings_input"),
                        leadingIcon = {
                            Text(
                                text = selectedCurrencySymbol,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        val initialSavings = cleanAmountForParsing(initialSavingsInput).toDoubleOrNull() ?: 0.0
                        viewModel.saveProfile(
                            name = nameInput.trim(),
                            avatarId = selectedAvatarId,
                            currencyCode = selectedCurrencyCode,
                            currencySymbol = selectedCurrencySymbol,
                            initialSavings = initialSavings
                        )
                    }
                },
                enabled = nameInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_save_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Comenzar mi aventura financiera",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val currentName by viewModel.userName.collectAsStateWithLifecycle()
    val currentAvatarId by viewModel.userAvatarId.collectAsStateWithLifecycle()
    val currentTheme by viewModel.themeAccent.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currentCurrencyCode by viewModel.userCurrencyCode.collectAsStateWithLifecycle()
    val currentCurrencySymbol by viewModel.userCurrencySymbol.collectAsStateWithLifecycle()

    var tempName by remember { mutableStateOf(currentName) }
    var selectedAvatarId by remember { mutableIntStateOf(currentAvatarId) }

    // Update temp states when profile changes
    LaunchedEffect(currentProfile) {
        tempName = currentName
        selectedAvatarId = currentAvatarId
    }

    var showNewProfileDialog by remember { mutableStateOf(false) }

    // Currency Converter State
    var converterAmount by remember { mutableStateOf("100") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    val currencies = listOf("USD", "EUR", "MXN", "COP", "ARS")
    val rates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.93,
        "MXN" to 18.12,
        "COP" to 4150.0,
        "ARS" to 910.0
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header TopBar
            TopAppBar(
                title = {
                    Text(
                        "Configuración y Utilidades",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // 1. PROFILES MANAGEMENT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
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
                                text = "Mis Perfiles Financieros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { showNewProfileDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Nuevo Perfil")
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allProfiles, key = { it.id }) { profile ->
                                val isSelected = currentProfile?.id == profile.id
                                Card(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable { viewModel.switchProfile(profile.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AvatarImage(
                                            avatarId = profile.avatarId,
                                            size = 48.dp,
                                            borderWidth = if (isSelected) 2.dp else 0.dp,
                                            borderColor = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = profile.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. PROFILE SETTINGS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Editar Perfil Actual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AvatarImage(
                                avatarId = selectedAvatarId,
                                size = 64.dp,
                                borderWidth = 2.dp,
                                borderColor = MaterialTheme.colorScheme.primary
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    label = { Text("Nombre de usuario") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Avatar selector inside settings
                        Text(
                            text = "Cambiar avatar:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AvatarOptions) { avatar ->
                                val isSelected = selectedAvatarId == avatar.id
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedAvatarId = avatar.id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarImage(
                                        avatarId = avatar.id,
                                        size = 38.dp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (tempName.isNotBlank()) {
                                    if (currentProfile != null) {
                                        viewModel.updateCurrentProfile(tempName.trim(), selectedAvatarId)
                                        Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.createNewProfile(tempName.trim(), selectedAvatarId, currentCurrencyCode, currentCurrencySymbol)
                                        Toast.makeText(context, "Perfil creado con éxito", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = tempName.isNotBlank() && (tempName != currentName || selectedAvatarId != currentAvatarId),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar Cambios")
                        }
                    }
                }

                if (showNewProfileDialog) {
                    var newProfileName by remember { mutableStateOf("") }
                    var newProfileAvatarId by remember { mutableIntStateOf(0) }
                    AlertDialog(
                        onDismissRequest = { showNewProfileDialog = false },
                        title = { Text("Nuevo Perfil Financiero") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = newProfileName,
                                    onValueChange = { newProfileName = it },
                                    label = { Text("Nombre del Perfil") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Elige un Avatar:")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(AvatarOptions) { avatar ->
                                        val isSelected = newProfileAvatarId == avatar.id
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f),
                                                    shape = CircleShape
                                                )
                                                .clickable { newProfileAvatarId = avatar.id },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AvatarImage(avatarId = avatar.id, size = 38.dp)
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newProfileName.isNotBlank()) {
                                        viewModel.createNewProfile(newProfileName.trim(), newProfileAvatarId)
                                        showNewProfileDialog = false
                                        Toast.makeText(context, "Perfil creado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Crear")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNewProfileDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // 2. PERSONALIZATION: THEME ACCENT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Personalización del Tema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Elige el color acento que más te inspire hoy:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val themeOptions = listOf(
                            Triple("PURPLE", Color(0xFF6650A4), "Amatista"),
                            Triple("GREEN", Color(0xFF2E7D32), "Esmeralda"),
                            Triple("ORANGE", Color(0xFFE65100), "Coral"),
                            Triple("BLUE", Color(0xFF1565C0), "Zafiro"),
                            Triple("TEAL", Color(0xFF00695C), "Menta"),
                            Triple("ROSE", Color(0xFFC2185B), "Rosa"),
                            Triple("GOLD", Color(0xFFFFB300), "Oro"),
                            Triple("INDIGO", Color(0xFF283593), "Índigo"),
                            Triple("CRIMSON", Color(0xFFC62828), "Carmesí"),
                            Triple("CYAN", Color(0xFF00838F), "Cian"),
                            Triple("SLATE", Color(0xFF37474F), "Pizarra")
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            themeOptions.forEach { (accent, color, label) ->
                                val isSelected = currentTheme == accent
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateThemeAccent(accent) },
                                    label = { Text(label) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()

                        Text(
                            text = "Modo de Apariencia",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("LIGHT", Icons.Default.WbSunny, "Claro"),
                                Triple("DARK", Icons.Default.NightsStay, "Oscuro"),
                                Triple("SYSTEM", Icons.Default.Settings, "Sistema")
                            ).forEach { (mode, icon, label) ->
                                val isSelected = currentThemeMode == mode
                                ElevatedFilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateThemeMode(mode) },
                                    label = { Text(label) },
                                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 2.1 Moneda de la Aplicación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Moneda de la Aplicación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Selecciona la divisa por defecto que se utilizará en toda la aplicación:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val currenciesAvailable = listOf(
                            Triple("USD", "$", "USD ($)"),
                            Triple("EUR", "€", "EUR (€)"),
                            Triple("MXN", "$", "MXN ($)"),
                            Triple("COP", "$", "COP ($)"),
                            Triple("ARS", "$", "ARS ($)"),
                            Triple("GBP", "£", "GBP (£)")
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            currenciesAvailable.forEach { (code, symbol, label) ->
                                val isSelected = currentCurrencyCode == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateCurrency(code, symbol) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                // 3. HANDY FEATURE A: CURRENCY CONVERTER
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Convertidor de Divisas Rápido",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Ideal para calcular precios de productos del extranjero o equivalencias.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = converterAmount,
                                onValueChange = { converterAmount = formatInputAmount(it) },
                                label = { Text("Monto") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = com.example.ui.components.CurrencyVisualTransformation(),
                                modifier = Modifier.weight(1.5f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // From Select
                            var fromExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { fromExpanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(fromCurrency)
                                }
                                DropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                                    currencies.forEach { curr ->
                                        DropdownMenuItem(
                                            text = { Text(curr) },
                                            onClick = {
                                                fromCurrency = curr
                                                fromExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))

                            // To Select
                            var toExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { toExpanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(toCurrency)
                                }
                                DropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                                    currencies.forEach { curr ->
                                        DropdownMenuItem(
                                            text = { Text(curr) },
                                            onClick = {
                                                toCurrency = curr
                                                toExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Conversion Result Calculation
                        val convertedValue = remember(converterAmount, fromCurrency, toCurrency) {
                            val amount = cleanAmountForParsing(converterAmount).toDoubleOrNull() ?: 0.0
                            val rateFrom = rates[fromCurrency] ?: 1.0
                            val rateTo = rates[toCurrency] ?: 1.0
                            // Convert to USD base first, then to target
                            val inUSD = amount / rateFrom
                            inUSD * rateTo
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Resultado: ${String.format("%.2f", convertedValue)} $toCurrency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 6. HANDY FEATURE D: EXPORT DATA (REAL CSV EXPORT & SHARE)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Exportar mis Datos Financieros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Crea un archivo de datos (CSV) con todos tus movimientos actuales para guardarlo en tu móvil o enviarlo por correo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                if (transactions.isEmpty()) {
                                    Toast.makeText(context, "No tienes transacciones para exportar.", Toast.LENGTH_SHORT).show()
                                } else {
                                    try {
                                        // Generate CSV content
                                        val csvBuilder = StringBuilder()
                                        csvBuilder.append("ID,Monto,Tipo,Categoria,Descripcion,Fecha,Metodo de Pago\n")
                                        transactions.forEach { trans ->
                                            csvBuilder.append("${trans.id},${trans.amount},${trans.type.name},\"${trans.category}\",\"${trans.description}\",${trans.date},\"${trans.paymentMethod}\"\n")
                                        }

                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, csvBuilder.toString())
                                            putExtra(Intent.EXTRA_SUBJECT, "Finanzas_Export_${System.currentTimeMillis()}.csv")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Guardar o compartir CSV")
                                        context.startActivity(shareIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al compartir datos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generar y Compartir CSV")
                        }
                    }
                }

                // 7. BUG AND IMPROVEMENTS REPORTING (GITHUB SYNC)
                var showReportDialog by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Soporte y Reportes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Ayúdanos a mejorar. Envía sugerencias, ideas o reporta errores que hayas encontrado directamente a nuestro sistema en GitHub.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { showReportDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crear un Reporte")
                        }
                    }
                }

                if (showReportDialog) {
                    var reportType by remember { mutableStateOf("Bug") }
                    var reportCategory by remember { mutableStateOf("General/Ninguna") }
                    var showCategoryDropdown by remember { mutableStateOf(false) }
                    val categories = listOf("General/Ninguna", "Presupuestos", "Planificación", "Transacciones", "Dashboard", "Ajustes", "Otro")
                    var reportTitle by remember { mutableStateOf("") }
                    var reportDescription by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { showReportDialog = false },
                        title = { Text("Nuevo Reporte", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Selecciona el tipo de reporte:", style = MaterialTheme.typography.bodyMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Bug", "Mejora", "Idea").forEach { type ->
                                        FilterChip(
                                            selected = reportType == type,
                                            onClick = { reportType = type },
                                            label = { Text(type) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                
                                Box {
                                    OutlinedTextField(
                                        value = reportCategory,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Categoría (Opcional)") },
                                        trailingIcon = {
                                            IconButton(onClick = { showCategoryDropdown = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir Categoría")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { showCategoryDropdown = true },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    DropdownMenu(
                                        expanded = showCategoryDropdown,
                                        onDismissRequest = { showCategoryDropdown = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    reportCategory = cat
                                                    showCategoryDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = reportTitle,
                                    onValueChange = { reportTitle = it },
                                    label = { Text("Título breve") },
                                    placeholder = { Text("Ej: Error al agregar meta") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = reportDescription,
                                    onValueChange = { reportDescription = it },
                                    label = { Text("Descripción detallada") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (reportTitle.isNotBlank()) {
                                        val categoryTag = if (reportCategory == "General/Ninguna") "" else " [$reportCategory]"
                                        val fullTitle = "[$reportType]$categoryTag ${reportTitle.trim()}"
                                        val url = "https://github.com/jgwuu/financepro/issues/new?title=${android.net.Uri.encode(fullTitle)}&body=${android.net.Uri.encode(reportDescription)}"
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                        showReportDialog = false
                                    }
                                },
                                enabled = reportTitle.isNotBlank()
                            ) {
                                Text("Enviar a GitHub")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showReportDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
