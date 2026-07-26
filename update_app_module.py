import re

with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

migration_6_7 = """
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop and recreate achievements table with new schema
        database.execSQL("DROP TABLE IF EXISTS `achievements`")
        database.execSQL(\"\"\"
            CREATE TABLE IF NOT EXISTS `achievements` (
                `id` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `description` TEXT NOT NULL, 
                `icon` TEXT NOT NULL, 
                `tier` TEXT NOT NULL, 
                `category` TEXT NOT NULL, 
                `progress` INTEGER NOT NULL, 
                `maxProgress` INTEGER NOT NULL, 
                `isUnlocked` INTEGER NOT NULL, 
                `unlockedAt` INTEGER, 
                PRIMARY KEY(`id`)
            )
        \"\"\")

        // Add new columns to user_profile
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `streakDays` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `maxStreakDays` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `lastStudyDate` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `avatarIcon` TEXT NOT NULL DEFAULT '🧑‍🎓'")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `avatarColor` TEXT NOT NULL DEFAULT '0xFFE0E7FF'")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `gamificationEnabled` INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `soundsEnabled` INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `notificationsEnabled` INTEGER NOT NULL DEFAULT 1")

        // Create challenges table
        database.execSQL(\"\"\"
            CREATE TABLE IF NOT EXISTS `challenges` (
                `id` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `description` TEXT NOT NULL, 
                `type` TEXT NOT NULL, 
                `requiredAmount` INTEGER NOT NULL, 
                `currentAmount` INTEGER NOT NULL, 
                `xpReward` INTEGER NOT NULL, 
                `isCompleted` INTEGER NOT NULL, 
                `expiresAt` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        \"\"\")
    }
}
"""

content = content.replace("val appModule = module {", migration_6_7 + "\nval appModule = module {")
content = content.replace(".addMigrations(MIGRATION_5_6)", ".addMigrations(MIGRATION_5_6, MIGRATION_6_7)")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
