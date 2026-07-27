with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel { SyncViewModel(get()) }", "viewModel { SyncViewModel(get()) }\n    viewModel { com.example.presentation.viewmodel.AITutorViewModel(get()) }")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
