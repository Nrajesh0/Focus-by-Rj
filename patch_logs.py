import re
path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """        if (latestPackage != null) {
            currentForegroundPackage = latestPackage
        } else if (currentForegroundPackage == null) {
            // Fallback only if we have no cached package
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 60 * 60, now)
            if (!stats.isNullOrEmpty()) {
                val mostRecent = stats.maxByOrNull { it.lastTimeUsed }
                if (mostRecent != null) {
                    currentForegroundPackage = mostRecent.packageName
                }
            }
        }
        android.util.Log.d("FocusGuard", "Current Foreground: $currentForegroundPackage")
        return currentForegroundPackage"""

text = re.sub(r'        if \(latestPackage != null\) \{\n            currentForegroundPackage = latestPackage\n        \} else if \(currentForegroundPackage == null\) \{\n            // Fallback only if we have no cached package\n            val stats = usm\.queryUsageStats\(UsageStatsManager\.INTERVAL_DAILY, now - 1000 \* 60 \* 60, now\)\n            if \(!stats\.isNullOrEmpty\(\)\) \{\n                val mostRecent = stats\.maxByOrNull \{ it\.lastTimeUsed \}\n                if \(mostRecent != null\) \{\n                    currentForegroundPackage = mostRecent\.packageName\n                \}\n            \}\n        \}\n        return currentForegroundPackage', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
