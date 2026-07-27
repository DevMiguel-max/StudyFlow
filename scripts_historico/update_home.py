import re

with open("app/src/main/java/com/example/presentation/viewmodel/HomeViewModel.kt", "r") as f:
    content = f.read()

state_replacement = """    val lastTechniqueUsed: StudyMethodModel? = null,
    val totalSessionsCompleted: Int = 0,
    val userProfile: com.example.data.local.UserProfile? = null
)"""
content = content.replace("    val totalSessionsCompleted: Int = 0\n)", state_replacement)

combine_replacement = """    val state: StateFlow<HomeState> = combine(
        repository.getSubjects(),
        repository.getTasks(),
        repository.getStudyPlans(),
        repository.getAllStudySessions(),
        repository.getUserProfile()
    ) { subjects, tasks, plans, sessions, profile ->"""
content = content.replace("""    val state: StateFlow<HomeState> = combine(
        repository.getSubjects(),
        repository.getTasks(),
        repository.getStudyPlans(),
        repository.getAllStudySessions()
    ) { subjects, tasks, plans, sessions ->""", combine_replacement)

instantiation_replacement = """            timeStudiedTodayMinutes = timeStudiedToday,
            lastTechniqueUsed = lastTech,
            totalSessionsCompleted = sessions.count { it.status == "completed" },
            userProfile = profile
        )"""
content = content.replace("""            timeStudiedTodayMinutes = timeStudiedToday,
            lastTechniqueUsed = lastTech,
            totalSessionsCompleted = sessions.count { it.status == "completed" }
        )""", instantiation_replacement)

with open("app/src/main/java/com/example/presentation/viewmodel/HomeViewModel.kt", "w") as f:
    f.write(content)
