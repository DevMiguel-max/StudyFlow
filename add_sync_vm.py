with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.presentation.viewmodel.AuthViewModel", "import com.example.presentation.viewmodel.AuthViewModel\nimport com.example.presentation.viewmodel.SyncViewModel")
content = content.replace("viewModel { AuthViewModel(get()) }", "viewModel { AuthViewModel(get()) }\n    viewModel { SyncViewModel(get()) }")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
