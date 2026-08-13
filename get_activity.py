import re

path = 'app/src/main/java/com/focusbyrj/app/ui/screens/AuthScreen.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """    var activity: FragmentActivity? = null
    var currentContext = context
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is FragmentActivity) {
            activity = currentContext
            break
        }
        currentContext = currentContext.baseContext
    }"""

text = text.replace("val activity = context as? FragmentActivity", replacement)

with open(path, 'w') as f:
    f.write(text)
