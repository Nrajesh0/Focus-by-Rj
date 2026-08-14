import re

path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """    private fun getForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return currentForegroundPackage
        val now = System.currentTimeMillis()
        
        // Query events for the last hour to ensure we don't miss the current foreground app
        // even if it has been open for a while without new activity transitions.
        val events = usm.queryEvents(now - 1000 * 60 * 60, now)
        val event = UsageEvents.Event()
        var latestPackage: String? = null
        var latestTime = 0L
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            // Track both RESUMED and PAUSED to know what is truly in foreground
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp > latestTime) {
                    latestTime = event.timeStamp
                    latestPackage = event.packageName
                }
            } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                // If the most recent event is a pause of the same package, it might be going to background
                // But we don't strictly clear it here because systemUI might pause it temporarily
            }
        }
        
        if (latestPackage != null) {
            currentForegroundPackage = latestPackage
        } else {
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 60 * 60, now)
            if (!stats.isNullOrEmpty()) {
                val mostRecent = stats.maxByOrNull { it.lastTimeUsed }
                if (mostRecent != null) {
                    currentForegroundPackage = mostRecent.packageName
                }
            }
        }
        return currentForegroundPackage
    }"""

text = re.sub(r'    private fun getForegroundPackage\(\): String\? \{.*?\n    \}', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
