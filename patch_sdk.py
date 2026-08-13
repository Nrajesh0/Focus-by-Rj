import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace('compileSdk { version = release(36) { minorApiLevel = 1 } }', 'compileSdk = 35')
content = content.replace('targetSdk = 36', 'targetSdk = 35')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
print("SDK patched.")
