with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

imports = """import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.SyncRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.SyncRepositoryImpl
import org.koin.dsl.module"""

content = content.replace("import org.koin.dsl.module", imports)

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
