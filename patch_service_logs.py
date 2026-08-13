import re

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'r') as f:
    content = f.read()

# Add logging
content = content.replace(
    'private fun getForegroundPackage(): String? {',
    'private fun getForegroundPackage(): String? {\n        // Log everything\n'
)

content = content.replace(
    'return currentForegroundPackage\n    }\n\n    private suspend fun checkAndBlockApp(packageName: String) {',
    'android.util.Log.d("FocusGuard", "Foreground package is: $currentForegroundPackage")\n        return currentForegroundPackage\n    }\n\n    private suspend fun checkAndBlockApp(packageName: String) {'
)

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'w') as f:
    f.write(content)
