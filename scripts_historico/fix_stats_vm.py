import re

with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "r") as f:
    content = f.read()

replacement = """            ) { args: Array<Any?> ->
                val sessions = args[0] as List<StudySession>
                val profile = args[1] as? com.example.data.local.UserProfile
                val goals = args[2] as List<StudyGoal>
                val subjects = args[3] as List<Subject>
                val tasks = args[4] as List<Task>
                val reviews = args[5] as List<ReviewSchedule>
                val essays = args[6] as List<EssaySubmission>
                val simulations = args[7] as List<Simulation>
                val corrections = args[8] as List<EssayCorrection>
                val newState = computeStats(sessions, goals, subjects, tasks, reviews, essays, simulations, corrections)
                newState.copy(userProfile = profile)
            }.collect { newState ->"""

content = re.sub(r"\) \{ args: Array<Any\?> ->[\s\S]*?\}\.collect \{ newState ->", replacement, content)

with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "w") as f:
    f.write(content)
