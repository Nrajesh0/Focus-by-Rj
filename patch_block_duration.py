import re
path = 'app/src/main/java/com/focusbyrj/app/service/BlockOverlayManager.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """                            val unlockMins = context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE).getInt("soft_unlock_duration", 5)
                            TemporaryUnlockManager.grantUnlock(context, packageName, unlockMins)"""

text = re.sub(r'                            TemporaryUnlockManager\.grantUnlock\(context, packageName, 5\)', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
