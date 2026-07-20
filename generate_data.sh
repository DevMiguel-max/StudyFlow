#!/bin/bash

cat << 'ENT' > app/src/main/java/com/example/data/local/Subject.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val color: String = "",
    val icon: String = "",
    val professor: String = "",
    val description: String = "",
    val difficulty: Int = 1,
    val examDate: Long? = null,
    val weeklyHoursTarget: Int = 0,
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
ENT

cat << 'ENT' > app/src/main/java/com/example/data/local/Task.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "tasks", indices = [Index("subjectId")])
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val subjectId: Int = 0,
    val priority: Int = 1,
    val date: Long? = null,
    val time: String? = null,
    val estimatedMinutes: Int = 0,
    val suggestedMethod: String = "",
    val status: String = "pending",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
ENT

cat << 'ENT' > app/src/main/java/com/example/data/local/StudyFlowDao.kt
package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyFlowDao {
    @Query("SELECT 1")
    suspend fun ping(): Int

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY date ASC, priority DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId")
    fun getTasksForSubject(subjectId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("DELETE FROM tasks WHERE subjectId = :subjectId")
    suspend fun deleteTasksBySubject(subjectId: Int)
    
    @Query("UPDATE tasks SET subjectId = :newSubjectId WHERE subjectId = :oldSubjectId")
    suspend fun moveTasksToSubject(oldSubjectId: Int, newSubjectId: Int)
}
ENT

cat << 'ENT' > app/src/main/java/com/example/domain/repository/StudyFlowRepository.kt
package com.example.domain.repository

import com.example.data.local.Subject
import com.example.data.local.Task
import kotlinx.coroutines.flow.Flow

interface StudyFlowRepository {
    fun getSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(id: Int): Subject?
    suspend fun insertSubject(subject: Subject)
    suspend fun updateSubject(subject: Subject)
    suspend fun deleteSubject(subject: Subject)

    fun getTasks(): Flow<List<Task>>
    fun getTasksForSubject(subjectId: Int): Flow<List<Task>>
    suspend fun getTaskById(id: Int): Task?
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteTasksBySubject(subjectId: Int)
    suspend fun moveTasksToSubject(oldSubjectId: Int, newSubjectId: Int)
}
ENT

cat << 'ENT' > app/src/main/java/com/example/data/repository/StudyFlowRepositoryImpl.kt
package com.example.data.repository

import com.example.data.local.StudyFlowDao
import com.example.data.local.Subject
import com.example.data.local.Task
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.Flow

class StudyFlowRepositoryImpl(
    private val dao: StudyFlowDao
) : StudyFlowRepository {
    override fun getSubjects(): Flow<List<Subject>> = dao.getAllSubjects()
    override suspend fun getSubjectById(id: Int): Subject? = dao.getSubjectById(id)
    override suspend fun insertSubject(subject: Subject) = dao.insertSubject(subject)
    override suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)
    override suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    override fun getTasks(): Flow<List<Task>> = dao.getAllTasks()
    override fun getTasksForSubject(subjectId: Int): Flow<List<Task>> = dao.getTasksForSubject(subjectId)
    override suspend fun getTaskById(id: Int): Task? = dao.getTaskById(id)
    override suspend fun insertTask(task: Task) = dao.insertTask(task)
    override suspend fun updateTask(task: Task) = dao.updateTask(task)
    override suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    override suspend fun deleteTasksBySubject(subjectId: Int) = dao.deleteTasksBySubject(subjectId)
    override suspend fun moveTasksToSubject(oldSubjectId: Int, newSubjectId: Int) = dao.moveTasksToSubject(oldSubjectId, newSubjectId)
}
ENT

cat << 'ENT' > app/src/main/java/com/example/di/AppModule.kt
package com.example.di

import androidx.room.Room
import com.example.data.local.StudyFlowDatabase
import com.example.data.repository.StudyFlowRepositoryImpl
import com.example.domain.repository.StudyFlowRepository
import com.example.presentation.viewmodel.HomeViewModel
import com.example.presentation.viewmodel.SubjectViewModel
import com.example.presentation.viewmodel.TaskViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            StudyFlowDatabase::class.java,
            "studyflow.db"
        ).fallbackToDestructiveMigration().build()
    }
    
    single { get<StudyFlowDatabase>().studyFlowDao() }
    
    single<StudyFlowRepository> { StudyFlowRepositoryImpl(get()) }
    
    viewModel { HomeViewModel(get()) }
    viewModel { SubjectViewModel(get()) }
    viewModel { TaskViewModel(get()) }
}
ENT
