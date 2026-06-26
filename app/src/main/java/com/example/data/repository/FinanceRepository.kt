package com.example.data.repository

import com.example.data.dao.CategoryBudgetDao
import com.example.data.dao.PlanningGoalDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.CategoryBudget
import com.example.data.model.PlanningGoal
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryBudgetDao: CategoryBudgetDao,
    private val planningGoalDao: PlanningGoalDao,
    private val userProfileDao: UserProfileDao
) {
    private val currentProfileId = MutableStateFlow(1)

    fun setCurrentProfileId(id: Int) {
        currentProfileId.value = id
    }
    
    fun getCurrentProfileId(): Int = currentProfileId.value

    val allProfiles: Flow<List<UserProfile>> = userProfileDao.getAllProfiles()

    val allTransactions: Flow<List<Transaction>> = currentProfileId.flatMapLatest { id ->
        transactionDao.getAllTransactions(id)
    }
    val allBudgets: Flow<List<CategoryBudget>> = currentProfileId.flatMapLatest { id ->
        categoryBudgetDao.getAllBudgets(id)
    }
    val allGoals: Flow<List<PlanningGoal>> = currentProfileId.flatMapLatest { id ->
        planningGoalDao.getAllGoals(id)
    }

    suspend fun insertProfile(profile: UserProfile): Int {
        return userProfileDao.insertProfile(profile).toInt()
    }

    suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: UserProfile) {
        userProfileDao.deleteProfile(profile)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.copy(profileId = currentProfileId.value))
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertBudget(budget: CategoryBudget) {
        categoryBudgetDao.insertBudget(budget.copy(profileId = currentProfileId.value))
    }

    suspend fun updateBudget(budget: CategoryBudget) {
        categoryBudgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: CategoryBudget) {
        categoryBudgetDao.deleteBudget(budget)
    }

    suspend fun insertGoal(goal: PlanningGoal) {
        planningGoalDao.insertGoal(goal.copy(profileId = currentProfileId.value))
    }

    suspend fun updateGoal(goal: PlanningGoal) {
        planningGoalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: PlanningGoal) {
        planningGoalDao.deleteGoal(goal)
    }

    suspend fun prepopulateDefaultBudgetsIfEmpty() {
        val pid = currentProfileId.value
        val budgets = categoryBudgetDao.getAllBudgets(pid).first()
        if (budgets.isEmpty()) {
            val defaultBudgets = listOf(
                CategoryBudget(profileId = pid, categoryName = "Comida", iconName = "Restaurant", colorHex = "#FF9800"),
                CategoryBudget(profileId = pid, categoryName = "Transporte", iconName = "DirectionsBus", colorHex = "#00BCD4"),
                CategoryBudget(profileId = pid, categoryName = "Servicios", iconName = "Receipt", colorHex = "#4CAF50"),
                CategoryBudget(profileId = pid, categoryName = "Entretenimiento", iconName = "SportsEsports", colorHex = "#E91E63"),
                CategoryBudget(profileId = pid, categoryName = "Vivienda", iconName = "Home", colorHex = "#9C27B0"),
                CategoryBudget(profileId = pid, categoryName = "Otros", iconName = "Category", colorHex = "#607D8B")
            )
            categoryBudgetDao.insertBudgets(defaultBudgets)
        }
    }
}
