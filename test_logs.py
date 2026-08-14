import re

path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """        var shouldBlock = false
        var blockQuote = ""
        var blockMode = "HARD"

        // 1. Check 24/7 restrictions
        val restriction = db.appRestrictionDao().getRestriction(packageName)
        android.util.Log.d("FocusGuard", "Checking $packageName: restriction=$restriction, isRestricted=${restriction?.isRestricted}")
"""

text = re.sub(r'        var shouldBlock = false\n        var blockQuote = ""\n        var blockMode = "HARD"\n\n        // 1\. Check 24/7 restrictions\n        val restriction = db\.appRestrictionDao\(\)\.getRestriction\(packageName\)', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
