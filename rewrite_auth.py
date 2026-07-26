import re

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "r") as f:
    content = f.read()

# We want to remove the Google Sign in button block
import_credentials_pattern = re.compile(r"import androidx\.credentials\..*?\n", re.DOTALL)
content = re.sub(import_credentials_pattern, "", content)

import_googleid_pattern = re.compile(r"import com\.google\.android\.libraries\.identity\.googleid\..*?\n", re.DOTALL)
content = re.sub(import_googleid_pattern, "", content)

import_build_config = re.compile(r"import com\.studyflow\.app\.BuildConfig\n", re.DOTALL)
content = re.sub(import_build_config, "", content)

google_btn_pattern = re.compile(r"Spacer\(modifier = Modifier\.height\(16\.dp\)\)\s*OutlinedButton\(\s*onClick = \{\s*coroutineScope\.launch \{\s*try \{\s*val credentialManager = CredentialManager\.create\(context\).*?Text\(\"Entrar com Google\", style = MaterialTheme\.typography\.titleMedium\)\s*\}\s*", re.DOTALL)
content = re.sub(google_btn_pattern, "", content)

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "w") as f:
    f.write(content)

