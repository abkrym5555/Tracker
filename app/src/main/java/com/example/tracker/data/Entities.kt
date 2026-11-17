package com.example.tracker.data

import androidx.room3.ColumnInfo
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Entity

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("userId")
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // صاحب المصروف (id من جدول users)
    @ColumnInfo(defaultValue = "0") val userId: Int = 0,
    val categoryId: Int,
    val title: String,
    val amount: Double
)