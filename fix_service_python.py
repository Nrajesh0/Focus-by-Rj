import re

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'r') as f:
    content = f.read()

# Match everything from `private fun getForegroundPackage(): String? {` to `private suspend fun checkAndBlockApp`
pattern = re.compile(r'    private fun getForegroundPackage\(\): String\? \{.*?    private suspend fun checkAndBlockApp', re.DOTALL)

new_func = """    private var currentForegroundPackage: String? = null

    private fun getForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return currentForegroundPackage
        val now = System.currentTimeMillis()
        val startTime = now - 1000 * 30 // 30 second sliding window
        
        val events = usm.queryEvents(startTime, now)
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
        }
        return currentForegroundPackage
    }

    private suspend fun checkAndBlockApp"""

content = re.sub(pattern, new_func, content)

with open('app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt', 'w') as f:
    f.write(content)
