import re

path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """    private suspend fun checkAndBlockApp(packageName: String) {
        if (packageName == applicationContext.packageName ||
            packageName == "com.focusbyrj.app" ||
            packageName == "com.android.settings" ||
            packageName.contains("launcher")
        ) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.hideOverlay()
            }
            return
        }
        
        // Ignore SystemUI without hiding the overlay to prevent swipe-back bypass
        if (packageName == "com.android.systemui") {
            return
        }"""

text = re.sub(r'    private suspend fun checkAndBlockApp\(packageName: String\) \{\n\s*if \(packageName == applicationContext\.packageName \|\|\n\s*packageName == "com\.focusbyrj\.app" \|\|\n\s*packageName == "com\.android\.systemui" \|\|\n\s*packageName == "com\.android\.settings" \|\|\n\s*packageName\.contains\("launcher"\)\n\s*\) \{\n\s*android\.os\.Handler\(android\.os\.Looper\.getMainLooper\(\)\)\.post \{\n\s*BlockOverlayManager\.hideOverlay\(\)\n\s*\}\n\s*return\n\s*\}', replacement, text)

with open(path, 'w') as f:
    f.write(text)
