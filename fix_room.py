import re
paths = ['app/src/main/java/com/focusbyrj/app/FocusApplication.kt', 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt']
for p in paths:
    with open(p, 'r') as f:
        text = f.read()
    # fallbackToDestructiveMigration(dropAllTables = true) -> fallbackToDestructiveMigration()
    text = re.sub(r'fallbackToDestructiveMigration\([^)]*\)', 'fallbackToDestructiveMigration()', text)
    with open(p, 'w') as f:
        f.write(text)
