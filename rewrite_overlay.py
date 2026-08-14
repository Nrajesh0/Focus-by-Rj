import re

path = 'app/src/main/java/com/focusbyrj/app/service/BlockOverlayManager.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """        val layout = object : LinearLayout(context) {
            override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
                if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                    if (event.action == android.view.KeyEvent.ACTION_UP) {
                        goHome(context)
                    }
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }.apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#14151D"))
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }"""

text = re.sub(r'        val layout = LinearLayout\(context\)\.apply \{\n.*?\n.*?\n.*?\n.*?\n        \}', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
