package com.example.data.dao

import androidx.room.*
import com.example.data.model.CategoryBudget
import com.example.data.model.PlanningGoal
import com.example.data.model.Transaction
import com.example.data.model.GoalTerm
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE profileId = :profileId ORDER BY date DESC")
    fun getAllTransactions(profileId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE profileId = :profileId AND date >= :start AND date <= :end ORDER BY date DESC")
    fun getTransactionsInTimeRange(profileId: Int, start: Long, end: Long): Flow<List<Transaction>>
}

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets WHERE profileId = :profileId ORDER BY categoryName ASC")
    fun getAllBudgets(profileId: Int): Flow<List<CategoryBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: CategoryBudget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<CategoryBudget>)

    @Update
    suspend fun updateBudget(budget: CategoryBudget)

    @Delete
    suspend fun deleteBudget(budget: CategoryBudget)

    @Query("SELECT * FROM category_budgets WHERE profileId = :profileId AND categoryName = :name LIMIT 1")
    suspend fun getBudgetByCategory(profileId: Int, name: String): CategoryBudget?
}

@Dao
interface PlanningGoalDao {
    @Query("SELECT * FROM planning_goals WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getAllGoals(profileId: Int): Flow<List<PlanningGoal>>

    @Query("SELECT * FROM planning_goals WHERE profileId = :profileId AND term = :term ORDER BY createdAt DESC")
    fun getGoalsByTerm(profileId: Int, term: GoalTerm): Flow<List<PlanningGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: PlanningGoal)

    @Update
    suspend fun updateGoal(goal: PlanningGoal)

    @Delete
    suspend fun deleteGoal(goal: PlanningGoal)
}
