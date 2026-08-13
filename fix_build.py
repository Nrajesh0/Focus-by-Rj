import re

path = '/app/applet/app/build.gradle.kts'
with open(path, 'r') as f:
    text = f.read()

# Remove the debugConfig block
text = re.sub(r'\s*create\("debugConfig"\)\s*\{[\s\S]*?keyPassword\s*=\s*"android"\s*\}', '', text)
# Change the debug build type signingConfig
text = re.sub(r'debug\s*\{\s*signingConfig\s*=\s*signingConfigs\.getByName\("debugConfig"\)\s*\}', 'debug { signingConfig = signingConfigs.getByName("debug") }', text)

with open(path, 'w') as f:
    f.write(text)

