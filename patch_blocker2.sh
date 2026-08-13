#!/bin/bash
awk '
/private fun getForegroundPackage/ {
    print "    private fun getForegroundPackage(): String? {"
    print "        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return currentForegroundPackage"
    print "        val now = System.currentTimeMillis()"
    print "        val startTime = now - 1000 * 20 // 20 second sliding window"
    print "        "
    print "        val events = usm.queryEvents(startTime, now)"
    print "        val event = UsageEvents.Event()"
    print "        var latestPackage: String? = null"
    print "        var latestTime = 0L"
    print "        "
    print "        while (events.hasNextEvent()) {"
    print "            events.getNextEvent(event)"
    print "            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {"
    print "                if (event.timeStamp > latestTime) {"
    print "                    latestTime = event.timeStamp"
    print "                    latestPackage = event.packageName"
    print "                }"
    print "            }"
    print "        }"
    print "        "
    print "        if (latestPackage != null) {"
    print "            currentForegroundPackage = latestPackage"
    print "        }"
    print "        return currentForegroundPackage"
    print "    }"
    skip = 1
    next
}
skip {
    if (/return currentForegroundPackage/) {
        skip = 0
        next
    }
    next
}
{ print }
' app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt > temp_svc2.kt
mv temp_svc2.kt app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt
