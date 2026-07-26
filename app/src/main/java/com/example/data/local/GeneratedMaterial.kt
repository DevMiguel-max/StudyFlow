package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_materials")
data class GeneratedMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val title: String,
    val content: String,
    val source: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
