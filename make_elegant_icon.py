import os

# 1. Background layer
bg_xml = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#000000"
        android:pathData="M0,0h108v108h-108z" />
</vector>
"""
with open('app/src/main/res/drawable/ic_launcher_background.xml', 'w') as f:
    f.write(bg_xml)

# 2. Foreground layer
fg_xml = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#00000000"
        android:strokeColor="#FFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="M 48,78 L 48,40 C 48,28 56,24 64,26 M 34,44 L 62,44" />
</vector>
"""
with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write(fg_xml)

# 3. SVG for F-Droid PNG generation
svg_content = """<svg width="512" height="512" viewBox="0 0 108 108" xmlns="http://www.w3.org/2000/svg">
  <rect width="108" height="108" fill="#000000" />
  <path fill="none" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" d="M 48,78 L 48,40 C 48,28 56,24 64,26 M 34,44 L 62,44" />
</svg>
"""
with open('icon.svg', 'w') as f:
    f.write(svg_content)

print("Icon XMLs and SVG generated.")
