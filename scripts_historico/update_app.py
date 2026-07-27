with open("app/src/main/java/com/example/StudyFlowApplication.kt", "r") as f:
    content = f.read()

imports = """import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
"""

content = content.replace("import android.app.Application", imports + "import android.app.Application")

init_firebase = """        try {
            val options = FirebaseOptions.Builder()
                .setProjectId("dummy-project-id")
                .setApplicationId("1:1234567890:android:abcdef123456")
                .setApiKey("AIzaSyDummyKey-12345")
                .build()
            FirebaseApp.initializeApp(this, options)
        } catch (e: Exception) {
            // Might be already initialized
        }
"""
content = content.replace("super.onCreate()", "super.onCreate()\n" + init_firebase)

with open("app/src/main/java/com/example/StudyFlowApplication.kt", "w") as f:
    f.write(content)
