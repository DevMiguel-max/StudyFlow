with open("app/src/main/java/com/example/data/local/StudyFlowDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("Challenge::class", "Challenge::class,\n        AIConversation::class,\n        AIMessage::class")
content = content.replace("version = 8,", "version = 9,")

with open("app/src/main/java/com/example/data/local/StudyFlowDatabase.kt", "w") as f:
    f.write(content)
