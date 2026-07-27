with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

migration_9_10 = """
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recreate essay_corrections table with new fields
        database.execSQL("DROP TABLE IF EXISTS `essay_corrections`")
        database.execSQL(\"\"\"
            CREATE TABLE IF NOT EXISTS `essay_corrections` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `submissionId` INTEGER NOT NULL, 
                `comp1` INTEGER NOT NULL, 
                `comp2` INTEGER NOT NULL, 
                `comp3` INTEGER NOT NULL, 
                `comp4` INTEGER NOT NULL, 
                `comp5` INTEGER NOT NULL, 
                `totalScore` INTEGER NOT NULL, 
                `strengths` TEXT NOT NULL, 
                `weaknesses` TEXT NOT NULL, 
                `suggestions` TEXT NOT NULL, 
                `date` INTEGER NOT NULL,
                `generalComment` TEXT NOT NULL DEFAULT '',
                `essayLevel` TEXT NOT NULL DEFAULT '',
                `performanceEstimate` TEXT NOT NULL DEFAULT '',
                `revisedVersion` TEXT NOT NULL DEFAULT '',
                `detailedAnalysisJson` TEXT NOT NULL DEFAULT '',
                `competenciesDetailsJson` TEXT NOT NULL DEFAULT '',
                `modelUsed` TEXT NOT NULL DEFAULT 'gemini-1.5-pro-latest',
                `isDetailed` INTEGER NOT NULL DEFAULT 1
            )
        \"\"\")
    }
}
"""

content = content.replace("val appModule = module {", migration_9_10 + "\nval appModule = module {")
content = content.replace(".addMigrations(com.example.data.local.MIGRATION_7_8)", ".addMigrations(com.example.data.local.MIGRATION_7_8, MIGRATION_9_10)")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
