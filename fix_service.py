import re

path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

text = text.replace('        startForeground(NOTIFICATION_ID, notification)', '''        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }''')

with open(path, 'w') as f:
    f.write(text)
