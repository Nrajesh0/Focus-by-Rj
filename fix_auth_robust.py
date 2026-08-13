import re

path = 'app/src/main/java/com/focusbyrj/app/MainActivity.kt'
with open(path, 'r') as f:
    text = f.read()

# Let's check how MainActivity is structured.
print(text.find('class MainActivity'))
