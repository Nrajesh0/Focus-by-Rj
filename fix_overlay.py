import re

path = 'app/src/main/java/com/focusbyrj/app/service/BlockOverlayManager.kt'
with open(path, 'r') as f:
    text = f.read()

text = text.replace('    fun showOverlay(context: Context, packageName: String, quote: String, mode: String) {', '''    fun showOverlay(context: Context, packageName: String, quote: String, mode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) return
''')

with open(path, 'w') as f:
    f.write(text)
