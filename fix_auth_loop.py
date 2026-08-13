import re

path = 'app/src/main/java/com/focusbyrj/app/MainActivity.kt'
with open(path, 'r') as f:
    text = f.read()

print("Initial text length:", len(text))
