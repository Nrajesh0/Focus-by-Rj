import re

with open('app/src/main/java/com/focusbyrj/app/ui/screens/BlockActivity.kt', 'r') as f:
    content = f.read()

# Add onNewIntent
new_intent_func = """    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }
"""

content = content.replace("    override fun onCreate(savedInstanceState: Bundle?) {", new_intent_func + "\n    override fun onCreate(savedInstanceState: Bundle?) {")

with open('app/src/main/java/com/focusbyrj/app/ui/screens/BlockActivity.kt', 'w') as f:
    f.write(content)
