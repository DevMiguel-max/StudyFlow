sed -i '/val options = FirebaseOptions.Builder()/,/.build()/d' app/src/main/java/com/example/StudyFlowApplication.kt
sed -i 's/FirebaseApp.initializeApp(this, options)/FirebaseApp.initializeApp(this)/g' app/src/main/java/com/example/StudyFlowApplication.kt
sed -i '/import com.google.firebase.FirebaseOptions/d' app/src/main/java/com/example/StudyFlowApplication.kt
