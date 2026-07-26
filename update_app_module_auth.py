with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

imports = """import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.SyncRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.SyncRepositoryImpl
"""

content = content.replace("import org.koin.core.module.dsl.viewModel", imports + "import org.koin.core.module.dsl.viewModel")

repos = """    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<SyncRepository> { SyncRepositoryImpl(get(), get(), get<com.example.data.local.StudyFlowDatabase>().studyFlowDao()) }
"""
content = content.replace("single<StudyFlowRepository> { StudyFlowRepositoryImpl(get()) }", repos + "    single<StudyFlowRepository> { StudyFlowRepositoryImpl(get()) }")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
