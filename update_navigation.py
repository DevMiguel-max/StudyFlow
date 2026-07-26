with open("app/src/main/java/com/example/presentation/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace('startDestination = "onboarding"', 'startDestination = "login"')

screens = """            composable("onboarding") { OnboardingScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
            composable("recover_password") { RecoverPasswordScreen(navController) }
"""
content = content.replace('            composable("onboarding") { OnboardingScreen(navController) }', screens)

with open("app/src/main/java/com/example/presentation/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
