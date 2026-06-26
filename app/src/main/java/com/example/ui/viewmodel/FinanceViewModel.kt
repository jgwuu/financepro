package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CategoryBudget
import com.example.data.model.GoalTerm
import com.example.data.model.PlanningGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)

    private val _currentProfileId = MutableStateFlow(prefs.getInt("current_profile_id", 0))

    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProfile: StateFlow<UserProfile?> = combine(allProfiles, _currentProfileId) { profiles, id ->
        profiles.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName = currentProfile.map { it?.name ?: "" }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userAvatarId = currentProfile.map { it?.avatarId ?: 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val userCurrencyCode = currentProfile.map { it?.currencyCode ?: "USD" }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")
    val userCurrencySymbol = currentProfile.map { it?.currencySymbol ?: "$" }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")
    val themeAccent = currentProfile.map { it?.themeAccent ?: "PURPLE" }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "PURPLE")
    val themeMode = currentProfile.map { it?.themeMode ?: "SYSTEM" }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val isOnboarded = _currentProfileId.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            _currentProfileId.collect { id ->
                repository.setCurrentProfileId(id)
                if (id > 0) {
                    val key = "has_prepopulated_defaults_for_$id"
                    if (!prefs.getBoolean(key, false)) {
                        repository.prepopulateDefaultBudgetsIfEmpty()
                        prefs.edit().putBoolean(key, true).apply()
                    }
                }
            }
        }
    }

    fun switchProfile(profileId: Int) {
        prefs.edit().putInt("current_profile_id", profileId).apply()
        _currentProfileId.value = profileId
    }

    fun saveProfile(name: String, avatarId: Int, currencyCode: String = "USD", currencySymbol: String = "$", initialSavings: Double = 0.0) {
        viewModelScope.launch {
            val newProfile = UserProfile(
                name = name,
                avatarId = avatarId,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                initialSavings = initialSavings
            )
            val insertedId = repository.insertProfile(newProfile)
            switchProfile(insertedId)
        }
    }

    fun updateCurrentProfile(name: String, avatarId: Int) {
        viewModelScope.launch {
            currentProfile.value?.let { p ->
                repository.updateProfile(p.copy(name = name, avatarId = avatarId))
            }
        }
    }

    fun createNewProfile(name: String, avatarId: Int, currencyCode: String = "USD", currencySymbol: String = "$") {
        viewModelScope.launch {
            val newProfile = UserProfile(
                name = name,
                avatarId = avatarId,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol
            )
            val insertedId = repository.insertProfile(newProfile)
            switchProfile(insertedId)
        }
    }

    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            currentProfile.value?.let { p ->
                repository.updateProfile(p.copy(currencyCode = code, currencySymbol = symbol))
            }
        }
    }

    fun updateThemeAccent(accent: String) {
        viewModelScope.launch {
            currentProfile.value?.let { p ->
                repository.updateProfile(p.copy(themeAccent = accent))
            }
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            currentProfile.value?.let { p ->
                repository.updateProfile(p.copy(themeMode = mode))
            }
        }
    }

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<CategoryBudget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<PlanningGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operations for Transactions
    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        description: String,
        paymentMethod: String,
        date: Long,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    description = description,
                    paymentMethod = paymentMethod,
                    date = date,
                    notes = notes
                )
            )
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Operations for Budgets
    fun addBudget(categoryName: String, monthlyLimit: Double, iconName: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertBudget(
                CategoryBudget(
                    categoryName = categoryName,
                    monthlyLimit = monthlyLimit,
                    iconName = iconName,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateBudget(budget: CategoryBudget) {
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: CategoryBudget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Operations for Goals
    fun addGoal(
        title: String,
        description: String,
        targetAmount: Double,
        currentAmount: Double,
        targetDate: Long,
        depositFrequency: com.example.data.model.DepositFrequency,
        generatesInterest: Boolean,
        interestRate: Double,
        interestType: com.example.data.model.InterestType,
        interestFrequency: com.example.data.model.DepositFrequency
    ) {
        viewModelScope.launch {
            val monthsDiff = (targetDate - System.currentTimeMillis()) / (1000L * 60 * 60 * 24 * 30.44)
            val term = when {
                monthsDiff <= 12 -> GoalTerm.SHORT_TERM
                monthsDiff <= 36 -> GoalTerm.MEDIUM_TERM
                else -> GoalTerm.LONG_TERM
            }
            
            repository.insertGoal(
                PlanningGoal(
                    title = title,
                    description = description,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDate = targetDate,
                    term = term,
                    depositFrequency = depositFrequency,
                    generatesInterest = generatesInterest,
                    interestRate = interestRate,
                    interestType = interestType,
                    interestFrequency = interestFrequency
                )
            )
        }
    }

    fun updateGoal(goal: PlanningGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: PlanningGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }
}

class FinanceViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = FinanceRepository(
                database.transactionDao(),
                database.categoryBudgetDao(),
                database.planningGoalDao(),
                database.userProfileDao()
            )
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
