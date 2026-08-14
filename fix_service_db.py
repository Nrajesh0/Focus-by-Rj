import re
path = 'app/src/main/java/com/focusbyrj/app/service/FocusBlockerService.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """        db = (application as com.focusbyrj.app.FocusApplication).database"""
text = re.sub(r'        db = Room\.databaseBuilder\(applicationContext, FocusDatabase::class\.java, "focus_database"\)\n            \.fallbackToDestructiveMigration\(\)\n            \.build\(\)', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
