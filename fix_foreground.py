import re

path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """    private fun getForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return currentForegroundPackage
        val now = System.currentTimeMillis()
        val startTime = now - 1000 * 60 * 60 // 1 hour sliding window to ensure we get daily stats if events fail
        
        val events = usm.queryEvents(now - 1000 * 30, now) // 30 seconds for precise events
        val event = UsageEvents.Event()
        var latestPackage: String? = null
        var latestTime = 0L
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp > latestTime) {
                    latestTime = event.timeStamp
                    latestPackage = event.packageName
                }
            }
        }
        
        if (latestPackage != null) {
            currentForegroundPackage = latestPackage
        } else {
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, now)
            if (!stats.isNullOrEmpty()) {
                val mostRecent = stats.maxByOrNull { it.lastTimeUsed }
                if (mostRecent != null) {
                    currentForegroundPackage = mostRecent.packageName
                }
            }
        }
        return currentForegroundPackage
    }"""

# Replace the old getForegroundPackage
text = re.sub(r'    private fun getForegroundPackage\(\): String\? \{.*?\n    \}', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
