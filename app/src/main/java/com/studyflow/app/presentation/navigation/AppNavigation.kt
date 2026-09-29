package com.studyflow.app.presentation.navigation

import androidx.compose.animation.*
import com.studyflow.app.presentation.screens.*

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studyflow.app.presentation.components.AppBottomBar

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute?.startsWith("home") == true || 
                        currentRoute?.startsWith("subjects") == true ||
                        currentRoute?.startsWith("more") == true ||
                        currentRoute?.startsWith("calendar") == true ||
                        currentRoute?.startsWith("study_methods") == true ||
                        currentRoute?.startsWith("profile") == true || currentRoute?.startsWith("statistics") == true

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AppBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f) }
        ) {
            composable("onboarding") { OnboardingScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
            composable("recover_password") { RecoverPasswordScreen(navController) }

            composable("home") { HomeScreen(navController) }
            composable("more") { MoreScreen(navController) }
            composable("mentor") { MentorScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("ai_tutor") { AITutorScreen(navController) }
            composable("subjects") { SubjectsScreen(navController) }
            
            composable(
                route = "subject_form?subjectId={subjectId}",
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId")
                SubjectFormScreen(navController, subjectId = if (subjectId == -1) null else subjectId)
            }
            
            composable("tasks") { TasksScreen(navController) }
            
            composable(
                route = "task_form?taskId={taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getInt("taskId")
                TaskFormScreen(navController, taskId = if (taskId == -1) null else taskId)
            }
            
            composable("study_plan_form") { StudyPlanFormScreen(navController) }
            
            composable("calendar") { CalendarScreen(navController) }
            
            composable("study_methods") { StudyMethodsScreen(navController) }
            
            composable(
                route = "study_method_detail/{methodId}",
                arguments = listOf(navArgument("methodId") { type = NavType.StringType })
            ) { backStackEntry ->
                val methodId = backStackEntry.arguments?.getString("methodId") ?: ""
                StudyMethodDetailScreen(navController, methodId)
            }
            
            composable("statistics") { StatisticsScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            
            composable(
                route = "pomodoro?subjectId={subjectId}&taskId={taskId}",
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("taskId") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
                val taskId = backStackEntry.arguments?.getInt("taskId").let { if (it == -1) null else it }
                PomodoroScreen(navController, subjectId, taskId)
            }

            composable(
                route = "feynman?subjectId={subjectId}&taskId={taskId}",
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("taskId") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
                val taskId = backStackEntry.arguments?.getInt("taskId").let { if (it == -1) null else it }
                FeynmanScreen(navController, subjectId, taskId)
            }

            composable(
                route = "flashcard?subjectId={subjectId}",
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
                FlashcardScreen(navController, subjectId)
            }
            
            composable("cornell?subjectId={subjectId}", arguments = listOf(navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 })) {
                CornellScreen(navController, it.arguments?.getInt("subjectId") ?: -1)
            }
            composable("blurting?subjectId={subjectId}", arguments = listOf(navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 })) {
                BlurtingScreen(navController, it.arguments?.getInt("subjectId") ?: -1)
            }
            composable("sq3r?subjectId={subjectId}", arguments = listOf(navArgument("subjectId") { type = NavType.IntType; defaultValue = -1 })) {
                SQ3RScreen(navController, it.arguments?.getInt("subjectId") ?: -1)
            }
            composable("time_blocking") {
                TimeBlockingScreen(navController)
            }

            // Phase 6
            composable("preparation") { PreparationScreen(navController) }
            composable("exam_goal_form") { ExamGoalFormScreen(navController) }
            composable("enem") { GoalTypeScreen(navController, "ENEM") }
            composable("vestibulares") { GoalTypeScreen(navController, "Vestibular") }
            composable("essay_module") { EssayModuleScreen(navController) }
            composable("essay_themes") { EssayThemesScreen(navController) }
            
            composable(
                route = "essay_editor/{submissionId}?themeId={themeId}",
                arguments = listOf(
                    navArgument("submissionId") { type = NavType.IntType },
                    navArgument("themeId") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { backStackEntry ->
                val submissionId = backStackEntry.arguments?.getInt("submissionId") ?: -1
                val themeId = backStackEntry.arguments?.getInt("themeId") ?: -1
                EssayEditorScreen(navController, submissionId, themeId)
            }
            
            composable(
                route = "essay_result/{submissionId}",
                arguments = listOf(navArgument("submissionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val submissionId = backStackEntry.arguments?.getInt("submissionId") ?: -1
                EssayResultScreen(navController, submissionId)
            }
            
            composable("simulations") { SimulationsScreen(navController) }
            composable("concursos") { GoalTypeScreen(navController, "Concurso") }
            composable("faculdade") { GoalTypeScreen(navController, "Faculdade") }
            composable("editais") { DocumentAnalyzerScreen(navController) }
            composable("material_generator") { MaterialGeneratorScreen(navController) }
        }
    }
}
