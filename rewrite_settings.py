with open("settings.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("mavenCentral()", "mavenCentral()\n    maven { url = uri(\"https://jitpack.io\") }")

with open("settings.gradle.kts", "w") as f:
    f.write(content)
