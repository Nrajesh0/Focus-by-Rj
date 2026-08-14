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
        }"""

text = re.sub(r'        if \(latestPackage != null\) \{\n\s*currentForegroundPackage = latestPackage\n\s*\} else \{\n\s*// Fallback for apps that have been open for a while\n\s*val stats = usm\.queryUsageStats\(UsageStatsManager\.INTERVAL_DAILY, now - 1000 \* 60 \* 60, now\)\n\s*if \(!stats\.isNullOrEmpty\(\)\) \{\n\s*val mostRecent = stats\.maxByOrNull \{ it\.lastTimeUsed \}\n\s*if \(mostRecent != null\) \{\n\s*currentForegroundPackage = mostRecent\.packageName\n\s*\}\n\s*\}\n\s*\}', replacement, text)

with open(path, 'w') as f:
    f.write(text)
