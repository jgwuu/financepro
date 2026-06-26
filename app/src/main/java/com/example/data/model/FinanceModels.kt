package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

enum class TransactionType {
    EXPENSE, INCOME
}

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val avatarId: Int,
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val themeAccent: String = "PURPLE",
    val themeMode: String = "SYSTEM",
    val initialSavings: Double = 0.0
) : Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Long,
    val description: String,
    val paymentMethod: String,
    val notes: String = ""
) : Serializable

@Entity(tableName = "category_budgets")
data class CategoryBudget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val categoryName: String,
    val monthlyLimit: Double = 0.0,
    val iconName: String = "Category",
    val colorHex: String = "#2196F3"
) : Serializable

enum class GoalTerm {
    SHORT_TERM,   // Corto plazo (e.g., < 1 año)
    MEDIUM_TERM,  // Mediano plazo (e.g., 1-3 años)
    LONG_TERM     // Largo plazo (e.g., > 3 años)
}

enum class DepositFrequency(val displayName: String) {
    DAILY("Diario"),
    WEEKLY("Semanal"),
    BIWEEKLY("Quincenal"),
    MONTHLY("Mensual"),
    YEARLY("Anual")
}

enum class InterestType(val displayName: String) {
    SIMPLE("Simple"),
    COMPOUND("Compuesto")
}

@Entity(tableName = "planning_goals")
data class PlanningGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long,
    val term: GoalTerm,
    val depositFrequency: DepositFrequency = DepositFrequency.MONTHLY,
    val generatesInterest: Boolean = false,
    val interestRate: Double = 0.0,
    val interestType: InterestType = InterestType.COMPOUND,
    val interestFrequency: DepositFrequency = DepositFrequency.YEARLY,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
