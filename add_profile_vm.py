with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.presentation.viewmodel.StatisticsViewModel", "import com.example.presentation.viewmodel.StatisticsViewModel\nimport com.example.presentation.viewmodel.ProfileViewModel")
content = content.replace("viewModel { StatisticsViewModel(get()) }", "viewModel { StatisticsViewModel(get()) }\n    viewModel { ProfileViewModel(get(), get()) }")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
