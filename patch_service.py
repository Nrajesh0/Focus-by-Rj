import re

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'r') as f:
    content = f.read()

# Replace block activity intent with overlay call
new_block = """
            lastBlockedPackage = packageName
            lastBlockTime = now
            
            // Show WindowManager Overlay directly!
            // Launch on main thread
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.showOverlay(applicationContext, packageName, blockQuote, blockMode)
            }
"""

content = re.sub(r'            lastBlockedPackage = packageName\n            lastBlockTime = now\n            val intent = Intent\(this, BlockActivity::class\.java\)\.apply \{\n                putExtra\("PACKAGE_NAME", packageName\)\n                putExtra\("QUOTE", blockQuote\)\n                putExtra\("MODE", blockMode\)\n                flags = Intent\.FLAG_ACTIVITY_NEW_TASK or Intent\.FLAG_ACTIVITY_SINGLE_TOP or Intent\.FLAG_ACTIVITY_CLEAR_TOP\n            \}\n            startActivity\(intent\)', new_block, content)

# Also, when the foreground package is the launcher, or something safe, we should hide the overlay.
new_return = """
        if (packageName == applicationContext.packageName ||
            packageName == "com.focusbyrj.app" ||
            packageName == "com.android.systemui" ||
            packageName == "com.android.settings" ||
            packageName.contains("launcher")
        ) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.hideOverlay()
            }
            return
        }
"""

content = re.sub(r'        if \(packageName == applicationContext.packageName \|\|\n            packageName == "com.focusbyrj.app" \|\|\n            packageName == "com.focusbyrj.app" \|\|\n            packageName == "com.android.systemui" \|\|\n            packageName == "com.android.settings" \|\|\n            packageName.contains\("launcher"\)\n        \) return', new_return, content)

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'w') as f:
    f.write(content)
