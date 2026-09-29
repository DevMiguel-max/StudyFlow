import re

with open("app/src/main/java/com/studyflow/app/di/AppModule.kt", "r") as f:
    content = f.read()

missing_migrations = """
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // MANUAL REVIEW NEEDED: cannot infer schema delta for version 8->9
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // MANUAL REVIEW NEEDED: cannot infer schema delta for version 10->11
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // MANUAL REVIEW NEEDED: cannot infer schema delta for version 11->12
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // MANUAL REVIEW NEEDED: cannot infer schema delta for version 13->14
    }
}
"""

content = content.replace("val MIGRATION_9_10 = object : Migration(9, 10) {", missing_migrations + "\nval MIGRATION_9_10 = object : Migration(9, 10) {")

content = content.replace(".addMigrations(MIGRATION_5_6, MIGRATION_6_7)\n        .addMigrations(com.studyflow.app.data.local.MIGRATION_7_8, MIGRATION_9_10, com.studyflow.app.di.MIGRATION_12_13)\n            .fallbackToDestructiveMigration()",
".addMigrations(\n            MIGRATION_5_6,\n            MIGRATION_6_7,\n            com.studyflow.app.data.local.MIGRATION_7_8,\n            MIGRATION_8_9,\n            MIGRATION_9_10,\n            MIGRATION_10_11,\n            MIGRATION_11_12,\n            com.studyflow.app.di.MIGRATION_12_13,\n            MIGRATION_13_14\n        )")

with open("app/src/main/java/com/studyflow/app/di/AppModule.kt", "w") as f:
    f.write(content)
