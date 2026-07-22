import os

# Fix CalendarViewModel
with open("app/src/main/java/com/example/presentation/viewmodel/CalendarViewModel.kt", "r") as f:
    content = f.read()

bad_combine = """    val state: StateFlow<CalendarState> = combine(
        repository.getTasks(),
        repository.getSubjects(),
        repository.getAllStudySessions(),
        repository.getAllReviewSchedules(),
        repository.getAllEssaySubmissions(),
        repository.getAllSimulations(),
        repository.getAllStudyGoals(),
        _selectedDate
    ) { tasks, subjects, sessions, reviews, essays, simulations, goals, selectedDate ->"""

good_combine = """    val state: StateFlow<CalendarState> = combine(
        repository.getTasks(),
        repository.getSubjects(),
        repository.getAllStudySessions(),
        repository.getAllReviewSchedules(),
        repository.getAllEssaySubmissions(),
        repository.getAllSimulations(),
        repository.getAllStudyGoals(),
        _selectedDate
    ) { args: Array<Any> ->
        val tasks = args[0] as List<Task>
        val subjects = args[1] as List<Subject>
        val sessions = args[2] as List<StudySession>
        val reviews = args[3] as List<ReviewSchedule>
        val essays = args[4] as List<EssaySubmission>
        val simulations = args[5] as List<Simulation>
        val goals = args[6] as List<StudyGoal>
        val selectedDate = args[7] as Long"""

content = content.replace(bad_combine, good_combine)
with open("app/src/main/java/com/example/presentation/viewmodel/CalendarViewModel.kt", "w") as f:
    f.write(content)

# Fix StatisticsViewModel
with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "r") as f:
    content = f.read()

bad_combine2 = """        viewModelScope.launch {
            combine(
                repository.getAllStudySessions(),
                repository.getAllStudyGoals(),
                repository.getSubjects(),
                repository.getTasks(),
                repository.getAllReviewSchedules(),
                repository.getAllEssaySubmissions(),
                repository.getAllSimulations(),
                repository.getAllEssayCorrections()
            ) { sessions, goals, subjects, tasks, reviews, essays, simulations, corrections ->
                computeStats(sessions, goals, subjects, tasks, reviews, essays, simulations, corrections)"""

good_combine2 = """        viewModelScope.launch {
            combine(
                repository.getAllStudySessions(),
                repository.getAllStudyGoals(),
                repository.getSubjects(),
                repository.getTasks(),
                repository.getAllReviewSchedules(),
                repository.getAllEssaySubmissions(),
                repository.getAllSimulations(),
                repository.getAllEssayCorrections()
            ) { args: Array<Any> ->
                val sessions = args[0] as List<StudySession>
                val goals = args[1] as List<StudyGoal>
                val subjects = args[2] as List<Subject>
                val tasks = args[3] as List<Task>
                val reviews = args[4] as List<ReviewSchedule>
                val essays = args[5] as List<EssaySubmission>
                val simulations = args[6] as List<Simulation>
                val corrections = args[7] as List<EssayCorrection>
                computeStats(sessions, goals, subjects, tasks, reviews, essays, simulations, corrections)"""

content = content.replace(bad_combine2, good_combine2)
with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "w") as f:
    f.write(content)
