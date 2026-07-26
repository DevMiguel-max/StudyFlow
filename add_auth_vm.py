with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.presentation.viewmodel.ProfileViewModel", "import com.example.presentation.viewmodel.ProfileViewModel\nimport com.example.presentation.viewmodel.AuthViewModel")
content = content.replace("viewModel { ProfileViewModel(get(), get()) }", "viewModel { ProfileViewModel(get(), get()) }\n    viewModel { AuthViewModel(get()) }")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
