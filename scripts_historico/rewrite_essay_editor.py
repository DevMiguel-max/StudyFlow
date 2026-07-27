with open("app/src/main/java/com/example/presentation/screens/EssayEditorScreen.kt", "r") as f:
    content = f.read()

target = """                            viewModel.saveSubmission(text, timeSpentSeconds, "sent")
                            viewModel.requestCorrection(state.currentSubmission?.id ?: -1)
                            navController.popBackStack()"""
replacement = """                            viewModel.submitForCorrection(text, timeSpentSeconds)
                            navController.popBackStack()"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/presentation/screens/EssayEditorScreen.kt", "w") as f:
    f.write(content)
