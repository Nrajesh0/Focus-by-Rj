import re

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'r') as f:
    content = f.read()

new_func = """
        if (latestPackage != null) {
            currentForegroundPackage = latestPackage
        } else if (currentForegroundPackage == null) {
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, now)
            if (!stats.isNullOrEmpty()) {
                val mostRecent = stats.maxByOrNull { it.lastTimeUsed }
                if (mostRecent != null) {
                    currentForegroundPackage = mostRecent.packageName
                }
            }
        }
        return currentForegroundPackage
"""

content = re.sub(r'        if \(latestPackage != null\) \{\n            currentForegroundPackage = latestPackage\n        \}\n        return currentForegroundPackage', new_func, content)

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'w') as f:
    f.write(content)
