with open("app/build.gradle.kts", "r") as f:
    text = f.read()
import re
# add kotlin android plugin if not present
if "org.jetbrains.kotlin.android" not in text:
    text = text.replace("plugins {", 'plugins {\n  id("org.jetbrains.kotlin.android")')
with open("app/build.gradle.kts", "w") as f:
    f.write(text)
