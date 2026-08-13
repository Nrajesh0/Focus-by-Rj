with open("app/build.gradle.kts", "r") as f:
    text = f.read()
import re
text = text.replace('  id("org.jetbrains.kotlin.android")\n', '')
text = re.sub(r'  kotlinOptions \{\n    jvmTarget = "17"\n  \}\n', '', text)
with open("app/build.gradle.kts", "w") as f:
    f.write(text)
