import re

# 1. Update gradle/libs.versions.toml
with open('gradle/libs.versions.toml', 'r') as f:
    toml = f.read()

toml = re.sub(r'agp = ".*"', 'agp = "8.7.3"', toml)
toml = re.sub(r'kotlin = ".*"', 'kotlin = "2.1.0"', toml)
toml = re.sub(r'googleDevtoolsKsp = ".*"', 'googleDevtoolsKsp = "2.1.0-1.0.29"', toml)
toml = re.sub(r'roomRuntime = ".*"', 'roomRuntime = "2.6.1"', toml)
toml = re.sub(r'roomKtx = ".*"', 'roomKtx = "2.6.1"', toml)
toml = re.sub(r'roomCompiler = ".*"', 'roomCompiler = "2.6.1"', toml)

# ensure kotlin android plugin is in the toml if we are on 8.7.3
if "kotlin-android =" not in toml:
    toml += '\nkotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }\n'

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(toml)

# 2. Update settings.gradle.kts
with open('settings.gradle.kts', 'w') as f:
    f.write("""pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}
rootProject.name = "Focus by Rj"
include(":app")
""")

# 3. Update Root build.gradle.kts
with open('build.gradle.kts', 'w') as f:
    f.write("""// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
}
""")

# 4. Refactor app/build.gradle.kts
with open('app/build.gradle.kts', 'r') as f:
    app_build = f.read()

# Add kotlin android plugin to app
if "libs.plugins.kotlin.android" not in app_build:
    app_build = app_build.replace('plugins {', 'plugins {\n  alias(libs.plugins.kotlin.android)')

# Replace "ksp"(...) with ksp(...)
app_build = app_build.replace('"ksp"(libs.androidx.room.compiler)', 'ksp(libs.androidx.room.compiler)')

# Add kotlinOptions block
if "kotlinOptions" not in app_build:
    app_build = app_build.replace('compileOptions {', 'kotlinOptions {\n    jvmTarget = "17"\n  }\n\n  compileOptions {')

with open('app/build.gradle.kts', 'w') as f:
    f.write(app_build)

# 5. Update app/src/main/AndroidManifest.xml
with open('app/src/main/AndroidManifest.xml', 'r') as f:
    manifest = f.read()

manifest = manifest.replace('<uses-permission android:name="android.permission.HIDE_OVERLAY_WINDOWS" />',
                            '<uses-permission android:name="android.permission.HIDE_OVERLAY_WINDOWS" tools:ignore="ProtectedPermissions" />')

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(manifest)
