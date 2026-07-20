package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mind_map_nodes")
data class MindMapNode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val text: String,
    val parentId: Int? = null,
    val xPos: Float = 0f,
    val yPos: Float = 0f
)
