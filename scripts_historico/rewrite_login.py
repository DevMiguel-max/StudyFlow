import re

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "r") as f:
    content = f.read()

imports_to_add = """import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import com.example.R
"""

content = content.replace("import org.koin.androidx.compose.koinViewModel", imports_to_add + "import org.koin.androidx.compose.koinViewModel")

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "w") as f:
    f.write(content)
