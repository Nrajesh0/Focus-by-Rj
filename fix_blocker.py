import re

path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """        if (shouldBlock) {
            val now = System.currentTimeMillis()
            if (lastBlockedPackage == packageName && (now - lastBlockTime) < 1200) {
                return
            }
            lastBlockedPackage = packageName
            lastBlockTime = now
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    BlockOverlayManager.showOverlay(this@FocusBlockerService, packageName, blockQuote, blockMode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.hideOverlay()
            }
        }"""

text = re.sub(r'        if \(shouldBlock\) \{\n            val now = System.currentTimeMillis\(\)\n            if \(lastBlockedPackage == packageName && \(now - lastBlockTime\) < 1200\) \{\n                return\n            \}\n            lastBlockedPackage = packageName\n            lastBlockTime = now\n            android.os.Handler\(android.os.Looper.getMainLooper\(\)\).post \{\n                try \{\n                    BlockOverlayManager.showOverlay\(this@FocusBlockerService, packageName, blockQuote, blockMode\)\n                \} catch \(e: Exception\) \{\n                    e.printStackTrace\(\)\n                \}\n            \}\n        \}', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
