import re

with open('gradle/libs.versions.toml', 'r') as f:
    toml = f.read()

# versions to remove
bloat_versions = [
    r'coilCompose = ".*"\n',
    r'retrofit = ".*"\n',
    r'converterMoshi = ".*"\n',
    r'loggingInterceptor = ".*"\n',
    r'okhttp = ".*"\n',
    r'moshiKotlin.* = ".*"\n',
    r'datastorePreferences = ".*"\n',
    r'accompanistPermissions = ".*"\n',
    r'camera.* = ".*"\n',
    r'secretsGradlePlugin = ".*"\n'
]
for p in bloat_versions:
    toml = re.sub(p, '', toml)

# libraries to remove
bloat_libs = [
    r'coil-compose = \{.*\}\n',
    r'retrofit = \{.*\}\n',
    r'converter-moshi = \{.*\}\n',
    r'logging-interceptor = \{.*\}\n',
    r'okhttp = \{.*\}\n',
    r'moshi-kotlin.* = \{.*\}\n',
    r'androidx-datastore-preferences = \{.*\}\n',
    r'accompanist-permissions = \{.*\}\n',
    r'androidx-camera-.* = \{.*\}\n'
]
for p in bloat_libs:
    toml = re.sub(p, '', toml)

# plugin
toml = re.sub(r'secrets = \{.*\}\n', '', toml)

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(toml)

with open('build.gradle.kts', 'r') as f:
    root_build = f.read()
root_build = re.sub(r'\s*alias\(libs\.plugins\.secrets\).*', '', root_build)
with open('build.gradle.kts', 'w') as f:
    f.write(root_build)

with open('app/build.gradle.kts', 'r') as f:
    app_build = f.read()

app_build = re.sub(r'\s*alias\(libs\.plugins\.secrets\).*', '', app_build)
# remove the secrets block
app_build = re.sub(r'// Configure the Secrets Gradle Plugin.*?secrets \{.*?\}\n', '', app_build, flags=re.DOTALL)
# buildConfig = true might still be there, that's fine but we can remove it if unused, let's leave it.

with open('app/build.gradle.kts', 'w') as f:
    f.write(app_build)

print("Bloat removed.")
