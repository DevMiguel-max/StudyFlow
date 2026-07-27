import re

with open("app/src/main/java/com/example/data/local/StudyFlowDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("version = 7,", "version = 8,")

with open("app/src/main/java/com/example/data/local/StudyFlowDatabase.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    app_module = f.read()

app_module = app_module.replace(
    ".fallbackToDestructiveMigration()",
    ".addMigrations(com.example.data.local.MIGRATION_7_8)\n            .fallbackToDestructiveMigration()"
)

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(app_module)
